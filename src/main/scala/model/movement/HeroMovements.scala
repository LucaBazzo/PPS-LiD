package model.movement

import controller.GameEvent
import controller.GameEvent.GameEvent
import model.collisions.ImplicitConversions.{RichFloat, _}
import model.entities.{Hero, State}
import utils.ApplicationConstants.{CROUCH_OFFSET, HERO_SIZE_SMALL}

class HeroMovementStrategy(private val entity: Hero, private var speed: Float) extends MovementStrategy {

  private val jumpForce: Float = 7500f.PPM
  private val runningForce: Float = 1000f.PPM
  private val slidingForce: Float = 6000f.PPM
  private val maxRunningVelocity: Float = 35f.PPM

  override def apply(command: GameEvent): Unit = {
    if(this.checkState && checkCommand(command)) {
      command match {
        case GameEvent.Up => this.jump()
        case GameEvent.MoveRight => this.moveRight()
        case GameEvent.MoveLeft => this.moveLeft()
        case GameEvent.Slide => this.slide()
        case GameEvent.Down => this.crouch()
        case GameEvent.DownReleased => this.entity.setState(State.Standing)
      }
    }
  }

  override def stopMovement(): Unit = {
    this.entity.getBody.setLinearVelocity(0,0)
    if(this.entity.getFeet.nonEmpty)
      this.entity.getFeet.get.getBody.setLinearVelocity(0,0)
  }

  private def checkCommand(command: GameEvent): Boolean = command match {
    case GameEvent.Up => entity.getState != State.Falling &&
      entity.getState != State.Somersault && entity.getState != State.Crouch
    case GameEvent.MoveRight | GameEvent.MoveLeft => true
    case GameEvent.Down => entity.getState == State.Running ||
      entity.getState == State.Standing
    case GameEvent.DownReleased => entity.getState == State.Crouch
    case GameEvent.Slide => entity.getState != State.Jumping && entity.getState != State.Falling && entity.getState != State.Somersault
    case GameEvent.UpReleased => false
    case _ => throw new UnsupportedOperationException
  }

  private def jump(): Unit = {
    this.applyLinearImpulse((0, jumpForce))
    if(this.entity.getState == State.Jumping)
      this.entity.setState(State.Somersault)
    else
      this.entity.setState(State.Jumping)
  }

  private def moveRight(): Unit = {
    this.move(maxRunningVelocity, runningForce, right = true)
  }

  private def moveLeft(): Unit = {
    this.move(-maxRunningVelocity, -runningForce, right = false)
  }

  private def move(maxVelocity: Float, runForce: Float, right: Boolean) {
    if(entity.getState != State.Crouch) {
      if(this.entity.isTouchingGround || (!this.entity.isTouchingGround && !this.entity.isColliding)) {
        if ((right && entity.getBody.getLinearVelocity.x <= maxVelocity) ||
          (!right && entity.getBody.getLinearVelocity.x >= maxVelocity)) {
          this.applyLinearImpulse(this.setSpeed(runForce, 0))
        }

        if(this.entity.getState == State.Standing)
          this.entity.setState(State.Running)
      }
    }
    entity.setFacing(right = right)
  }

  private def crouch(): Unit = {
    this.stopMovement()
    entity.changeHeroFixture(HERO_SIZE_SMALL, CROUCH_OFFSET)
    entity.setState(State.Crouch)
    entity.setLittle(true)
  }

  private def slide(): Unit = {
    this.entity.stopMovement()

    if(entity.getState != State.Crouch) {
      this.entity.changeHeroFixture(HERO_SIZE_SMALL, CROUCH_OFFSET)
      this.entity.setLittle(true)
    }

    if (entity.isFacingRight) {
      this.applyLinearImpulse(this.setSpeed(slidingForce, 0))
    }
    else {
      this.applyLinearImpulse(this.setSpeed(-slidingForce, 0))
    }

    this.entity.setState(State.Sliding)
  }

  private def applyLinearImpulse(vector: (Float, Float)): Unit =
    entity.getBody.applyLinearImpulse(entity.vectorScalar(vector), entity.getBody.getWorldCenter, true)

  override def alterSpeed(alteration: Float): Unit = this.speed += alteration

  private def setSpeed(force: (Float, Float)): (Float, Float) = force * speed

  override def apply(): Unit = ???

  private def checkState: Boolean = entity.getState match {
    case State.Sliding | State.Attack01 | State.Attack02
      | State.Attack03 | State.BowAttack | State.Hurt | State.ItemPicked => false
    case _ => true
  }
}

package model.movement

import controller.GameEvent
import controller.GameEvent.GameEvent
import model.collisions.ImplicitConversions.{RichFloat, _}
import model.entities.{Hero, State}
import utils.ApplicationConstants.HERO_SIZE_SMALL

class HeroMovementStrategy(private val entity: Hero, private var speed: Float) extends MovementStrategy {

  private val jumpForce: Float = 7500f.PPM
  private val runningForce: Float = 1000f.PPM
  private val slidingForce: Float = 6000f.PPM
  private val maxRunningVelocity: Float = 35f.PPM

  override def apply(command: GameEvent): Unit = {
    if(checkCommand(command)) {
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

  override def stopMovement(): Unit = this.entity.getBody.setLinearVelocity(0,0)

  private def checkCommand(command: GameEvent): Boolean = {
    if(entity.getState != State.Sliding && entity.getState != State.Attack01 &&
      entity.getState != State.Attack02 && entity.getState != State.Attack03 &&
      entity.getState != State.BowAttack && entity.getState != State.Hurt &&
      entity.getState != State.ItemPicked) {
      command match {
        case GameEvent.Up => return entity.getState != State.Falling &&
          entity.getState != State.Somersault && entity.getState != State.Crouch
        case GameEvent.MoveRight | GameEvent.MoveLeft => return true
        case GameEvent.Down => return entity.getState == State.Running ||
          entity.getState == State.Standing
        case GameEvent.DownReleased => return entity.getState == State.Crouch
        case GameEvent.Slide => return entity.getState != State.Jumping && entity.getState != State.Falling && entity.getState != State.Somersault
        case GameEvent.UpReleased => return false
        case _ => throw new UnsupportedOperationException
      }
    }
    false
  }

  private def jump(): Unit = {
    this.applyLinearImpulse((0, jumpForce))
    if(this.entity.getState == State.Jumping)
      this.entity.setState(State.Somersault)
    else
      this.entity.setState(State.Jumping)
  }

  private def moveRight(): Unit = {
    if(entity.getState != State.Crouch) {
      if(this.entity.isTouchingGround || (!this.entity.isTouchingGround && !this.entity.isColliding)) {
        if (entity.getBody.getLinearVelocity.x <= maxRunningVelocity) {
          this.applyLinearImpulse(this.setSpeed(runningForce, 0))
        }

        if(this.entity.getState == State.Standing)
          this.entity.setState(State.Running)
      }
    }
    entity.setFacing(right = true)
  }

  private def moveLeft(): Unit = {
    if(entity.getState != State.Crouch) {
      if(this.entity.isTouchingGround || (!this.entity.isTouchingGround && !this.entity.isColliding)) {
        if (entity.getBody.getLinearVelocity.x >= -maxRunningVelocity) {
          this.applyLinearImpulse(this.setSpeed(-runningForce, 0))
        }

        if(this.entity.getState == State.Standing)
          this.entity.setState(State.Running)
      }
    }
    entity.setFacing(right = false)
  }

  private def crouch(): Unit = {
    this.stopMovement()
    entity.changeHeroFixture(HERO_SIZE_SMALL, (0f, -20f))
    entity.setState(State.Crouch)
    entity.setLittle(true)
  }

  private def slide(): Unit = {
    this.entity.stopMovement()

    if(entity.getState != State.Crouch) {
      this.entity.changeHeroFixture(HERO_SIZE_SMALL, (0, -20f))
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
}

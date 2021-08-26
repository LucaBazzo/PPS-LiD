package model.movement

import controller.GameEvent
import controller.GameEvent.GameEvent
import model.collisions.ImplicitConversions.{RichFloat, _}
import model.entities.{Hero, MobileEntity, State}
import utils.ApplicationConstants.HERO_SIZE_SMALL

trait MovementStrategy {

  def apply()
  def apply(command: GameEvent)
  def stopMovement()

  def alterSpeed(alteration: Float): Unit = ???
}

class HeroMovementStrategy(private val entity: Hero, private var speed: Float) extends MovementStrategy {

  private val jumpForce: Float = 7500f.PPM
  private val runningForce: Float = 1000f.PPM
  private val slidingForce: Float = 6000f.PPM
  private val maxRunningVelocity: Float = 35f.PPM

  override def apply(command: GameEvent): Unit = {
    if(checkCommand(command)) {
      command match {
        case GameEvent.Jump => jump()
        case GameEvent.MoveRight => moveRight()
        case GameEvent.MoveLeft => moveLeft()
        case GameEvent.Slide => slide()
      }
    }
  }

  override def stopMovement(): Unit = this.entity.getBody.setLinearVelocity(0,0)

  private def checkCommand(command: GameEvent): Boolean = {
    if(entity.getState != State.Sliding && entity.getState != State.Attack01 &&
      entity.getState != State.Attack02 && entity.getState != State.Attack03 &&
      entity.getState != State.BowAttack) {
      command match {
        case GameEvent.Jump => return entity.getState != State.Falling &&
                entity.getState != State.Somersault && entity.getState != State.Crouch
        case GameEvent.MoveRight | GameEvent.MoveLeft => return true
        case GameEvent.Slide => return entity.getState != State.Jumping && entity.getState != State.Falling && entity.getState != State.Somersault
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
      if (entity.getBody.getLinearVelocity.x <= maxRunningVelocity) {
        this.applyLinearImpulse(this.setSpeed(runningForce, 0))
      }

      if(this.entity.getState == State.Standing)
        this.entity.setState(State.Running)
    }
    entity.setFacing(right = true)
  }

  private def moveLeft(): Unit = {
    if(entity.getState != State.Crouch) {
      if (entity.getBody.getLinearVelocity.x >= -maxRunningVelocity) {
        this.applyLinearImpulse(this.setSpeed(-runningForce, 0))
      }

      if(this.entity.getState == State.Standing)
        this.entity.setState(State.Running)
    }
    entity.setFacing(right = false)
  }

  private def slide(): Unit = {
    this.entity.stopMovement()

    if(entity.getState != State.Crouch) {
      this.entity.changeHeroFixture(HERO_SIZE_SMALL, (0, -6f))
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

class ArrowMovementStrategy(private val entity: MobileEntity, private var speed: Float) extends MovementStrategy {

  private val arrowForce: Float = 15000f.PPM

  override def apply(): Unit = {
    if (entity.isFacingRight) {
      this.applyLinearImpulse(this.setSpeed(arrowForce, 0))
    }
    else {
      this.applyLinearImpulse(this.setSpeed(-arrowForce, 0))
    }
  }

  override def apply(command: GameEvent): Unit = ???

  override def stopMovement(): Unit = this.entity.getBody.setLinearVelocity(0,0)

  private def setSpeed(force: (Float, Float)): (Float, Float) = force * speed

  private def applyLinearImpulse(vector: (Float, Float)): Unit =
    entity.getBody.applyLinearImpulse(entity.vectorScalar(vector), entity.getBody.getWorldCenter, true)
}

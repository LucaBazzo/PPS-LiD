package model.movement

import controller.GameEvent
import controller.GameEvent.GameEvent
import model.collisions.ImplicitConversions.{RichFloat, _}
import model.entities.{Hero, State}

class LadderClimbMovementStrategy(private val entity: Hero, private var speed: Float) extends MovementStrategy {

  private val climbForce: Float = 2500f.PPM
  private val maxClimbVelocity: Float = 70f.PPM

  override def apply(command: GameEvent): Unit = {
    if(checkCommand(command)) {
      command match {
        case GameEvent.Up => this.climb()
        case GameEvent.Down => this.descend()
        case GameEvent.UpReleased | GameEvent.DownReleased => this.idle()
      }
    }
  }

  override def stopMovement(): Unit = {
    this.entity.getBody.setLinearVelocity(0,0)
    if(this.entity.getFeet.nonEmpty)
      this.entity.getFeet.get.getBody.setLinearVelocity(0,0)
  }

  private def checkCommand(command: GameEvent): Boolean = {
    if(entity.getState != State.Sliding && entity.getState != State.Attack01 &&
      entity.getState != State.Attack02 && entity.getState != State.Attack03 &&
      entity.getState != State.BowAttack) {
      command match {
        case GameEvent.Up | GameEvent.Down => return true
        case GameEvent.UpReleased => return this.entity.getState == State.LadderClimb
        case GameEvent.DownReleased => return this.entity.getState == State.LadderDescend
        case GameEvent.Slide | GameEvent.MoveLeft | GameEvent.MoveRight => return false
        case _ => throw new UnsupportedOperationException
      }
    }
    false
  }

  private def climb(): Unit = {
    if (entity.getBody.getLinearVelocity.y <= maxClimbVelocity) {
      this.applyLinearImpulse(this.setSpeed(0, climbForce))
    }

    this.entity.setState(State.LadderClimb)
  }

  private def descend(): Unit = {
    if (entity.getBody.getLinearVelocity.y >= -maxClimbVelocity) {
      this.applyLinearImpulse(this.setSpeed(0, -climbForce))
    }

    this.entity.setState(State.LadderDescend)
  }

  private def idle(): Unit = {
    this.stopMovement()
    this.entity.setState(State.LadderIdle)
  }

  private def applyLinearImpulse(vector: (Float, Float)): Unit =
    entity.getBody.applyLinearImpulse(entity.vectorScalar(vector), entity.getBody.getWorldCenter, true)

  override def alterSpeed(alteration: Float): Unit = this.speed += alteration

  private def setSpeed(force: (Float, Float)): (Float, Float) = force * speed

  override def apply(): Unit = ???
}


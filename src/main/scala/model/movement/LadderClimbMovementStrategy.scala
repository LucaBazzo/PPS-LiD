package model.movement

import controller.GameEvent
import controller.GameEvent.GameEvent
import model.collisions.ImplicitConversions.{RichFloat, _}
import model.entities.{Hero, State}

class LadderClimbMovementStrategy(private val entity: Hero, private var speed: Float) extends MovementStrategy {

  private val climbForce: Float = 2500f.PPM
  private val maxClimbVelocity: Float = 70f.PPM

  override def apply(command: GameEvent): Unit = {
    if(checkState && checkCommand(command)) {
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

  override def alterSpeed(alteration: Float): Unit = this.speed += alteration

  override def apply(): Unit = ???

  private def checkState: Boolean = entity.getState match {
    case State.Sliding | State.Attack01 | State.Attack02
         | State.Attack03 | State.BowAttack | State.Hurt | State.ItemPicked => false
    case _ => true
  }

  private def checkCommand(command: GameEvent): Boolean = command match {
    case GameEvent.Up | GameEvent.Down => true
    case GameEvent.UpReleased => this.entity.getState == State.LadderClimb
    case GameEvent.DownReleased => this.entity.getState == State.LadderDescend
    case GameEvent.Slide | GameEvent.MoveLeft | GameEvent.MoveRight => false
    case _ => throw new UnsupportedOperationException
  }

  private def climb(): Unit = {
    this.move(maxClimbVelocity, climbForce, up = true)
    this.entity.setState(State.LadderClimb)
  }

  private def descend(): Unit = {
    this.move(-maxClimbVelocity, -climbForce, up = false)
    this.entity.setState(State.LadderDescend)
  }

  private def move(maxVelocity: Float, force: Float, up: Boolean): Unit = {
    if (up && entity.getBody.getLinearVelocity.y <= maxVelocity ||
          entity.getBody.getLinearVelocity.y >= maxVelocity) {
      this.applyLinearImpulse(this.setSpeed(0, force))
    }
  }

  private def idle(): Unit = {
    this.stopMovement()
    this.entity.setState(State.LadderIdle)
  }

  private def applyLinearImpulse(vector: (Float, Float)): Unit =
    entity.getBody.applyLinearImpulse(entity.vectorScalar(vector), entity.getBody.getWorldCenter, true)

  private def setSpeed(force: (Float, Float)): (Float, Float) = force * speed
}


package model.movement

import controller.GameEvent
import controller.GameEvent.GameEvent
import model.entities.State._
import model.entities.{Hero, State}
import utils.HeroConstants.LADDER_CLIMB_VELOCITY

/** Implementation of the Hero Movement Strategy when the hero is climbing a ladder.
 *
 *  @constructor the hero ladder climb movement strategy
 *  @param entity the entity that will be moved in the world
 *  @param speed a multiplier to the climbing velocity of the hero
 */
class LadderClimbMovementStrategy(private val entity: Hero, private var speed: Float) extends DoNothingMovementStrategy {

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
    this.entity.setVelocity((0,0))
    if(this.entity.getFeet.nonEmpty)
      this.entity.getFeet.get.setVelocity((0,0))
  }

  override def alterSpeed(alteration: Float): Unit = this.speed += alteration

  private def checkState: Boolean = entity.getState match {
    case State.Sliding | State.Attack01 | State.Attack02
         | State.Attack03 | State.BowAttacking | State.Hurt | State.`pickingItem` => false
    case _ => true
  }

  private def checkCommand(command: GameEvent): Boolean = command match {
    case GameEvent.Up | GameEvent.Down => true
    case GameEvent.UpReleased => this.entity is LadderClimbing
    case GameEvent.DownReleased => this.entity is LadderDescending
    case GameEvent.Slide | GameEvent.MoveLeft | GameEvent.MoveRight => false
    case _ => throw new UnsupportedOperationException
  }

  private def climb(): Unit = {
    this.entity.setVelocityY(LADDER_CLIMB_VELOCITY, this.speed)
    this.entity.setState(State.LadderClimbing)
  }

  private def descend(): Unit = {
    this.entity.setVelocityY(-LADDER_CLIMB_VELOCITY, this.speed)
    this.entity.setState(State.LadderDescending)
  }

  private def idle(): Unit = {
    this.stopMovement()
    this.entity.setState(State.LadderIdle)
  }
}


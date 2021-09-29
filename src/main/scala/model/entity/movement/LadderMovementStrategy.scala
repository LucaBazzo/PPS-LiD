package model.entity.movement

import alice.tuprolog.{SolveInfo, Term}
import controller.GameEvent
import controller.GameEvent.GameEvent
import model.entity.{Hero, State}
import utils.HeroConstants.LADDER_CLIMB_VELOCITY
import utils.Scala2P._

/** Implementation of the Hero Movement Strategy when the hero is climbing a ladder.
 *
 *  @constructor the hero ladder climb movement strategy
 *  @param entity the entity that will be moved in the world
 *  @param speed a multiplier to the climbing velocity of the hero
 */
case class LadderMovementStrategy(private val entity: Hero,
                                  private var speed: Float) extends MovementStrategy {

  private val engine: Term => Iterable[SolveInfo] = mkPrologEngine("""
    checkUpAndDown(_).
    checkUpReleased(state(ladderclimbing)).
    checkDownReleased(state(ladderdescending)).

    checkCommand(C, S) :-
      (C=command(up) -> call(checkUpAndDown(_)));
      (C=command(down) -> call(checkUpAndDown(_)));
      (C=command(upreleased) -> call(checkUpReleased(S)));
      (C=command(downreleased) -> call(checkDownReleased(S))).
  """)

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
         | State.Attack03 | State.BowAttacking | State.Hurt | State.PickingItem => false
    case _ => true
  }

  private def checkCommand(command: GameEvent): Boolean = {
    val goal: String = "checkCommand(command(" + command.toString.toLowerCase() + "), " +
      "state(" + entity.getState.toString.toLowerCase() + "))"

    solveWithSuccess(engine, goal)
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


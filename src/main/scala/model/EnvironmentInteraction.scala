package model

import controller.GameEvent.GameEvent
import model.entities.{Hero, State, Statistic}
import model.movement.{HeroMovementStrategy, LadderClimbMovementStrategy}

case class HeroInteraction(command: GameEvent, environmentInteraction: EnvironmentInteraction)

/** An environment interaction between the hero and another entity in the world. Changes
 *  the normal behaviour of the hero.
 *
 */
trait EnvironmentInteraction {

  /** Change the behaviour of the entity attached.
   *
   */
  def apply()

}

/** Implementation of the interaction between hero and ladder. With the apply the hero
 *  will change his movement strategy with the LadderClimbMovementStrategy.
 *
 *  @constructor the hero-ladder environment interaction
 *  @param entity the entity that will change its behavior
 */
class LadderInteraction(entity: Hero) extends EnvironmentInteraction {

  private var applied: Boolean = false

  override def apply(): Unit = {
    this.entity.stopMovement()

    if(!applied)
      this.startLadderInteraction()
    else
      this.restoreNormalMovementStrategy()

    this.applied = !applied
  }

  private def startLadderInteraction(): Unit = {
    this.entity.setMovementStrategy(new LadderClimbMovementStrategy(this.entity, this.entity.getStatistic(Statistic.MovementSpeed).get))
    this.entity.setState(State.LadderIdle)
    this.entity.getEntityBody.setGravityScale(0)
  }

  private def restoreNormalMovementStrategy(): Unit = {
    this.entity.setMovementStrategy(new HeroMovementStrategy(this.entity, this.entity.getStatistic(Statistic.MovementSpeed).get))
    this.entity.getEntityBody.setGravityScale()
    this.entity.setState(State.Falling)
    this.entity.getBody.setAwake(true)
  }
}

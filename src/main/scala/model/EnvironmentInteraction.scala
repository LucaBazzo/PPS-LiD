package model

import controller.GameEvent.GameEvent
import model.entities.{Hero, State, Statistic}
import model.movement.{HeroMovementStrategy, LadderClimbMovementStrategy}

case class HeroInteraction(command: GameEvent, environmentInteraction: EnvironmentInteraction)

trait EnvironmentInteraction {

  def apply()

}

class LadderInteraction(entity: Hero) extends EnvironmentInteraction {

  private var applied: Boolean = false

  override def apply(): Unit = {
    this.entity.stopMovement()

    if(!applied) {
      this.entity.setMovementStrategy(new LadderClimbMovementStrategy(this.entity, this.entity.getStatistic(Statistic.MovementSpeed)))
      this.entity.setState(State.LadderIdle)
      this.entity.getEntityBody.setGravityScale(0)
    }
    else {
      this.entity.setMovementStrategy(new HeroMovementStrategy(this.entity, this.entity.getStatistic(Statistic.MovementSpeed)))
      this.entity.getEntityBody.setGravityScale()
      this.entity.setState(State.Falling)
      this.entity.getBody.setAwake(true)
    }
    this.applied = !applied
  }
}

package model.movement

import model.behaviour.{MovementBehaviours, MovementBehavioursImpl}
import model.entities.{Entity, MobileEntity, State, Statistic}
import model.helpers.EntitiesUtilities._

// TODO: rifattorizzare i predicati usati spesso in delel classi Predicate

abstract class BehaviourMovementStrategy extends MovementStrategyImpl {
  protected val behaviour: MovementBehaviours = new MovementBehavioursImpl()

  override def apply(): Unit = {
    this.behaviour.update
    this.behaviour.getCurrentBehaviour.apply()
  }

  override def stopMovement(): Unit = this.behaviour.getCurrentBehaviour.stopMovement()

  override def onBegin(): Unit = this.behaviour.getCurrentBehaviour.onBegin()

  override def onEnd(): Unit = this.behaviour.getCurrentBehaviour.onEnd()
}

case class MovingMovementStrategy(sourceEntity: MobileEntity,
                                  right:Boolean) extends MovementStrategyImpl {
  private val maxMovementSpeed: Float = sourceEntity.getStatistic(Statistic.MaxMovementSpeed).get
  private val acceleration: Float = sourceEntity.getStatistic(Statistic.Acceleration).get

  override def apply(): Unit = {
    if (this.right) {
      this.sourceEntity.setVelocityX(this.sourceEntity.getVelocity._1 + this.acceleration)
      if (this.sourceEntity.getVelocity._1 > this.maxMovementSpeed)
        this.sourceEntity.setVelocityX(this.maxMovementSpeed)
    } else {
      this.sourceEntity.setVelocityX(this.sourceEntity.getVelocity._1 - this.acceleration)
      if (this.sourceEntity.getVelocity._1 < -this.maxMovementSpeed)
        this.sourceEntity.setVelocityX(-this.maxMovementSpeed)
    }
  }

  override def stopMovement(): Unit = {
    this.sourceEntity.setVelocityX(0)
  }

  override def onBegin(): Unit = {
    this.stopMovement()
    this.sourceEntity.setFacing(right = this.right)
    this.sourceEntity.setState(State.Running)
  }

  override def onEnd(): Unit = {
    this.stopMovement()
    this.sourceEntity.setState(State.Standing)
  }
}

case class FaceTarget(sourceEntity: MobileEntity,
                      targetEntity: Entity) extends MovementStrategyImpl {

  override def apply(): Unit = {
    this.sourceEntity.setFacing(right = isEntityOnTheRight(sourceEntity, targetEntity))
  }

  override def stopMovement(): Unit = { }

  override def onBegin(): Unit = {
    this.sourceEntity.setState(State.Standing)
  }

  override def onEnd(): Unit = { }
}


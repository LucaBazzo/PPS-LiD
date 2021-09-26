package model.movement

import model.behaviour.MovementBehavioursImpl
import model.collisions.ImplicitConversions.entityToBody
import model.entities.{Entity, MobileEntity, State, Statistic}
import model.helpers.GeometricUtilities.isBodyOnTheRight

abstract class BehaviourMovementStrategy extends MovementStrategyImpl {
  protected val behaviours: MovementBehavioursImpl = new MovementBehavioursImpl()

  override def apply(): Unit = {
    this.behaviours.update()
    this.behaviours.getCurrentBehaviour.apply()
  }

  override def stopMovement(): Unit = this.behaviours.getCurrentBehaviour.stopMovement()

  override def onBegin(): Unit = this.behaviours.getCurrentBehaviour.onBegin()

  override def onEnd(): Unit = this.behaviours.getCurrentBehaviour.onEnd()
}

case class MovingMovementStrategy(sourceEntity: MobileEntity,
                                  right:Boolean) extends MovementStrategyImpl {
  private val movementSpeed: Float = sourceEntity.getStatistic(Statistic.MovementSpeed).get

  override def apply(): Unit = {
    if (this.right) this.sourceEntity.setVelocityX(this.movementSpeed)
    else this.sourceEntity.setVelocityX( - this.movementSpeed)
  }

  override def stopMovement(): Unit = this.sourceEntity.setVelocityX(0)

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
    if (!(List(State.Attack01, State.Attack02, State.Attack03) contains sourceEntity.getState))
      this.sourceEntity.setFacing(right = isBodyOnTheRight(sourceEntity, targetEntity))
  }

  override def stopMovement(): Unit = { }

  override def onBegin(): Unit = {
    this.sourceEntity.setVelocityX(0)
  }

  override def onEnd(): Unit = { }
}


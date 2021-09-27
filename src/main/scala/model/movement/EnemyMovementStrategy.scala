package model.movement

import model.behaviour.{MovementStateManager, StateManagerImpl}
import model.entities.{Entity, MobileEntity, State, Statistic}
import model.helpers.GeometricUtilities.isBodyOnTheRight
import model.helpers.ImplicitConversions.{entityToBody, tupleToVector2}
import model.helpers.WorldUtilities.computeDirectionToTarget

abstract class StatefulMovementStrategy extends MovementStrategy {
  protected val stateManager: MovementStateManager = new StateManagerImpl() with MovementStateManager

  override def apply(): Unit = {
    this.stateManager.update()
    this.stateManager.getCurrentBehaviour.apply()
  }

  override def stopMovement(): Unit = this.stateManager.getCurrentBehaviour.stopMovement()

  override def onBegin(): Unit = this.stateManager.getCurrentBehaviour.onBegin()

  override def onEnd(): Unit = this.stateManager.getCurrentBehaviour.onEnd()
}

case class WalkingMovementStrategy(private val sourceEntity: MobileEntity,
                                   private val right:Boolean) extends MovementStrategy {
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

case class FaceTarget(private val sourceEntity: MobileEntity,
                      private val targetEntity: Entity) extends MovementStrategy {

  override def apply(): Unit = {
    if (!(List(State.Attack01, State.Attack02, State.Attack03) contains sourceEntity.getState))
      this.sourceEntity.setFacing(right = isBodyOnTheRight(sourceEntity, targetEntity))
  }

  override def onBegin(): Unit = {
    this.sourceEntity.setVelocityX(0)
  }
}

case class FlyingMovementStrategy(private val sourceEntity:MobileEntity,
                                  private val targetEntity:Entity) extends MovementStrategy {

  this.sourceEntity.setGravityScale(0)

  override def apply(): Unit = {
    val direction = computeDirectionToTarget(this.sourceEntity.getPosition, this.targetEntity.getPosition,
      sourceEntity.getStatistic(Statistic.MovementSpeed).get)

    this.sourceEntity.setFacing(direction.x > 0)
    this.sourceEntity.setVelocity(direction)
  }

  override def onEnd(): Unit = {
    this.sourceEntity.setVelocity((0, 0))
  }
}

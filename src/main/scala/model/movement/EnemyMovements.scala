package model.movement

import model.behaviour.RichPredicates.funcToPredicate
import model.behaviour.{NotPredicate, _}
import model.entities.{Entity, MobileEntity, State, Statistic}
import model.helpers.EntitiesUtilities._
import utils.EnemiesConstants.ENEMIES_ACTIVATION_DISTANCE

// TODO: rifattorizzare i predicati usati spesso in delel classi Predicate

abstract class BehaviourMovementStrategy extends MovementStrategy {
  protected val behaviour: MovementBehaviours = new MovementBehavioursImpl()

  override def apply(): Unit = {
    this.behaviour.update
    this.behaviour.getCurrentBehaviour.apply()
  }

  override def stopMovement(): Unit = {
    this.behaviour.getCurrentBehaviour.stopMovement()
  }

  override def onBegin(): Unit = this.behaviour.getCurrentBehaviour.onBegin()

  override def onEnd(): Unit = this.behaviour.getCurrentBehaviour.onEnd()
}

case class DisabledMovementStrategy(sourceEntity: MobileEntity,
                                    targetEntity: Entity,
                                    strategy: MovementStrategy) extends BehaviourMovementStrategy {
  val b1: MovementStrategy = behaviour.addBehaviour(DoNothingMovementStrategy())
  val b2: MovementStrategy = behaviour.addBehaviour(strategy)

  behaviour.addTransition(b1, b2,
    () => getEntitiesDistance(this.sourceEntity, this.targetEntity) <= ENEMIES_ACTIVATION_DISTANCE)
  behaviour.addTransition(b2, b1, NotPredicate(
    () => getEntitiesDistance(this.sourceEntity, this.targetEntity) <= ENEMIES_ACTIVATION_DISTANCE))
}

case class EnemyMovementStrategy(sourceEntity: MobileEntity,
                                 targetEntity: Entity) extends BehaviourMovementStrategy {

  case class InnerMovementStrategy(sourceEntity: MobileEntity,
                                   targetEntity: Entity) extends BehaviourMovementStrategy {

    // TODO: magic number qui, codice duplicato
    private val visionAngle: Float = sourceEntity.getStatistic(Statistic.VisionAngle).get
    private val minDistance = sourceEntity.getStatistic(Statistic.VisionDistance).get

    private val isTargetVisible: () => Boolean =
      () => isEntityVisible(this.sourceEntity, this.targetEntity, visionAngle)
    private val isTargetNear: () => Boolean =
      () => getEntitiesDistance(this.sourceEntity, this.targetEntity) <= minDistance
    private val isPathWalkable: () => Boolean =
      () => (!isPathObstructedOnTheLeft(sourceEntity, vOffset = 0) &&
        isFloorPresentOnTheLeft(sourceEntity, vOffset = 0) &&
        isEntityOnTheLeft(this.sourceEntity, this.targetEntity)) ||
        (!isPathObstructedOnTheRight(sourceEntity, vOffset = 0) &&
          isFloorPresentOnTheRight(sourceEntity, vOffset = 0) &&
          isEntityOnTheRight(this.sourceEntity, this.targetEntity))

    private val b1: MovementStrategy = behaviour.addBehaviour(PatrolMovementStrategy(sourceEntity))
    private val b2: MovementStrategy = behaviour.addBehaviour(FaceTarget(sourceEntity, targetEntity))
    private val b3: MovementStrategy = behaviour.addBehaviour(ChaseTarget(sourceEntity, targetEntity))
    private val b4: MovementStrategy = behaviour.addBehaviour(DoNothingMovementStrategy())

    behaviour.addTransition(b1, b2, AllPredicate(List(isTargetNear, isTargetVisible)))
    behaviour.addTransition(b2, b1, NotPredicate(isTargetVisible))
    behaviour.addTransition(b2, b3, AllPredicate(List(NotPredicate(isTargetNear), isTargetVisible, isPathWalkable)))
    behaviour.addTransition(b3, b2, AllPredicate(List(isTargetNear, isTargetVisible)))
    behaviour.addTransition(b3, b1, NotPredicate(isTargetVisible))
    behaviour.addTransition(b3, b4, NotPredicate(isPathWalkable))
    behaviour.addTransition(b4, b1, RandomTruePredicate(30))
    behaviour.addTransition(b4, b2, AllPredicate(List(isTargetNear, isTargetVisible)))
    behaviour.addTransition(b4, b3, AllPredicate(List(NotPredicate(isTargetNear), isTargetVisible, isPathWalkable)))
  }

  this.behaviour.addBehaviour(DisabledMovementStrategy(this.sourceEntity, this.targetEntity,
    InnerMovementStrategy(this.sourceEntity, this.targetEntity)))

}

case class PatrolMovementStrategy(sourceEntity: MobileEntity) extends BehaviourMovementStrategy {

  private val canMoveToTheLeft: () => Boolean =
    () => !isPathObstructedOnTheLeft(sourceEntity, vOffset = 0) &&
      isFloorPresentOnTheLeft(sourceEntity, vOffset = 0)
  private val canMoveToTheRight: () => Boolean =
    () => !isPathObstructedOnTheRight(sourceEntity, vOffset = 0) &&
      isFloorPresentOnTheRight(sourceEntity, vOffset = 0)

  private val b1: MovementStrategy = behaviour.addBehaviour(DoNothingMovementStrategy())
  private val b2: MovementStrategy = behaviour.addBehaviour(MovingMovementStrategy(sourceEntity, right=false))
  private val b3: MovementStrategy = behaviour.addBehaviour(MovingMovementStrategy(sourceEntity, right=true))

  behaviour.addTransition(b1, b2, () => !this.sourceEntity.isFacingRight)
  behaviour.addTransition(b2, b3, NotPredicate(canMoveToTheLeft))

  behaviour.addTransition(b1, b3, () => this.sourceEntity.isFacingRight)
  behaviour.addTransition(b3, b2, NotPredicate(canMoveToTheRight))
}


case class MovingMovementStrategy(sourceEntity: MobileEntity, right:Boolean) extends BehaviourMovementStrategy {
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
                      targetEntity: Entity) extends MovementStrategy {

  override def apply(): Unit = {
    this.sourceEntity.setFacing(right = isEntityOnTheRight(sourceEntity, targetEntity))
  }

  override def stopMovement(): Unit = { }

  override def onBegin(): Unit = {
    this.sourceEntity.setState(State.Standing)
  }

  override def onEnd(): Unit = { }
}


case class ChaseTarget(sourceEntity:MobileEntity,
                       targetEntity:Entity) extends BehaviourMovementStrategy {

  val b1: MovementStrategy = behaviour.addBehaviour(DoNothingMovementStrategy())
  val b2: MovementStrategy = behaviour.addBehaviour(MovingMovementStrategy(sourceEntity, right=false))
  val b3: MovementStrategy = behaviour.addBehaviour(MovingMovementStrategy(sourceEntity, right=true))

  behaviour.addTransition(b1, b2, () => !isEntityOnTheRight(this.sourceEntity, this.targetEntity))
  behaviour.addTransition(b1, b3, () => isEntityOnTheRight(this.sourceEntity, this.targetEntity))
  behaviour.addTransition(b2, b3, () => isEntityOnTheRight(this.sourceEntity, this.targetEntity))
  behaviour.addTransition(b3, b2, () => !isEntityOnTheRight(this.sourceEntity, this.targetEntity))

  override def apply(): Unit = {
    super.apply()
  }
}

package model.behaviour

import model.behaviour.RichTransitions._
import model.behaviour.RichTransitions.LogicalTransition
import model.entities.{Entity, MobileEntity, Statistic}
import model.helpers.EntitiesUtilities._
import model.movement._
import utils.EnemiesConstants.ENEMIES_ACTIVATION_DISTANCE

trait MovementBehaviours extends BehavioursImpl {
  override type Behaviour = MovementStrategy

  def getMovementStrategy: MovementStrategy
}

class MovementBehavioursImpl extends MovementBehaviours {
  override type Behaviour = MovementStrategy

  override def getMovementStrategy: MovementStrategy = this.getCurrentBehaviour

  override def onBehaviourBegin(): Unit = {
    this.getMovementStrategy.onBegin()
  }

  override def onBehaviourEnd(): Unit = {
    this.getMovementStrategy.onEnd()
  }
}

// TODO implementare Disable come mixin?
case class DisabledMovementStrategy(sourceEntity: MobileEntity,
                                    targetEntity: Entity,
                                    strategy: MovementStrategy) extends BehaviourMovementStrategy {
  val b1: MovementStrategy = behaviour.addBehaviour(DoNothingMovementStrategy())
  val b2: MovementStrategy = behaviour.addBehaviour(strategy)

  behaviour.addTransition(b1, b2,
    () => getEntitiesDistance(this.sourceEntity, this.targetEntity) <= ENEMIES_ACTIVATION_DISTANCE)
  behaviour.addTransition(b2, b1, Not(
    () => getEntitiesDistance(this.sourceEntity, this.targetEntity) <= ENEMIES_ACTIVATION_DISTANCE))
}

case class GroundEnemyMovementStrategy(sourceEntity: MobileEntity,
                                       targetEntity: Entity) extends BehaviourMovementStrategy {
  // TODO: magic number qui, codice duplicato
  private val visionAngle: Float = sourceEntity.getStatistic(Statistic.VisionAngle).get
  private val minDistance = sourceEntity.getStatistic(Statistic.VisionDistance).get

  private val isTargetVisible: Transition =
    () => isEntityVisible(this.sourceEntity, this.targetEntity, visionAngle)
  private val isTargetNear: Transition =
    () => getEntitiesDistance(this.sourceEntity, this.targetEntity) <= minDistance
  private val isPathWalkable: Transition =
    () => (!isPathObstructedOnTheLeft(sourceEntity, vOffset = 0) &&
      isFloorPresentOnTheLeft(sourceEntity, vOffset = 0) &&
      isEntityOnTheLeft(this.sourceEntity, this.targetEntity)) ||
      (!isPathObstructedOnTheRight(sourceEntity, vOffset = 0) &&
        isFloorPresentOnTheRight(sourceEntity, vOffset = 0) &&
        isEntityOnTheRight(this.sourceEntity, this.targetEntity))

  private val b1: MovementStrategy = behaviour.addBehaviour(PatrolMovementStrategy(sourceEntity))
  private val b2: MovementStrategy = behaviour.addBehaviour(FaceTarget(sourceEntity, targetEntity))
  private val b3: MovementStrategy = behaviour.addBehaviour(ChaseMovementStrategy(sourceEntity, targetEntity))
  private val b4: MovementStrategy = behaviour.addBehaviour(DoNothingMovementStrategy())

  behaviour.addTransition(b1, b2, isTargetNear && isTargetVisible)
  behaviour.addTransition(b2, b1, Not(isTargetVisible))
  behaviour.addTransition(b2, b3, Not(isTargetNear) && isTargetVisible && isPathWalkable)
  behaviour.addTransition(b3, b2, isTargetNear && isTargetVisible)
  behaviour.addTransition(b3, b1, Not(isTargetVisible))
  behaviour.addTransition(b3, b4, Not(isPathWalkable))
  behaviour.addTransition(b4, b1, RandomlyTrue(30))
  behaviour.addTransition(b4, b2, isTargetNear && isTargetVisible)
  behaviour.addTransition(b4, b3, Not(isTargetNear) && isTargetVisible && isPathWalkable)
}

case class BossMovementStrategy(sourceEntity: MobileEntity,
                                 targetEntity: Entity,
                                distance: Float) extends BehaviourMovementStrategy {

  private val visionAngle: Float = sourceEntity.getStatistic(Statistic.VisionAngle).get

  private val isTargetVisible: Transition =
    () => isEntityVisible(this.sourceEntity, this.targetEntity, visionAngle)
  private val isTargetNear: Transition =
    () => getEntitiesDistance(this.sourceEntity, this.targetEntity) <= distance
  private val isPathWalkable: Transition =
    () => (!isPathObstructedOnTheLeft(sourceEntity, vOffset = 0) &&
      isFloorPresentOnTheLeft(sourceEntity, vOffset = 0) &&
      isEntityOnTheLeft(this.sourceEntity, this.targetEntity)) ||
      (!isPathObstructedOnTheRight(sourceEntity, vOffset = 0) &&
        isFloorPresentOnTheRight(sourceEntity, vOffset = 0) &&
        isEntityOnTheRight(this.sourceEntity, this.targetEntity))

  private val b1: MovementStrategy = behaviour.addBehaviour(FaceTarget(sourceEntity, targetEntity))
  private val b2: MovementStrategy = behaviour.addBehaviour(ChaseMovementStrategy(sourceEntity, targetEntity))

  behaviour.addTransition(b1, b2, Not(isTargetNear) && isTargetVisible && isPathWalkable)
  behaviour.addTransition(b2, b1, isTargetNear || Not(isTargetVisible) || Not(isPathWalkable))
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
  behaviour.addTransition(b2, b3, Not(canMoveToTheLeft))

  behaviour.addTransition(b1, b3, () => this.sourceEntity.isFacingRight)
  behaviour.addTransition(b3, b2, Not(canMoveToTheRight))
}

case class ChaseMovementStrategy(sourceEntity:MobileEntity,
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


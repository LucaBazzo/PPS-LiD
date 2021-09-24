package model.behaviour

import model.behaviour.RichTransitions.LogicalTransition
import model.entities.{Entity, MobileEntity, Statistic}
import model.helpers.GeometricUtilities.isBodyOnTheRight
import model.movement._
import model.collisions.ImplicitConversions.entityToBody

trait MovementBehaviours {
  def getMovementStrategy: MovementStrategy
}

class MovementBehavioursImpl extends BehavioursImpl with MovementBehaviours {
  override type Behaviour = MovementStrategy

  override def getMovementStrategy: MovementStrategy = this.getCurrentBehaviour

  override def onBehaviourBegin(): Unit = {
    this.getMovementStrategy.onBegin()
  }

  override def onBehaviourEnd(): Unit = {
    this.getMovementStrategy.onEnd()
  }
}

case class GroundEnemyMovementStrategy(sourceEntity: MobileEntity,
                                       targetEntity: Entity) extends BehaviourMovementStrategy {
  private val WAIT_MOVEMENT_PROBABILITY: Float = 0.3f

  private val minDistance = sourceEntity.getStatistic(Statistic.VisionDistance).get

  private val b1: MovementStrategy = behaviours.addBehaviour(PatrolMovementStrategy(sourceEntity))
  private val b2: MovementStrategy = behaviours.addBehaviour(FaceTarget(sourceEntity, targetEntity))
  private val b3: MovementStrategy = behaviours.addBehaviour(ChaseMovementStrategy(sourceEntity, targetEntity))
  private val b4: MovementStrategy = behaviours.addBehaviour(DoNothingMovementStrategy())

  behaviours.addTransition(b1, b2, IsTargetNearby(sourceEntity, targetEntity, this.minDistance) &&
    IsTargetVisible(sourceEntity, targetEntity))

  behaviours.addTransition(b2, b1, Not(IsTargetVisible(sourceEntity, targetEntity)))

  behaviours.addTransition(b2, b3, Not(IsTargetNearby(sourceEntity, targetEntity, this.minDistance)) &&
    IsTargetVisible(sourceEntity, targetEntity) &&
    IsPathWalkable(sourceEntity, targetEntity))

  behaviours.addTransition(b3, b2, IsTargetNearby(sourceEntity, targetEntity, this.minDistance) &&
    IsTargetVisible(sourceEntity, targetEntity))

  behaviours.addTransition(b3, b1, Not(IsTargetVisible(sourceEntity, targetEntity)))

  behaviours.addTransition(b3, b4, Not(IsPathWalkable(sourceEntity, targetEntity)))

  behaviours.addTransition(b4, b1, RandomlyTrue(WAIT_MOVEMENT_PROBABILITY))

  behaviours.addTransition(b4, b2, IsTargetNearby(sourceEntity, targetEntity, this.minDistance) &&
    IsTargetVisible(sourceEntity, targetEntity))

  behaviours.addTransition(b4, b3, Not(IsTargetNearby(sourceEntity, targetEntity, this.minDistance)) &&
    IsTargetVisible(sourceEntity, targetEntity) &&
    IsPathWalkable(sourceEntity, targetEntity))
}

case class BossMovementStrategy(sourceEntity: MobileEntity,
                                targetEntity: Entity,
                                distance: Float) extends BehaviourMovementStrategy {

  private val b1: MovementStrategy = behaviours.addBehaviour(FaceTarget(sourceEntity, targetEntity))
  private val b2: MovementStrategy = behaviours.addBehaviour(ChaseMovementStrategy(sourceEntity, targetEntity))

  behaviours.addTransition(b1, b2, Not(IsTargetNearby(sourceEntity, targetEntity, this.distance)) &&
    IsTargetVisible(sourceEntity, targetEntity) &&
    IsPathWalkable(sourceEntity, targetEntity))

  behaviours.addTransition(b2, b1, IsTargetNearby(sourceEntity, targetEntity, this.distance) ||
    Not(IsTargetVisible(sourceEntity, targetEntity)) ||
    Not(IsPathWalkable(sourceEntity, targetEntity)))
}

case class PatrolMovementStrategy(sourceEntity: MobileEntity) extends BehaviourMovementStrategy {

  private val isFacingRight: Transition = () => this.sourceEntity.isFacingRight

  private val b1: MovementStrategy = behaviours.addBehaviour(DoNothingMovementStrategy())
  private val b2: MovementStrategy = behaviours.addBehaviour(MovingMovementStrategy(sourceEntity, right=false))
  private val b3: MovementStrategy = behaviours.addBehaviour(MovingMovementStrategy(sourceEntity, right=true))

  behaviours.addTransition(b1, b2, Not(isFacingRight))

  behaviours.addTransition(b2, b3, Not(CanMoveToTheLeft(sourceEntity)))

  behaviours.addTransition(b1, b3, isFacingRight)

  behaviours.addTransition(b3, b2, Not(CanMoveToTheRight(sourceEntity)))
}

case class ChaseMovementStrategy(sourceEntity:MobileEntity,
                                 targetEntity:Entity) extends BehaviourMovementStrategy {

  private val isTargetOnTheRight:Transition = () => isBodyOnTheRight(this.sourceEntity, this.targetEntity)

  private val b1: MovementStrategy = behaviours.addBehaviour(DoNothingMovementStrategy())
  private val b2: MovementStrategy = behaviours.addBehaviour(MovingMovementStrategy(sourceEntity, right=false))
  private val b3: MovementStrategy = behaviours.addBehaviour(MovingMovementStrategy(sourceEntity, right=true))

  behaviours.addTransition(b1, b2, Not(isTargetOnTheRight))

  behaviours.addTransition(b1, b3, isTargetOnTheRight)

  behaviours.addTransition(b2, b3, isTargetOnTheRight)

  behaviours.addTransition(b3, b2, Not(isTargetOnTheRight))
}


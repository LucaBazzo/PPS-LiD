package model.behaviour

import model.behaviour.RichTransitions.LogicalTransition
import model.helpers.ImplicitConversions.entityToBody
import model.entities.{Entity, MobileEntity}
import model.helpers.GeometricUtilities.isBodyOnTheRight
import model.movement._

trait MovementBehaviours {
  def getMovementStrategy: MovementStrategy
}

class MovementStateManagerImpl extends StateManagerImpl with MovementBehaviours {
  override type State = MovementStrategy

  override def getMovementStrategy: MovementStrategy = this.getCurrentBehaviour

  override def onBehaviourBegin(): Unit = {
    this.getMovementStrategy.onBegin()
  }

  override def onBehaviourEnd(): Unit = {
    this.getMovementStrategy.onEnd()
  }
}

case class GroundEnemyMovementStrategy(private val sourceEntity: MobileEntity,
                                       private val targetEntity: Entity,
                                       private val visionDistance: Float) extends StatefulMovementStrategy {
  private val WAIT_PROBABILITY: Float = 0.3f

  private val b1: MovementStrategy = stateManager.addBehaviour(PatrolMovementStrategy(sourceEntity))
  private val b2: MovementStrategy = stateManager.addBehaviour(FaceTarget(sourceEntity, targetEntity))
  private val b3: MovementStrategy = stateManager.addBehaviour(ChaseMovementStrategy(sourceEntity, targetEntity))
  private val b4: MovementStrategy = stateManager.addBehaviour(DoNothingMovementStrategy())

  stateManager.addTransition(b1, b2, IsTargetNearby(sourceEntity, targetEntity, this.visionDistance) &&
    IsTargetVisible(sourceEntity, targetEntity))

  stateManager.addTransition(b2, b1, Not(IsTargetVisible(sourceEntity, targetEntity)))

  stateManager.addTransition(b2, b3, Not(IsTargetNearby(sourceEntity, targetEntity, this.visionDistance)) &&
    IsTargetVisible(sourceEntity, targetEntity) &&
    IsPathWalkable(sourceEntity, targetEntity))

  stateManager.addTransition(b3, b2, IsTargetNearby(sourceEntity, targetEntity, this.visionDistance) &&
    IsTargetVisible(sourceEntity, targetEntity))

  stateManager.addTransition(b3, b1, Not(IsTargetVisible(sourceEntity, targetEntity)))

  stateManager.addTransition(b3, b4, Not(IsPathWalkable(sourceEntity, targetEntity)))

  stateManager.addTransition(b4, b1, RandomlyTrue(WAIT_PROBABILITY))

  stateManager.addTransition(b4, b2, IsTargetNearby(sourceEntity, targetEntity, this.visionDistance) &&
    IsTargetVisible(sourceEntity, targetEntity))

  stateManager.addTransition(b4, b3, Not(IsTargetNearby(sourceEntity, targetEntity, this.visionDistance)) &&
    IsTargetVisible(sourceEntity, targetEntity) &&
    IsPathWalkable(sourceEntity, targetEntity))
}

case class PatrolMovementStrategy(private val sourceEntity: MobileEntity) extends StatefulMovementStrategy {

  private val isFacingRight: Transition = () => this.sourceEntity.isFacingRight

  private val b1: MovementStrategy = stateManager.addBehaviour(DoNothingMovementStrategy())
  private val b2: MovementStrategy = stateManager.addBehaviour(MovingMovementStrategy(sourceEntity, right=false))
  private val b3: MovementStrategy = stateManager.addBehaviour(MovingMovementStrategy(sourceEntity, right=true))

  stateManager.addTransition(b1, b2, Not(isFacingRight))

  stateManager.addTransition(b2, b3, Not(CanMoveToTheLeft(sourceEntity)))

  stateManager.addTransition(b1, b3, isFacingRight)

  stateManager.addTransition(b3, b2, Not(CanMoveToTheRight(sourceEntity)))
}

case class ChaseMovementStrategy(private val sourceEntity:MobileEntity,
                                 private val targetEntity:Entity) extends StatefulMovementStrategy {

  private val isTargetOnTheRight:Transition = () => isBodyOnTheRight(this.sourceEntity, this.targetEntity)

  private val b1: MovementStrategy = stateManager.addBehaviour(DoNothingMovementStrategy())
  private val b2: MovementStrategy = stateManager.addBehaviour(MovingMovementStrategy(sourceEntity, right=false))
  private val b3: MovementStrategy = stateManager.addBehaviour(MovingMovementStrategy(sourceEntity, right=true))

  stateManager.addTransition(b1, b2, Not(isTargetOnTheRight))

  stateManager.addTransition(b1, b3, isTargetOnTheRight)

  stateManager.addTransition(b2, b3, isTargetOnTheRight)

  stateManager.addTransition(b3, b2, Not(isTargetOnTheRight))
}

case class FlyingEnemyMovementStrategy(private val sourceEntity:MobileEntity,
                                       private val targetEntity:Entity,
                                       private val visionDistance: Float) extends StatefulMovementStrategy {

  private val b1: MovementStrategy = stateManager.addBehaviour(DoNothingMovementStrategy())
  private val b2: MovementStrategy = stateManager.addBehaviour(new FlyingMovementStrategy(sourceEntity, targetEntity))
  private val b3: MovementStrategy = stateManager.addBehaviour(FaceTarget(sourceEntity, targetEntity))

  stateManager.addTransition(b1, b2, IsTargetNearby(sourceEntity, targetEntity, visionDistance))

  stateManager.addTransition(b2, b1, Not(IsTargetNearby(sourceEntity, targetEntity, visionDistance)))
  stateManager.addTransition(b2, b3, IsTargetNearby(sourceEntity, targetEntity, this.sourceEntity.getSize._1))

  stateManager.addTransition(b3, b2, Not(IsTargetNearby(sourceEntity, targetEntity, this.sourceEntity.getSize._1)) &&
    Not(IsEntityAttacking(sourceEntity)))
}



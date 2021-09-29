package model.entity.behaviour

import model.entity.behaviour.RichTransitions.LogicalTransition
import model.helpers.ImplicitConversions.entityToBody
import model.entity.{Entity, MobileEntity}
import model.helpers.GeometricUtilities.isBodyOnTheRight
import model.entity.movement._

/** Particular take of a MovementStrategy. This trait mixes the functionality
 * provided by the MovementStrategy interface with a StateManager to define
 * complex and deep movement strategies derived from other simplier strategies.
 */
trait MovementStateManager extends StateManagerImpl {
  override type State = MovementStrategy

  override def onStateBegin(): Unit = this.getMovementStrategy.onBegin()

  override def onStateEnd(): Unit = this.getMovementStrategy.onEnd()
  
  def getMovementStrategy: MovementStrategy = this.getCurrentState
}

case class GroundEnemyMovementStrategy(private val sourceEntity: MobileEntity,
                                       private val targetEntity: Entity,
                                       private val visionDistance: Float) extends StatefulMovementStrategy {
  private val WAIT_PROBABILITY: Float = 0.3f

  private val b1: MovementStrategy = stateManager.addState(PatrolMovementStrategy(this.sourceEntity))
  private val b2: MovementStrategy = stateManager.addState(FaceTarget(this.sourceEntity, this.targetEntity))
  private val b3: MovementStrategy = stateManager.addState(ChaseMovementStrategy(this.sourceEntity, this.targetEntity))
  private val b4: MovementStrategy = stateManager.addState(DoNothingMovementStrategy())

  stateManager.addTransition(b1, b2, IsTargetNearby(this.sourceEntity, this.targetEntity, this.visionDistance) &&
    IsTargetVisible(this.sourceEntity, this.targetEntity))

  stateManager.addTransition(b2, b1, Not(IsTargetVisible(this.sourceEntity, this.targetEntity)))

  stateManager.addTransition(b2, b3, Not(IsTargetNearby(this.sourceEntity, this.targetEntity, this.visionDistance)) &&
    IsTargetVisible(this.sourceEntity, this.targetEntity) &&
    IsPathWalkable(this.sourceEntity, this.targetEntity))

  stateManager.addTransition(b3, b2, IsTargetNearby(this.sourceEntity, this.targetEntity, this.visionDistance) &&
    IsTargetVisible(this.sourceEntity, this.targetEntity))

  stateManager.addTransition(b3, b1, Not(IsTargetVisible(this.sourceEntity, this.targetEntity)))

  stateManager.addTransition(b3, b4, Not(IsPathWalkable(this.sourceEntity, this.targetEntity)))

  stateManager.addTransition(b4, b1, RandomlyTrue(WAIT_PROBABILITY))

  stateManager.addTransition(b4, b2, IsTargetNearby(this.sourceEntity, this.targetEntity, this.visionDistance) &&
    IsTargetVisible(this.sourceEntity, this.targetEntity))

  stateManager.addTransition(b4, b3, Not(IsTargetNearby(this.sourceEntity, this.targetEntity, this.visionDistance)) &&
    IsTargetVisible(this.sourceEntity, this.targetEntity) &&
    IsPathWalkable(this.sourceEntity, this.targetEntity))
}

case class PatrolMovementStrategy(private val sourceEntity: MobileEntity) extends StatefulMovementStrategy {

  private val isFacingRight: Transition = () => this.sourceEntity.isFacingRight

  private val b1: MovementStrategy = stateManager.addState(DoNothingMovementStrategy())
  private val b2: MovementStrategy = stateManager.addState(WalkingMovementStrategy(this.sourceEntity, right=false))
  private val b3: MovementStrategy = stateManager.addState(WalkingMovementStrategy(this.sourceEntity, right=true))

  stateManager.addTransition(b1, b2, Not(isFacingRight))

  stateManager.addTransition(b2, b3, Not(CanMoveToTheLeft(this.sourceEntity)))

  stateManager.addTransition(b1, b3, isFacingRight)

  stateManager.addTransition(b3, b2, Not(CanMoveToTheRight(this.sourceEntity)))
}

case class ChaseMovementStrategy(private val sourceEntity:MobileEntity,
                                 private val targetEntity:Entity) extends StatefulMovementStrategy {

  private val isTargetOnTheRight:Transition = () => isBodyOnTheRight(this.sourceEntity, this.targetEntity)

  private val b1: MovementStrategy = stateManager.addState(DoNothingMovementStrategy())
  private val b2: MovementStrategy = stateManager.addState(WalkingMovementStrategy(this.sourceEntity, right=false))
  private val b3: MovementStrategy = stateManager.addState(WalkingMovementStrategy(this.sourceEntity, right=true))

  stateManager.addTransition(b1, b2, Not(isTargetOnTheRight))

  stateManager.addTransition(b1, b3, isTargetOnTheRight)

  stateManager.addTransition(b2, b3, isTargetOnTheRight)

  stateManager.addTransition(b3, b2, Not(isTargetOnTheRight))
}

case class FlyingEnemyMovementStrategy(private val sourceEntity:MobileEntity,
                                       private val targetEntity:Entity,
                                       private val visionDistance: Float) extends StatefulMovementStrategy {

  private val b1: MovementStrategy = stateManager.addState(DoNothingMovementStrategy())
  private val b2: MovementStrategy = stateManager.addState(FlyingMovementStrategy(this.sourceEntity, this.targetEntity))
  private val b3: MovementStrategy = stateManager.addState(FaceTarget(this.sourceEntity, this.targetEntity))

  stateManager.addTransition(b1, b2, IsTargetNearby(this.sourceEntity, this.targetEntity, this.visionDistance))

  stateManager.addTransition(b2, b1, Not(IsTargetNearby(this.sourceEntity, this.targetEntity, this.visionDistance)))
  stateManager.addTransition(b2, b3, IsTargetNearby(this.sourceEntity, this.targetEntity, this.sourceEntity.getSize._1))

  stateManager.addTransition(b3, b2, Not(IsTargetNearby(this.sourceEntity, this.targetEntity, this.sourceEntity.getSize._1)) &&
    Not(IsEntityAttacking(this.sourceEntity)))
}



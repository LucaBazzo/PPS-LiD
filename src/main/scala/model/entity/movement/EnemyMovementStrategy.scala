package model.entity.movement

import com.badlogic.gdx.physics.box2d.World
import model.entity.behaviour.{MovementStateManager, StateManagerImpl}
import model.entity.{Entity, MobileEntity, State, Statistic}
import model.helpers.EntitiesFactoryImpl
import model.helpers.GeometricUtilities.isBodyOnTheRight
import model.helpers.ImplicitConversions.{entityToBody, tupleToVector2}
import model.helpers.ImplicitConversions.RichWorld

/** Particular take of a MovementStrategy. This abstract class mixes the
 * functionality provided by the MovementStrategy interface with a StateManager
 * to define complex and deep movement strategies derived from other simpler
 * strategies.
 *
 * A stateful movement strategy may define inner MovementStateManager objects
 * which recursively may use more simple movement strategies.
 */
abstract class StatefulMovementStrategy extends MovementStrategy {
  protected val stateManager: MovementStateManager = new StateManagerImpl() with MovementStateManager

  override def apply(): Unit = {
    this.stateManager.update()
    this.stateManager.getMovementStrategy.apply()
  }

  override def stopMovement(): Unit = this.stateManager.getCurrentState.stopMovement()

  override def onBegin(): Unit = this.stateManager.getCurrentState.onBegin()

  override def onEnd(): Unit = this.stateManager.getCurrentState.onEnd()
}

/** The movement strategy adopted by walking basic enemies.
 *
 * @param sourceEntity the owner of this strategy
 * @param right specify if the entity should move to the left or the right
 */
case class WalkingMovementStrategy(private val sourceEntity: MobileEntity,
                                   private val right: Boolean) extends MovementStrategy {
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

/** Face the target entity and dont move. Useful if combined with an attack
 * strategy.
 *
 * @param sourceEntity the owner of this strategy
 * @param targetEntity the target entity, usually the hero
 */
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

/** Movement strategy adopted by flying enemies. They can ignore obstacles and
 * move directly towards the target entity, if near enught
 *
 * @param sourceEntity the owner of this strategy
 * @param targetEntity the target entity, usually the hero
 */
case class FlyingMovementStrategy(private val sourceEntity:MobileEntity,
                                  private val targetEntity:Entity) extends MovementStrategy {

  val world: World = EntitiesFactoryImpl.getEntitiesContainerMonitor.getWorld.get

  this.sourceEntity.setGravityScale(0)

  override def apply(): Unit = {
    val direction = world.computeDirectionToTarget(this.sourceEntity.getPosition, this.targetEntity.getPosition,
      sourceEntity.getStatistic(Statistic.MovementSpeed).get)

    this.sourceEntity.setFacing(direction.x > 0)
    this.sourceEntity.setVelocity(direction)
  }

  override def onEnd(): Unit = {
    this.sourceEntity.setVelocity((0, 0))
  }
}

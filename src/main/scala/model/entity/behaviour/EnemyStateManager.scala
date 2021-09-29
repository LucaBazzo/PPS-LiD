package model.entity.behaviour

import model.entity.attack._
import model.entity.collision.CollisionStrategy
import model.entity.movement.MovementStrategy

/** Implementation of the StateManagerImpl abstract class. Here the State type
 * is defined as triple of collision, movement and attack strategies.
 *
 * This trait may be mixed with StateManagerImpl to define a concrete
 * implementation.
 */
trait EnemyStateManager extends StateManagerImpl {

  override type State = (CollisionStrategy, MovementStrategy, AttackStrategy)

  def getCollisionStrategy: CollisionStrategy = this.getCurrentState._1
  def getMovementStrategy: MovementStrategy = this.getCurrentState._2
  def getAttackStrategy: AttackStrategy = this.getCurrentState._3

  override def onStateBegin(): Unit = {
    this.getMovementStrategy.onBegin()
  }

  override def onStateEnd(): Unit = {
    this.getMovementStrategy.onEnd()
  }
}
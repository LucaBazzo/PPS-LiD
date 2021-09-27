package model.behaviour

import model.attack._
import model.collisions.CollisionStrategy
import model.movement.MovementStrategy

trait EnemyStateManager extends StateManagerImpl {

  override type State = (CollisionStrategy, MovementStrategy, AttackStrategy)

  def getCollisionStrategy: CollisionStrategy = this.getCurrentBehaviour._1
  def getMovementStrategy: MovementStrategy = this.getCurrentBehaviour._2
  def getAttackStrategy: AttackStrategy = this.getCurrentBehaviour._3

  override def onBehaviourBegin(): Unit = {
    this.getMovementStrategy.onBegin()
  }

  override def onBehaviourEnd(): Unit = {
    this.getMovementStrategy.onEnd()
  }
}
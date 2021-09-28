package model.entity.behaviour

import model.entity.attack._
import model.entity.collision.CollisionStrategy
import model.entity.movement.MovementStrategy

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
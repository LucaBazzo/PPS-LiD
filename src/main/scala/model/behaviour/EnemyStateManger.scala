package model.behaviour

import model.attack._
import model.collisions.CollisionStrategy
import model.movement.MovementStrategy

trait EnemyBehaviours  {
  def getCollisionStrategy: CollisionStrategy
  def getMovementStrategy: MovementStrategy
  def getAttackStrategy: AttackStrategy
}

class EnemyStateManagerImpl extends StateManagerImpl with EnemyBehaviours  {

  override type State = (CollisionStrategy, MovementStrategy, AttackStrategy)

  override def getCollisionStrategy: CollisionStrategy = this.getCurrentBehaviour._1

  override def getMovementStrategy: MovementStrategy = this.getCurrentBehaviour._2

  override def getAttackStrategy: AttackStrategy = this.getCurrentBehaviour._3

  override def onBehaviourBegin(): Unit = {
    this.getMovementStrategy.onBegin()
  }

  override def onBehaviourEnd(): Unit = {
    this.getMovementStrategy.onEnd()
  }
}
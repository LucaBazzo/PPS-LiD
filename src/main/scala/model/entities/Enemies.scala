package model.entities

import model.EntityBody
import model.entities.EnemyType.EnemyType

object EnemyType extends Enumeration {
  type EnemyType = Value
  val Skeleton, Slime = Value
}


trait Enemy extends LivingEntity {
  def getType(): EnemyType
}

trait Score {
  def getScore: Int
}

class EnemyImpl(private var entityBody: EntityBody,
                private val size: (Float, Float),
                private val statistics:Map[Statistic, Float],
                private val score: Int = 100,
                private val enemyType: EnemyType) extends LivingEntityImpl(entityBody, size, statistics)
          with Enemy with Score {

  val attackDamage = 10

  override def getType(): EnemyType = this.enemyType

  override def getScore: Int = this.score

  override def update(): Unit = {
    this.move // movementStrategy.move()
    attackStrategy.apply()

    if (!attackStrategy.isAttackFinished) this.state = State.Attack01
    else if (this.entityBody.getBody.getLinearVelocity.x != 0) this.state = State.Running
    else this.state = State.Standing
  }
}
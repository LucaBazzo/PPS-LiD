package model.entities

import model.collisions.EntityType
import model.entities.EnemyType.EnemyType
import model.entities.Statistic.Statistic
import model.{EntityBody, Score}

import scala.collection.mutable

object EnemyType extends Enumeration {
  type EnemyType = Value
  val Skeleton, Slime, FireWorm = Value
}

trait Enemy extends LivingEntity {
  def getEnemyType: EnemyType
}

class EnemyImpl(private var entityBody: EntityBody,
                private val size: (Float, Float),
                private val stats:mutable.Map[Statistic, Float],
                private val score: Int = 100,
                private val enemyType: EnemyType) extends LivingEntityImpl(EntityType.Enemy, entityBody, size, stats)
          with Enemy with Score {

  override def getEnemyType: EnemyType = this.enemyType

  override def getScore: Int = this.score

  override def update(): Unit = {
    super.update()
    if (state != State.Dying) {
      movementStrategy.apply()
      attackStrategy.apply()

      if (!attackStrategy.isAttackFinished) this.state = State.Attack01
      else if (this.entityBody.getBody.getLinearVelocity.x != 0) this.state = State.Running
      else this.state = State.Standing
    }
  }

  override def destroyEntity(): Unit = {
    super.destroyEntity()
  }
}
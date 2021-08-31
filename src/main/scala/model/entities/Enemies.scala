package model.entities

import model.entities.EntityType.EntityType
import model.entities.Statistic.Statistic
import model.EntityBody

trait Score {
  def getScore: Int
}

class Enemy(private val entityType: EntityType,
            private var entityBody: EntityBody,
            private val size: (Float, Float),
            private val stats: Map[Statistic, Float],
            private val score: Int = 100) extends LivingEntityImpl(entityType, entityBody, size, stats)
          with LivingEntity with Score {

  override def getScore: Int = this.score

  override def update(): Unit = {
    super.update()
    if (state != State.Dying) {

      this.attackStrategy.apply()
      if (!this.attackStrategy.isAttackFinished) {
        this.state = State.Attack01
      } else {
        this.movementStrategy.apply()
        if (this.entityBody.getBody.getLinearVelocity.x != 0) {
          this.state = State.Running
        } else {
          this.state = State.Standing
        }
      }
    }
  }

//  override def sufferDamage(damage: Float): Unit = {
//    super.sufferDamage(damage)
//  }
//
//  override def destroyEntity(): Unit = {
//    super.destroyEntity()
//  }
}
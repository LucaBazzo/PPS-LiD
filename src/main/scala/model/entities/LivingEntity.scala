package model.entities

import model.EntityBody
import model.attack.{AttackStrategy, DoNotAttack}
import model.entities.EntityId.EntityId
import model.entities.Statistic.Statistic

trait LivingEntity extends MobileEntity {

  def sufferDamage(damage: Float)
  def getLife: Float
  def setAttackStrategy(strategy: AttackStrategy)
  def getStatistics: Map[Statistic, Float]
  def getStatistic(statistic: Statistic): Float
  def alterStatistics(statistic: Statistic, alteration: Float)
}

class LivingEntityImpl(private val entityType: EntityId,
                       private var entityBody: EntityBody,
                       private val size: (Float, Float),
                       private var stats: Map[Statistic, Float])
  extends MobileEntityImpl(entityType, entityBody, size, stats) with LivingEntity {

  protected var attackStrategy: AttackStrategy = new DoNotAttack()

  protected var dyingStateTimer:Long = 0
  protected val dyingStateDuration:Long = 1000

  override def update(): Unit = {
    if (this.state == State.Dying) {
      if (dyingStateTimer == 0) {
        this.dyingStateTimer = System.currentTimeMillis()
      } else if (System.currentTimeMillis() - dyingStateTimer > dyingStateDuration) {
        this.destroyEntity()
      }
    }
  }

  override def sufferDamage(damage: Float): Unit = {
    val trueDamage = damage - this.stats(Statistic.Defence)
    if(trueDamage > 0)
      this.alterStatistics(Statistic.CurrentHealth, -trueDamage)
    if (this.getStatistic(Statistic.CurrentHealth) <= 0) {
      this.state = State.Dying
    }
  }

  override def getLife: Float = this.stats(Statistic.CurrentHealth)

  override def setAttackStrategy(strategy: AttackStrategy): Unit = this.attackStrategy = strategy

  override def alterStatistics(statistic: Statistic, alteration: Float): Unit = {
    super.alterStatistics(statistic, alteration)

    statistic match {
      case Statistic.Strength => this.attackStrategy.alterStrength(alteration)
      case _ =>
    }
  }

  //  override def getStatistics[A <: Int](): HashMap[Statistic, A] = statistics
  //
  //  override def alterStatistics[A <: Int](statistic: Statistic, alteration: A): Unit = statistics(statistic) -> alteration
}

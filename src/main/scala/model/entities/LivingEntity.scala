package model.entities

import model.EntityBody
import model.attack.{AttackStrategy, DoNotAttack}
import model.entities.Statistic.Statistic

import scala.collection.mutable

trait LivingEntity extends MobileEntity {

  def sufferDamage(damage: Float)
  def getLife: Float
  def setAttackStrategy(strategy: AttackStrategy)
  def getStatistics: mutable.Map[Statistic, Float]
  def getStatistic(statistic: Statistic): Float
  def alterStatistics(statistic: Statistic, alteration: Float)
}

class LivingEntityImpl(private val entityType:Short,
                       private var entityBody: EntityBody,
                       private val size: (Float, Float),
                       private var stats:scala.collection.mutable.Map[Statistic, Float])
  extends MobileEntityImpl(entityType, entityBody, size) with LivingEntity {

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
    this.alterStatistics(Statistic.CurrentHealth, this.stats(Statistic.CurrentHealth) - damage)
    if (this.stats(Statistic.CurrentHealth) <= 0) {
      this.state = State.Dying
    }
  }

  override def getLife: Float = this.stats(Statistic.CurrentHealth)

  override def setAttackStrategy(strategy: AttackStrategy): Unit = this.attackStrategy = strategy

  //  override def getStatistics[A <: Int](): HashMap[Statistic, A] = statistics
  //
  //  override def alterStatistics[A <: Int](statistic: Statistic, alteration: A): Unit = statistics(statistic) -> alteration

  override def getStatistics: mutable.Map[Statistic, Float] = stats

  override def alterStatistics(statistic: Statistic, alteration: Float): Unit = stats(statistic) = alteration

  override def getStatistic(statistic:Statistic): Float = {
    if (stats.contains(statistic))
      stats(statistic)
    else
      throw new IllegalArgumentException
  }
}

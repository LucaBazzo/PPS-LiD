package model.entities

import model.EntityBody
import model.attack.AttackStrategy
import model.entities.EntityId.EntityId
import model.entities.Statistic.Statistic

trait LivingEntity extends MobileEntity {

  def sufferDamage(damage: Float)
  def getLife: Float
  def setAttackStrategy(strategy: AttackStrategy)
  def getStatistics: Map[Statistic, Float]
  def alterStatistics(statistic: Statistic, alteration: Float)
}

class LivingEntityImpl(private val entityType: EntityId, private var entityBody: EntityBody, private val size: (Float, Float), private var statistics:Map[Statistic, Float]) extends MobileEntityImpl(entityType, entityBody, size, statistics) with LivingEntity {

  protected var attackStrategy: AttackStrategy = _

  override def sufferDamage(damage: Float): Unit = {
    val trueDamage = damage - this.statistics(Statistic.Defence)
    if(trueDamage > 0)
      this.alterStatistics(Statistic.CurrentHealth, -trueDamage)
  }

  override def getLife: Float = this.statistics(Statistic.Health)

  override def setAttackStrategy(strategy: AttackStrategy): Unit = this.attackStrategy = strategy

  override def getStatistics: Map[Statistic, Float] = statistics

  override def alterStatistics(statistic: Statistic, alteration: Float): Unit = {

    val newValue = statistics(statistic) + alteration
    this.statistics += (statistic -> newValue)

    statistic match {
      case Statistic.MovementSpeed => this.movementStrategy.alterSpeed(alteration)
      case Statistic.Strength => this.attackStrategy.alterStrength(alteration)
      case _ =>
    }
  }





  //  override def getStatistics[A <: Int](): HashMap[Statistic, A] = statistics
  //
  //  override def alterStatistics[A <: Int](statistic: Statistic, alteration: A): Unit = statistics(statistic) -> alteration

}

package model.entities

import model.EntityBody
import model.attack.AttackStrategy
import model.entities.Statistic.Statistic

trait LivingEntity extends MobileEntity {

  def sufferDamage(damage: Float)
  def getLife(): Float
  def setAttackStrategy(strategy: AttackStrategy)
  def getStatistics(): Map[Statistic, Float]
  def alterStatistics(statistic: Statistic, alteration: Float)
}

class LivingEntityImpl(private var entityBody: EntityBody, private val size: (Float, Float), private val statistics:Map[Statistic, Float]) extends MobileEntityImpl(entityBody, size, statistics) with LivingEntity {

  protected var attackStrategy: AttackStrategy = _

  override def sufferDamage(damage: Float): Unit = {}

  override def getLife(): Float = ???

  override def setAttackStrategy(strategy: AttackStrategy): Unit = this.attackStrategy = strategy

  //  override def getStatistics[A <: Int](): HashMap[Statistic, A] = statistics
  //
  //  override def alterStatistics[A <: Int](statistic: Statistic, alteration: A): Unit = statistics(statistic) -> alteration

    override def getStatistics(): Map[Statistic, Float] = statistics

    override def alterStatistics(statistic: Statistic, alteration: Float): Unit = printf("Hero's " + statistic + " +" + alteration.toString + "\n")//statistics(statistic) -> alteration

}

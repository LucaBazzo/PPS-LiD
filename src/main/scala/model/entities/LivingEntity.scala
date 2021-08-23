package model.entities

import model.EntityBody
import model.attack.AttackStrategy

import scala.collection.immutable.HashMap

trait LivingEntity extends MobileEntity {

  def sufferDamage(damage: Float)
  def getLife(): Float
  def setAttackStrategy(strategy: AttackStrategy)
  def getStatistics[A <: Int](): HashMap[String, A]
  def alterStatistics[A <: Int](statistic: String, alteration: A)
}

class LivingEntityImpl(private var entityBody: EntityBody, private val size: (Float, Float)) extends MobileEntityImpl(entityBody, size) with LivingEntity {

  protected var attackStrategy: AttackStrategy = _

  override def sufferDamage(damage: Float): Unit = {}

  override def getLife(): Float = ???

  override def setAttackStrategy(strategy: AttackStrategy): Unit = this.attackStrategy = strategy

  override def getStatistics[A <: Int](): HashMap[String, A] = ???

  override def alterStatistics[A <: Int](statistic: String, alteration: A): Unit = ???

}

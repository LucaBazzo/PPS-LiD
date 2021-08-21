package model.entities

import com.badlogic.gdx.physics.box2d.Body
import model.attack.AttackStrategy

import scala.collection.immutable.HashMap

trait LivingEntity {

  def sufferDamage(damage: Float)
  def getLife(): Float
  def setAttackStrategy(strategy:AttackStrategy)
  def getStatistics[A <: Int](): HashMap[String, A]
  def alterStatistics[A <: Int](statistic: String, alteration: A)
}

class LivingEntityImpl(private var body: Body, private val size: (Float, Float)) extends MobileEntityImpl(body, size) with LivingEntity {
  protected var attackStrategy:AttackStrategy = _

  override def sufferDamage(damage: Float): Unit = {}

  override def getLife(): Float = ???

  override def setAttackStrategy(strategy:AttackStrategy): Unit = attackStrategy = strategy

  override def getStatistics[A <: Int](): HashMap[String, A] = ???

  override def alterStatistics[A <: Int](statistic: String, alteration: A): Unit = ???

}

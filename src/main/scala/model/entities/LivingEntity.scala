package model.entities

import com.badlogic.gdx.physics.box2d.Body

import scala.collection.immutable.HashMap

trait LivingEntity extends MobileEntity {

  def sufferDamage(damage: Float)
  def getLife(): Float
  def setAttackStrategy()
  def getStatistics[A <: Int](): HashMap[String, A]
  def alterStatistics[A <: Int](statistic: String, alteration: A)
}

class LivingEntityImpl(private var body: Body, private val size: (Float, Float)) extends MobileEntityImpl(body, size) with LivingEntity {

  override def sufferDamage(damage: Float): Unit = ???

  override def getLife(): Float = ???

  override def setAttackStrategy(): Unit = ???

  override def getStatistics[A <: Int](): HashMap[String, A] = ???

  override def alterStatistics[A <: Int](statistic: String, alteration: A): Unit = ???
}

package model.entities

import model.EntityBody
import model.entities.Statistic.Statistic

trait Enemy extends LivingEntity {

  def getType()
}

trait Score {
  def getScore: Int
}

class EnemyImpl(private var entityBody: EntityBody, private val size: (Float, Float), private val statistics:Map[Statistic, Float], private val score: Int = 100)
                  extends LivingEntityImpl(entityBody, size, statistics) with Enemy with Score {

  override def getType(): Unit = ???

  override def getScore: Int = this.score

  val attackDamage = 10

  override def update(): Unit = {
    this.move // movementStrategy.move()
    attackStrategy.apply()


  }
}
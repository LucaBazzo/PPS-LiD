package model.entities

import model.EntityBody

trait Enemy extends LivingEntity {

  def getType()
}

trait Score {
  def getScore: Int
}

class EnemyImpl(private var entityBody: EntityBody, private val size: (Float, Float), private val score: Int = 100)
                  extends LivingEntityImpl(entityBody, size) with Enemy with Score {

  override def getType(): Unit = ???

  override def getScore: Int = this.score

  val attackDamage = 10

  override def update(): Unit = {
    this.move // movementStrategy.move()
    attackStrategy.apply()


  }
}
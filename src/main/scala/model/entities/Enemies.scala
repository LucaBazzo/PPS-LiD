package model.entities

import model.EntityBody

trait Enemy {

  def getType()
}

trait Score {
  def getScore: Int
}

class EnemyImpl(private var entityBody: EntityBody, private val size: (Float, Float), private val score: Int)
                  extends LivingEntityImpl(entityBody, size) with Enemy with Score {

  override def getType(): Unit = ???

  override def getScore: Int = this.score
}
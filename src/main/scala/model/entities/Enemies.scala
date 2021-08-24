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

  val attackDamage = 10

  override def getType(): Unit = ???

  override def getScore: Int = this.score

  override def update(): Unit = {
    this.move // movementStrategy.move()
    attackStrategy.apply()

    if (!attackStrategy.isAttackFinished) this.state = State.Attack01
    else if (this.entityBody.getBody.getLinearVelocity.x != 0) this.state = State.Running
    else this.state = State.Standing
  }
}
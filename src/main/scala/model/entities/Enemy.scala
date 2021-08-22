package model.entities

import com.badlogic.gdx.physics.box2d.Body

trait Enemy {
}

trait Score {
  def getScore(): Int
}

case class EnemyImpl(private var body: Body, private val size: (Float, Float)) extends LivingEntityImpl(body, size) with Enemy {
  val attackDamage = 10

  override def update(): Unit = {
    movementStrategy.move()
    attackStrategy.attack()
  }
}

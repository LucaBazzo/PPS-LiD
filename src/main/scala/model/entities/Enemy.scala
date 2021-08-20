package model.entities

import com.badlogic.gdx.physics.box2d.Body

trait Enemy {
}

trait Score {
  def getScore(): Int
}

case class EnemyImpl(private var body: Body, private val size: (Float, Float)) extends LivingEntityImpl(body, size) with Enemy {

  val attackDamage = 10
  val movementSpeed = 5
  val horizontalFieldOfView = 5

  override def update(): Unit = {
    this.move()
  }

}

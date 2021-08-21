package model.entities

import com.badlogic.gdx.physics.box2d.Body

trait Enemy {
}

trait Score {
  def getScore(): Int
}

case class EnemyImpl(private var body: Body, private val size: (Float, Float)) extends LivingEntityImpl(body, size) with Enemy {
  val movementSpeed = 5
  val acceleration = 1

  // TODO: move into movement strategy
  val movementFrequency = 1000
  var lastMovementTime = System.currentTimeMillis()

  val attackDamage = 10

  override def update(): Unit = {
    if (attackStrategy.canAttack()) {
      println("Attacking")
      attackStrategy.attack()
      // set state as attacking
    } else if (movementStrategy.canMove()) {
      println("Moving")
      movementStrategy.move()
      //set state as moving
    } else {
      println("Standing")
      // set state as idle
    }
  }
}

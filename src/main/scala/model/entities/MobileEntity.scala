package model.entities

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import model.movement.{MovementStrategy}

trait MobileEntity {

  def setMovementStrategy(strategy: MovementStrategy)
  def move()
  def getDirection()
}

class MobileEntityImpl(private var body: Body, private val size: (Float, Float)) extends EntityImpl(body, size) with MobileEntity {
  private var movementStrategy: MovementStrategy = _


  override def update(): Unit = {}

  override def setMovementStrategy(strategy: MovementStrategy): Unit = this.movementStrategy = strategy

  override def move(): Unit = {
    this.movementStrategy.move()
  }

  override def getDirection(): Unit = ???
}
package model.entities

import com.badlogic.gdx.physics.box2d.Body
import model.movement.MovementStrategy

trait MobileEntity {

  def setMovementStrategy(strategy: MovementStrategy)
  def getDirection()
}

class MobileEntityImpl(private var body: Body, private val size: (Float, Float)) extends EntityImpl(body, size) with MobileEntity {
  protected var movementStrategy: MovementStrategy = _

  override def update(): Unit = {
    this.movementStrategy.move()
  }

  override def getDirection(): Unit = {}

  override def setMovementStrategy(movementStrategy: MovementStrategy): Unit = this.movementStrategy = movementStrategy

}
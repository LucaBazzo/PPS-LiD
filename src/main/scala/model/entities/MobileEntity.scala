package model.entities

import com.badlogic.gdx.physics.box2d.Body
import model.MovementStrategy

trait MobileEntity extends Entity {

  def setMovementStrategy(strategy: MovementStrategy)
  def move()
  def setFacing(right: Boolean)
  def isFacingRight: Boolean
}

class MobileEntityImpl(private var body: Body, private val size: (Float, Float)) extends EntityImpl(body, size) with MobileEntity {

  private var facingRight: Boolean = true

  protected var movementStrategy: MovementStrategy = _

  override def update(): Unit = this.move()

  override def setMovementStrategy(strategy: MovementStrategy): Unit = this.movementStrategy = strategy

  override def move(): Unit = {}

  override def setFacing(right: Boolean): Unit = this.facingRight = right

  override def isFacingRight: Boolean = this.facingRight
}

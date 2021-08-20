package model.entities

import com.badlogic.gdx.physics.box2d.{Body, Joint}
import model.MovementStrategy
import model.helpers.EntitiesFactoryImpl

trait MobileEntity extends Entity {

  def setMovementStrategy(strategy: MovementStrategy)
  def move()
  def stopMovement()
  def setFacing(right: Boolean)
  def isFacingRight: Boolean
}

class MobileEntityImpl(private var body: Body, private val size: (Float, Float)) extends EntityImpl(body, size) with MobileEntity {

  private var facingRight: Boolean = true

  protected var movementStrategy: MovementStrategy = _

  override def update(): Unit = {}

  override def setMovementStrategy(strategy: MovementStrategy): Unit = this.movementStrategy = strategy

  override def move(): Unit = {
    this.movementStrategy.apply()
  }

  override def stopMovement(): Unit = if(this.movementStrategy != null) this.movementStrategy.stopMovement()

  override def setFacing(right: Boolean): Unit = this.facingRight = right

  override def isFacingRight: Boolean = this.facingRight
}


class CircularMobileEntity(private var body: Body,
                           private val size: (Float, Float),
                           private val pivotBody: Body) extends MobileEntityImpl(body, size) {

  private val joint: Joint = EntitiesFactoryImpl.revoluteJoint(this.pivotBody, this.body)

  override def destroyEntity(): Unit = {
    EntitiesFactoryImpl.destroyJoint(this.joint)
    EntitiesFactoryImpl.destroyBody(this.pivotBody)
    EntitiesFactoryImpl.destroyBody(this.body)
    EntitiesFactoryImpl.removeEntity(this)
  }

}

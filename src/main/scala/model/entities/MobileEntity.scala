package model.entities

import com.badlogic.gdx.physics.box2d.Joint
import model.helpers.EntitiesFactoryImpl
import model.{EntityBody, MovementStrategy}

trait MobileEntity extends Entity {

  def setMovementStrategy(strategy: MovementStrategy)
  def move()
  def stopMovement()
  def setFacing(right: Boolean)
  def isFacingRight: Boolean
}

class MobileEntityImpl(private var entityBody: EntityBody, private val size: (Float, Float)) extends EntityImpl(entityBody, size) with MobileEntity {

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


class CircularMobileEntity(private var entityBody: EntityBody,
                           private val size: (Float, Float),
                           private val pivotBody: EntityBody) extends MobileEntityImpl(entityBody, size) {

  private val joint: Joint = EntitiesFactoryImpl.createJoint(this.pivotBody.getBody, this.entityBody.getBody)

  override def destroyEntity(): Unit = {
    EntitiesFactoryImpl.destroyJoint(this.joint)
    EntitiesFactoryImpl.destroyBody(this.pivotBody.getBody)
    EntitiesFactoryImpl.destroyBody(this.entityBody.getBody)
    EntitiesFactoryImpl.removeEntity(this)
  }

}

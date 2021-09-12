package model.entities

import model.EntityBody
import model.entities.EntityType.EntityType
import model.entities.Statistic.Statistic
import model.helpers.EntitiesFactoryImpl
import model.movement.{DoNotMove, MovementStrategy}
import model.collisions.ImplicitConversions._

object Statistic extends Enumeration {
  type Statistic = Value

  val CurrentHealth, Health, Strength, Defence, MovementSpeed, MaxMovementSpeed, Acceleration, AttackSpeed = Value

  val VisionDistance, VisionAngle, AttackFrequency, AttackDuration    = Value
}

trait MobileEntity extends Entity {
  def setMovementStrategy(strategy: MovementStrategy)

  def move()

  def stopMovement()

  def setFacing(right: Boolean)

  def isFacingRight: Boolean

  def getStatistics: Map[Statistic, Float]

  def alterStatistics(statistic: Statistic, alteration: Float)

  def getStatistic(statistic: Statistic): Option[Float]

  def setVelocity(velocity: (Float, Float), speed: Float = 1)
  def setVelocityX(velocity: Float, speed: Float = 1)
  def setVelocityY(velocity: Float, speed: Float = 1)

  def getVelocity: (Float, Float)
}

class MobileEntityImpl(private val entityType: EntityType,
                       private var entityBody: EntityBody,
                       private val size: (Float, Float),
                       private var stats: Map[Statistic, Float] = Map()) extends EntityImpl(entityType, entityBody, size) with MobileEntity {

  private var facingRight: Boolean = true

  protected var movementStrategy: MovementStrategy = new DoNotMove()

  override def update(): Unit = {

  }

  override def setMovementStrategy(strategy: MovementStrategy): Unit = this.movementStrategy = strategy

  override def move(): Unit = {
    this.movementStrategy.apply()
  }

  override def stopMovement(): Unit = if(this.movementStrategy != null) this.movementStrategy.stopMovement()

  override def setFacing(right: Boolean): Unit = this.facingRight = right

  override def isFacingRight: Boolean = this.facingRight

  override def getStatistics: Map[Statistic, Float] = stats

  override def alterStatistics(statistic: Statistic, alteration: Float): Unit = {
    if(stats.contains(statistic)) {
      val newValue = stats(statistic) + alteration
      this.stats += (statistic -> newValue)

      statistic match {
        case Statistic.MovementSpeed => this.movementStrategy.alterSpeed(alteration)
        case _ =>
      }
    }
  }

  override def getStatistic(statistic:Statistic): Option[Float] = {
    if (this.stats.contains(statistic))
      Option.apply(this.stats(statistic))
    else
      Option.empty
  }

  override def setVelocity(velocity: (Float, Float), speed: Float = 1): Unit =
    this.getBody.setLinearVelocity(velocity * speed)

  override def setVelocityX(velocity: Float, speed: Float = 1): Unit =
    this.getBody.setLinearVelocity(velocity * speed, this.getBody.getLinearVelocity.y)

  override def setVelocityY(velocity: Float, speed: Float = 1): Unit =
    this.getBody.setLinearVelocity(this.getBody.getLinearVelocity.x, velocity * speed)

  override def getVelocity: (Float, Float) = (this.getBody.getLinearVelocity.x, this.getBody.getLinearVelocity.y)
}


class CircularMobileEntity(private val entityType: EntityType,
                           private var entityBody: EntityBody,
                           private val size: (Float, Float),
                           private val statistics:Map[Statistic, Float],
                           private val pivotBody: EntityBody) extends MobileEntityImpl(entityType, entityBody, size, statistics) {

  EntitiesFactoryImpl.createJoint(this.pivotBody.getBody, this.entityBody.getBody)
}

class AirSwordMobileEntity(private val entityType: EntityType,
                           private var entityBody: EntityBody,
                           private val size: (Float, Float),
                           private val statistics:Map[Statistic, Float] = Map()) extends MobileEntityImpl(entityType, entityBody, size, statistics) {

  override def destroyEntity(): Unit = {
    EntitiesFactoryImpl.destroyBody(this.getBody)
    EntitiesFactoryImpl.removeEntity(this)
  }
}

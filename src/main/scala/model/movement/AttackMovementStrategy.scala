package model.movement

import model.entities.{Entity, MobileEntity}
import model.helpers.ImplicitConversions.{RichFloat, tupleToVector2, vectorToTuple}
import model.helpers.WorldUtilities.computeDirectionToTarget
import utils.EnemiesConstants.WIZARD_ATTACK3_HOMING_DURATION
import utils.HeroConstants.ARROW_VELOCITY

case class ArrowMovementStrategy(private val entity: MobileEntity,
                                 private var speed: Float) extends MovementStrategy {

  if (entity.isFacingRight)
    this.entity.setVelocityX(ARROW_VELOCITY.PPM, speed)
  else
    this.entity.setVelocityX(-ARROW_VELOCITY.PPM, speed)

  override def stopMovement(): Unit = this.entity.getBody.setLinearVelocity(0,0)
}

case class FireBallMovementStrategy(private val sourceEntity: Entity,
                                    private val originPoint: (Float, Float),
                                    private val targetPoint: (Float, Float),
                                    private val speed: Float) extends MovementStrategy {
  this.sourceEntity.getBody.applyLinearImpulse(
    computeDirectionToTarget(this.sourceEntity.getBody.getWorldCenter, this.targetPoint,
      speed), this.sourceEntity.getBody.getWorldCenter, true)
  this.sourceEntity.getBody.setGravityScale(0)

  override def stopMovement(): Unit = this.sourceEntity.getBody.setLinearVelocity(0,0)
}

case class EnergyBallMovementStrategy(private val sourceEntity: MobileEntity,
                                      private val originPoint:(Float, Float),
                                      private val targetEntity: Entity,
                                      private val speed: Float) extends MovementStrategy {
  this.changeBulletTrajectory()
  this.sourceEntity.getBody.setGravityScale(0)

  val homingTimer: Long = System.currentTimeMillis()

  override def apply(): Unit = {
    if (System.currentTimeMillis() - this.homingTimer < WIZARD_ATTACK3_HOMING_DURATION)
      this.changeBulletTrajectory()
  }

  private def changeBulletTrajectory(): Unit = {
    this.sourceEntity.getBody.setLinearVelocity(
      computeDirectionToTarget(this.originPoint, this.targetEntity.getPosition, speed))
  }

  override def stopMovement(): Unit = this.sourceEntity.getBody.setLinearVelocity(0,0)
}

case class CircularMovementStrategy(private val entity: MobileEntity,
                                    private val angularVelocity: Float) extends MovementStrategy {

  override def apply(): Unit = this.entity.getBody.setAngularVelocity(angularVelocity)

  override def stopMovement(): Unit = {
    this.entity.getBody.setAngularVelocity(0)
    this.entity.getBody.setLinearVelocity(0,0)
  }
}
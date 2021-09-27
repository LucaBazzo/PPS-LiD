package model.movement

import com.badlogic.gdx.math.Vector2
import model.helpers.ImplicitConversions.{RichFloat, tupleToVector2}
import model.entities.{Entity, MobileEntity}
import utils.EnemiesConstants.WIZARD_ATTACK3_HOMING_DURATION
import utils.HeroConstants.ARROW_VELOCITY

class ArrowMovementStrategy(private val entity: MobileEntity,
                            private var speed: Float) extends DoNothingMovementStrategy {

  if (entity.isFacingRight)
    this.entity.setVelocityX(ARROW_VELOCITY.PPM, speed)
  else
    this.entity.setVelocityX(-ARROW_VELOCITY.PPM, speed)

  override def stopMovement(): Unit = this.entity.getBody.setLinearVelocity(0,0)
}

case class WeightlessProjectileTrajectory(private val sourceEntity: Entity,
                                          private val originPoint: (Float, Float),
                                          private val targetPoint: (Float, Float),
                                          private val speed: Float) extends MovementStrategyImpl {
  this.sourceEntity.getBody.applyLinearImpulse(
    new Vector2(this.targetPoint._1, this.targetPoint._2)
      .sub(new Vector2(this.originPoint._1, this.originPoint._2))
      .nor()
      .scl(speed),
    this.sourceEntity.getBody.getWorldCenter, true)
  this.sourceEntity.getBody.setGravityScale(0)

  override def stopMovement(): Unit = this.sourceEntity.getBody.setLinearVelocity(0,0)
}
case class HomingProjectileTrajectory(private val sourceEntity: MobileEntity,
                                      private val originPoint:(Float, Float),
                                      private val targetEntity: Entity,
                                      private val speed: Float) extends MovementStrategyImpl {
  this.changeBulletTrajectory()
  this.sourceEntity.getBody.setGravityScale(0)

  val homingTimer: Long = System.currentTimeMillis()

  override def apply(): Unit = {
    if (System.currentTimeMillis() - this.homingTimer < WIZARD_ATTACK3_HOMING_DURATION)
      this.changeBulletTrajectory()
  }

  private def changeBulletTrajectory(): Unit = {

    //    val direction =
    //      this.targetEntity.getPosition
    //        .sub(sourceEntity.getPosition)
    //        .nor()
    //        .scl(sourceEntity.getStatistic(Statistic.MovementSpeed).get)
    //    this.sourceEntity.setVelocity(direction)

    this.sourceEntity.getBody.setLinearVelocity(
      new Vector2(this.targetEntity.getPosition)
        .sub(new Vector2(this.originPoint._1, this.originPoint._2))
        .nor()
        .scl(this.speed))
  }

  override def stopMovement(): Unit = this.sourceEntity.getBody.setLinearVelocity(0,0)
}

case class CircularMovementStrategy(private val entity: MobileEntity,
                                    private val angularVelocity: Float) extends MovementStrategyImpl {

  override def apply(): Unit = this.entity.getBody.setAngularVelocity(angularVelocity)

  override def stopMovement(): Unit = {
    this.entity.getBody.setAngularVelocity(0)
    this.entity.getBody.setLinearVelocity(0,0)
  }
}
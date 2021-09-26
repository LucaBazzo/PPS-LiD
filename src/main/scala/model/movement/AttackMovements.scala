package model.movement

import com.badlogic.gdx.math.Vector2
import model.collisions.ImplicitConversions.RichFloat
import model.entities.Statistic.Statistic
import model.entities.{Entity, MobileEntity, Statistic}
import utils.HeroConstants.ARROW_VELOCITY

class ArrowMovementStrategy(private val entity: MobileEntity, private var speed: Float) extends DoNothingMovementStrategy {

  override def apply(): Unit = {
    if (entity.isFacingRight)
      this.entity.setVelocityX(ARROW_VELOCITY.PPM, speed)
    else
      this.entity.setVelocityX(-ARROW_VELOCITY.PPM, speed)
  }

  override def stopMovement(): Unit = this.entity.getBody.setLinearVelocity(0,0)
}

class ProjectileTrajectory(val owner: Entity,
                           val originPoint:(Float, Float),
                           val targetPoint:(Float, Float),
                           val stats: Map[Statistic, Float]) extends DoNothingMovementStrategy {

  owner.getBody.applyLinearImpulse(
    new Vector2(targetPoint._1, targetPoint._2).sub(new Vector2(originPoint._1, originPoint._2)).nor()
      .scl(stats(Statistic.Strength)),
    owner.getBody.getWorldCenter, true)

  override def apply(): Unit = {
    // this.owner.getBody.setTransform(this.owner.getBody.getWorldCenter, computeVectorAngle(this.owner.getBody.getLinearVelocity))
  }

  // private def computeVectorAngle(vector: Vector2):Float = Math.atan(vector.y / vector.x).toFloat
}

class WeightlessProjectileTrajectory(val owner: Entity,
                                     val originPoint:(Float, Float),
                                     val targetPoint:(Float, Float),
                                     val speed: Float) extends DoNothingMovementStrategy {
  owner.getBody.applyLinearImpulse(
    new Vector2(targetPoint._1, targetPoint._2).sub(new Vector2(originPoint._1, originPoint._2)).nor()
    .scl(speed),
  owner.getBody.getWorldCenter, true)

  owner.getBody.setGravityScale(0)

  override def apply(): Unit = { }
}

class CircularMovementStrategy(private val entity: MobileEntity, private val angularVelocity: Float) extends DoNothingMovementStrategy {

  override def apply(): Unit = this.entity.getBody.setAngularVelocity(angularVelocity)

  override def stopMovement(): Unit = {
    this.entity.getBody.setAngularVelocity(0)
    this.entity.getBody.setLinearVelocity(0,0)
  }
}

class HomingProjectileTrajectory(val sourceEntity: MobileEntity,
                                 val originPoint:(Float, Float),
                                 val targetPoint:(Float, Float),
                                 val speed: Float) extends DoNothingMovementStrategy {
  sourceEntity.getBody.applyLinearImpulse(
    new Vector2(targetPoint._1, targetPoint._2).sub(new Vector2(originPoint._1, originPoint._2)).nor()
      .scl(this.speed),
    sourceEntity.getBody.getWorldCenter, true)
  sourceEntity.getBody.setGravityScale(0)

  // TODO: implement homing effect
  override def apply(): Unit = {
  }
}
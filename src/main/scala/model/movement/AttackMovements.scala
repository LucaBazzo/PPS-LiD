package model.movement

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import model.entities.Statistic.Statistic
import model.entities.{Entity, MobileEntity, Statistic}

import scala.collection.mutable

class ProjectileTrajectory(val owner: Entity,
                           val originPoint:(Float, Float),
                           val targetPoint:(Float, Float),
                           val stats:mutable.Map[Statistic, Float]) extends MovementStrategy {

  owner.getBody.applyLinearImpulse(
    new Vector2(targetPoint._1, targetPoint._2).sub(new Vector2(originPoint._1, originPoint._2)).nor()
      .scl(stats(Statistic.Strength)),
    owner.getBody.getWorldCenter, true)

  this.owner.getBody.setTransform(this.owner.getBody.getWorldCenter, computeVectorAngle(this.owner.getBody.getLinearVelocity))

  override def apply(): Unit = {
    this.owner.getBody.setTransform(this.owner.getBody.getWorldCenter, computeVectorAngle(this.owner.getBody.getLinearVelocity))
  }

  private def computeVectorAngle(vector: Vector2):Float = Math.atan(vector.y / vector.x).toFloat
}

class WeightlessProjectileTrajectory(val entity: Entity, val targetPoint: Vector2, val world: World) extends MovementStrategy {
  // velocity of the projectile
  val force = 10
  entity.getBody.applyLinearImpulse(targetPoint.nor().scl(force), entity.getBody.getWorldCenter, true)
  entity.getBody.setGravityScale(0)

  override def apply(): Unit = { }
}

class CircularMovementStrategy(private val entity: MobileEntity, private val angularVelocity: Float) extends MovementStrategy {

  override def apply(): Unit = this.entity.getBody.setAngularVelocity(angularVelocity)

  override def stopMovement(): Unit = {
    this.entity.getBody.setAngularVelocity(0)
    this.entity.getBody.setLinearVelocity(0,0)
  }
}
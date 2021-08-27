package model.movement

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import model.entities.{Entity, MobileEntity}

class ProjectileTrajectory(val entity: Entity, val sourceEntity: Entity, val targetEntity: Entity) extends MovementStrategy {
  val force = 5
  entity.getBody.applyLinearImpulse(
    targetEntity.getBody.getPosition.sub(sourceEntity.getBody.getPosition).nor().scl(force),
    entity.getBody.getWorldCenter, true)

  override def apply(): Unit = { }
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
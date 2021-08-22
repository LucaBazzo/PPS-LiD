package model.movement

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import model.entities.Entity

class ProjectileTrajectory(val entity: Entity, val sourceEntity: Entity, val targetEntity: Entity) extends MovementStrategy {
  val force = 20
  entity.getBody.applyLinearImpulse(
    targetEntity.getBody.getPosition.sub(sourceEntity.getBody.getPosition).nor().scl(force),
    entity.getBody.getWorldCenter, true)

  override def move(): Unit = { }
}

class WeightlessProjectileTrajectory(val entity: Entity, val targetPoint: Vector2, val world: World) extends MovementStrategy {
  // velocity of the projectile
  val force = 20
  entity.getBody.applyLinearImpulse(targetPoint.nor().scl(force), entity.getBody.getWorldCenter, true)
  entity.getBody.setGravityScale(0)

  override def move(): Unit = { }
}

package model.movement

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import model.entities.Entity

class ProjectileTrajectory(val entity: Entity, val vector: Vector2) extends MovementStrategy {
  entity.getBody.applyLinearImpulse(vector, entity.getBody.getWorldCenter, true)

  override def move(): Unit = { }
}

class WeightlessProjectileTrajectory(val entity: Entity, val world: World, val vector: Vector2) extends MovementStrategy {
  entity.getBody.applyLinearImpulse(vector, entity.getBody.getWorldCenter, true)

  override def move(): Unit = {
    // prevents the entity from falling
    entity.getBody.applyLinearImpulse(world.getGravity, entity.getBody.getWorldCenter, true)
  }
}

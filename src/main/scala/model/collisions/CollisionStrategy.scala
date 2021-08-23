package model.collisions

import model.Level
import model.entities.Entity

trait CollisionStrategy {
  def apply(entity: Entity)
}

class CollisionStrategyImpl() extends CollisionStrategy {
  override def apply(entity: Entity): Unit = println("Collision Detected with " + entity.toString)
}

class DoNothingOnCollision() extends CollisionStrategy {
  override def apply(entity: Entity): Unit = {}
}

class ApplyDamage(private val sourceEntity:Entity, private val targetEntity:Entity) extends CollisionStrategy {
  override def apply(entity: Entity): Unit = {
    if (entity equals targetEntity) {
      println("attacking target")
    }
  }
}

class ApplyDamageAndDestroyEntity(private val sourceEntity:Entity, private val targetEntity:Entity, private val level:Level)
  extends ApplyDamage(sourceEntity, targetEntity) {

  override def apply(entity: Entity): Unit = {
    super.apply(entity)
    level.removeEntity(sourceEntity)
  }
}

package model.collisions

import model.entities.Entity

trait CollisionStrategy {

  def apply(entity: Entity)
}

class CollisionStrategyImpl extends CollisionStrategy {
  override def apply(entity: Entity): Unit = println("Collision Detected with " + entity.toString)
}


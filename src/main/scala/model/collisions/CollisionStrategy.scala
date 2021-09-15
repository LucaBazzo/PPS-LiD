package model.collisions

import model.entities.Entity


trait CollisionStrategy {
  def apply(entity: Entity): Unit
  def release(entity: Entity): Unit
}

class DoNothingOnCollision() extends CollisionStrategy {
  override def apply(entity: Entity): Unit = {}

  override def release(entity: Entity): Unit = {}
}

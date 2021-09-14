package model.collisions

import model.Level
import model.entities.{Entity, Hero}


trait CollisionStrategy {
  def apply(entity: Entity): Unit
  def release(entity: Entity): Unit
}

class DoNothingOnCollision() extends CollisionStrategy {
  override def apply(entity: Entity): Unit = {}

  override def release(entity: Entity): Unit = {}
}

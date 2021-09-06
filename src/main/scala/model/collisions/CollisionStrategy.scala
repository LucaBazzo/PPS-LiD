package model.collisions

import model.entities.Statistic.Statistic
import model.entities.{CircularMobileEntity, Entity, Hero, ImmobileEntity, Item, _}


trait CollisionStrategy {
  def apply(entity: Entity): Unit
  def release(entity: Entity): Unit
}

class DoNothingOnCollision() extends CollisionStrategy {
  override def apply(entity: Entity): Unit = {}

  override def release(entity: Entity): Unit = {}
}

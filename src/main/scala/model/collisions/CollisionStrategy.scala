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

class NewLevelOnCollision(level: Level) extends DoNothingOnCollision {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => this.level.newLevel()
    case _ =>
  }
}
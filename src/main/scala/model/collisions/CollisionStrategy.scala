package model.collisions

import model.Level
import model.entities.{Entity, Hero}


trait CollisionStrategy {
  def apply(): Unit

  def contact(entity: Entity): Unit
  def release(entity: Entity): Unit
}

abstract class CollisionStrategyImpl() extends CollisionStrategy {
  override def contact(entity: Entity): Unit = { }

  override def release(entity: Entity): Unit = { }

  override def apply(): Unit = { }
}

case class DoNothingCollisionStrategy() extends CollisionStrategyImpl {
}

case class NewLevelOnCollision(level: Level) extends CollisionStrategyImpl {
  override def contact(entity: Entity): Unit = {
    entity match {
      case h:Hero => this.level.newLevel()
    }
  }
}
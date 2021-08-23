package model.collisions

import model.entities.{Entity, HeroImpl, ItemImpl}

trait CollisionStrategy {

  def apply(entity: Entity)
}

class CollisionStrategyImpl extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case i:ItemImpl => println("Collect item: " + entity.toString)
    case _ => println("Collision Detected with" + entity.toString)
  }
}

class ItemCollisionStrategy extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case h:HeroImpl => println("Hero picked up item")
    case _ => println("____")
  }
}


package model.collisions

import model.entities.{Entity, Hero, HeroImpl, Item, ItemImpl}
import model.Level
import model.helpers.EntitiesFactoryImpl

trait CollisionStrategy {
  def apply(entity: Entity)
}

class CollisionStrategyImpl extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case i:Item =>  println("Collect item: " + i.getEnumVal)
    case _ => println("Collision Detected with" + entity.toString)
  }
}

class ItemCollisionStrategy(private val item: Item) extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case h:Hero => println("Hero picked up item")
                   val effect = item.collect()
                   println(effect._3 + "\n +" + item.getScore + " points")
                   h.alterStatistics(effect._1, effect._2)
    case _ => println("____")
  }
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

class ApplyDamageAndDestroyEntity(private val sourceEntity:Entity, private val targetEntity:Entity)
  extends ApplyDamage(sourceEntity, targetEntity) {

  override def apply(entity: Entity): Unit = {
    super.apply(entity)
    EntitiesFactoryImpl.destroyBody(sourceEntity.getBody)
    EntitiesFactoryImpl.removeEntity(sourceEntity)
  }
}

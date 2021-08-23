package model.collisions

import model.entities.{Entity, HeroImpl, ItemImpl}
import model.Level
import model.helpers.EntitiesFactoryImpl

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

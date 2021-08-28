package model.collisions

import model.entities._
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

class ApplyDamage(private val origin:Entity, private val target:Entity => Boolean)
  extends CollisionStrategy {

  override def apply(entity: Entity): Unit = {
    println("ASD", target(entity), entity)
    if (target(entity)) {
      entity.asInstanceOf[LivingEntity].sufferDamage(
        origin.asInstanceOf[LivingEntity].getStatistic(Statistic.Strength))
    }
  }
}

class ApplyDamageAndDestroyEntity(private val origin:Entity, private val target:Entity => Boolean)
  extends ApplyDamage(origin, target) {

  override def apply(entity: Entity): Unit = {
    super.apply(entity)
    EntitiesFactoryImpl.destroyBody(origin.getBody)
    EntitiesFactoryImpl.removeEntity(origin)
  }
}

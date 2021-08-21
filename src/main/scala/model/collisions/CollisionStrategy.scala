package model.collisions

import model.entities.{EnemyImpl, Entity, EntityImpl, HeroImpl, ImmobileEntity, LivingEntityImpl, MobileEntityImpl}

trait CollisionStrategy {
  def apply(entity: Entity)
}

class CollisionStrategyImpl() extends CollisionStrategy {
  override def apply(entity: Entity): Unit = println("Collision Detected with " + entity.toString)
}

class DoNothingOnCollision() extends CollisionStrategy {
  override def apply(entity: Entity): Unit = {}
}

class ApplyDamageToHero(private val owner:Entity) extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case HeroImpl(_, _) => {
      println("Entity colliding with hero")
    }
    case _ => {}
  }
}
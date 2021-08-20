package model.collisions

import model.entities.{EnemyImpl, Entity, EntityImpl, HeroImpl, ImmobileEntity, LivingEntityImpl, MobileEntityImpl}

trait CollisionStrategy {
  def apply(entity: Entity)
}

class CollisionStrategyImpl() extends CollisionStrategy {
  override def apply(entity: Entity): Unit = println("Collision Detected with " + entity.toString)
}

class ApplyDamageToHero(private val owner:EnemyImpl) extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case HeroImpl(_, _) => {
      println("Entity colliding with hero")
      entity.asInstanceOf[HeroImpl].sufferDamage(this.owner.attackDamage)
    }
    case _ => {}
  }
}
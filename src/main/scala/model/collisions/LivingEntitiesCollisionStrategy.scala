package model.collisions

import model.entities.Statistic.Statistic
import model.entities.{Entity, Item, LivingEntity, Statistic}

class FeetCollisionStrategy extends DoNothingOnCollision {
  override def apply(entity: Entity): Unit = entity match {
    case _ => println("Foot Collision Detected with" + entity.toString)
  }

  override def release(entity: Entity): Unit = entity match {
    case _ => println("Foot Release Detected with" + entity.toString)
  }
}

class CollisionStrategyImpl extends DoNothingOnCollision {
  override def apply(entity: Entity): Unit = entity match {
    case i:Item =>  println("Collect item: " + i.getEnumVal)
    case _ => println("Collision Detected with" + entity.toString)
  }
}

class ApplyDamage(private val target: Entity => Boolean,
                  private val stats: Map[Statistic, Float])
  extends DoNothingOnCollision {

  override def apply(entity: Entity): Unit = {
    if (target(entity)) {
      println("ENTITY " + entity + " suffer damage")
      entity.asInstanceOf[LivingEntity].sufferDamage(stats(Statistic.Strength))
    }
  }
}

class ApplyDamageAndDestroyEntity(private val sourceEntity: Entity,
                                  private val target: Entity => Boolean,
                                  private val stats: Map[Statistic, Float])
  extends ApplyDamage(target, stats) {

  override def apply(entity: Entity): Unit = {
    super.apply(entity)

    if ((entity.getBody.getFixtureList.toArray().head.getFilterData.maskBits
      & this.sourceEntity.getBody.getFixtureList.toArray().head.getFilterData.categoryBits) != 0) {
      this.sourceEntity.destroyEntity()
    }
  }
}

//class ApplyDamageAndDestroyEntityGracefully(private val sourceEntity: Entity,
//                                  private val target: Entity => Boolean,
//                                  private val stats: Map[Statistic, Float])
//  extends ApplyDamage(target, stats) {
//
//  override def apply(entity: Entity): Unit = {
//    if (EntitiesUtilities.canEntitiesCollide(sourceEntity, entity)) {
//      this.sourceEntity.setState(State.Dying)
//    }
//  }
//}
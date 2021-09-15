package model.collisions

import model.entities.Statistic.Statistic
import model.entities.{Entity, LivingEntity, Statistic}

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
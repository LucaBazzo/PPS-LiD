package model.collisions

import model.entities.Statistic.Statistic
import model.entities.{Entity, LivingEntity, Statistic}

case class ApplyDamage(private val target: Entity => Boolean,
                  private val stats: Map[Statistic, Float])
  extends CollisionStrategyImpl {

  override def contact(entity: Entity): Unit = {
    if (target(entity)) {
      println("ENTITY " + entity + " suffer damage")
      entity.asInstanceOf[LivingEntity].sufferDamage(stats(Statistic.Strength))
    }
  }
}

case class ApplyDamageAndDestroyEntity(private val sourceEntity: Entity,
                                  private val target: Entity => Boolean,
                                  private val stats: Map[Statistic, Float])
  extends CollisionStrategyImpl() {

  override def contact(entity: Entity): Unit = {
    super.contact(entity)
    if (target(entity)) {
      entity.asInstanceOf[LivingEntity].sufferDamage(stats(Statistic.Strength))
    }

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
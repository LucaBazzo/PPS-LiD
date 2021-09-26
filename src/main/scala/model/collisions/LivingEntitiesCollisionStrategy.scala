package model.collisions

import model.entities.Statistic.Statistic
import model.entities.{Entity, LivingEntity, State, Statistic}
import model.helpers.WorldUtilities.canBodiesCollide
import ImplicitConversions.entityToBody

case class ApplyDamage(private val target: Entity => Boolean,
                       private val stats: Map[Statistic, Float])
  extends CollisionStrategyImpl {

  override def contact(entity: Entity): Unit = {
    if (target(entity))
      entity.asInstanceOf[LivingEntity].sufferDamage(stats(Statistic.Strength))
  }
}

case class ApplyDamageAndDestroyEntity(private val sourceEntity: Entity,
                                       private val target: Entity => Boolean,
                                       private val stats: Map[Statistic, Float])
  extends CollisionStrategyImpl() {

  override def contact(entity: Entity): Unit = {
    if (target(entity))
      entity.asInstanceOf[LivingEntity].sufferDamage(stats(Statistic.Strength))

    if (canBodiesCollide(sourceEntity, entity))
      this.sourceEntity.destroyEntity()
  }
}

case class ApplyDamageAndKillEntity(private val sourceEntity: Entity,
                               private val target: Entity => Boolean,
                               private val stats: Map[Statistic, Float])
  extends CollisionStrategyImpl() {

  override def contact(entity: Entity): Unit = {
    if (target(entity))
      entity.asInstanceOf[LivingEntity].sufferDamage(stats(Statistic.Strength))

    this.sourceEntity.setState(State.Dying)
  }
}
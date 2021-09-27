package model.collisions

import model.collisions.ImplicitConversions.entityToBody
import model.entities.Statistic.Statistic
import model.entities.{Entity, LivingEntity, State, Statistic}
import model.helpers.WorldUtilities.canBodiesCollide

abstract class AbstractDamageCollisionStrategy(protected val target: Entity => Boolean,
                                               protected val stats: Map[Statistic, Float])
  extends CollisionStrategy {

  override def contact(entity: Entity): Unit = {
    if (target(entity))
      entity.asInstanceOf[LivingEntity].sufferDamage(stats(Statistic.Strength))
  }
}

case class ApplyDamageCollisionStrategy(override protected val target: Entity => Boolean,
                                        override protected val stats: Map[Statistic, Float])
  extends AbstractDamageCollisionStrategy(target, stats) {

}

case class ApplyDamageAndDestroyCollisionStrategy(protected val sourceEntity: Entity,
                                                  override protected val target: Entity => Boolean,
                                                  override protected val stats: Map[Statistic, Float])
  extends AbstractDamageCollisionStrategy(target, stats) {

  override def contact(entity: Entity): Unit = {
    super.contact(entity)
    if (canBodiesCollide(sourceEntity, entity))
      this.sourceEntity.destroyEntity()
  }
}

case class ApplyDamageAndKillCollisionStrategy(protected val sourceEntity: Entity,
                                               override protected val target: Entity => Boolean,
                                               override protected val stats: Map[Statistic, Float])
  extends AbstractDamageCollisionStrategy(target, stats) {

  override def contact(entity: Entity): Unit = {
    super.contact(entity)
    this.sourceEntity.setState(State.Dying)
  }
}
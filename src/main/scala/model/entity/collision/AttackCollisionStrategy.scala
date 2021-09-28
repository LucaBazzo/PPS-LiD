package model.entity.collision

import model.helpers.ImplicitConversions.entityToBody
import model.entity.Statistic.Statistic
import model.entity.{Entity, LivingEntity, State, Statistic}
import model.helpers.WorldUtilities.canBodiesCollide

abstract class AbstractAttackCollisionStrategy(protected val target: Entity => Boolean,
                                               protected val stats: Map[Statistic, Float])
  extends CollisionStrategy {

  override def contact(entity: Entity): Unit = {
    if (target(entity))
      entity.asInstanceOf[LivingEntity].sufferDamage(stats(Statistic.Strength))
  }
}

case class AttackCollisionStrategy(override protected val target: Entity => Boolean,
                                   override protected val stats: Map[Statistic, Float])
  extends AbstractAttackCollisionStrategy(target, stats) {

}

case class ArrowCollisionStrategy(protected val sourceEntity: Entity,
                                  override protected val target: Entity => Boolean,
                                  override protected val stats: Map[Statistic, Float])
  extends AbstractAttackCollisionStrategy(target, stats) {

  override def contact(entity: Entity): Unit = {
    super.contact(entity)
    if (canBodiesCollide(sourceEntity, entity))
      this.sourceEntity.destroyEntity()
  }
}

case class BulletCollisionStrategy(protected val sourceEntity: Entity,
                                   override protected val target: Entity => Boolean,
                                   override protected val stats: Map[Statistic, Float])
  extends AbstractAttackCollisionStrategy(target, stats) {

  override def contact(entity: Entity): Unit = {
    super.contact(entity)
    this.sourceEntity.setState(State.Dying)
  }
}
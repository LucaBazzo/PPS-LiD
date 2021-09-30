package model.entity.collision

import com.badlogic.gdx.physics.box2d.World
import model.helpers.ImplicitConversions.{RichWorld, entityToBody}
import model.entity.{Entity, LivingEntity, State}
import model.helpers.EntitiesFactoryImpl

/** Abstract definition of attacks collision strategies. An attack is a
 * particular configuration of the MobileEntity interface, given a specific
 * collision behaviour (defined here) and a movement strategy.
 *
 * @param target a predicated filtering out unmanaged transitions
 * @param damage the damage to apply to the target entity
 */
abstract class AbstractAttackCollisionStrategy(protected val target: Entity => Boolean,
                                               protected val damage: Float)
  extends CollisionStrategy {

  override def contact(entity: Entity): Unit = {
    if (target(entity))
      entity.asInstanceOf[LivingEntity].sufferDamage(damage)
  }
}

case class AttackCollisionStrategy(override protected val target: Entity => Boolean,
                                   override protected val damage: Float)
  extends AbstractAttackCollisionStrategy(target, damage) { }

case class ArrowCollisionStrategy(protected val sourceEntity: Entity,
                                  override protected val target: Entity => Boolean,
                                  override protected val damage: Float)
  extends AbstractAttackCollisionStrategy(target, damage) {

  val world: World = EntitiesFactoryImpl.getEntitiesContainerMonitor.getWorld.get

  override def contact(entity: Entity): Unit = {
    super.contact(entity)
    if (world.canBodiesCollide(sourceEntity, entity))
      this.sourceEntity.destroyEntity()
  }
}

case class BulletCollisionStrategy(protected val sourceEntity: Entity,
                                   override protected val target: Entity => Boolean,
                                   override protected val damage: Float)
  extends AbstractAttackCollisionStrategy(target, damage) {

  override def contact(entity: Entity): Unit = {
    super.contact(entity)
    this.sourceEntity.setState(State.Dying)
  }
}

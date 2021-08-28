package model.attack

import com.badlogic.gdx.math.Vector2
import model.Level
import model.collisions.ApplyDamage
import model.collisions.ImplicitConversions.RichInt
import model.entities.Statistic.Statistic
import model.entities._
import model.helpers.EntitiesFactoryImpl
import model.helpers.EntitiesFactoryImpl.{createEnemyProjectile, createEnemySwordAttack}
import model.helpers.WorldUtilities.{checkBodyIsVisible, getBodiesDistance, isTargetOnTheRight}
import model.movement.ProjectileTrajectory

import scala.collection.mutable


class DoNotAttack() extends AttackStrategy {
  override def apply(): Unit = { }

  override def isAttackFinished: Boolean = true

  override def stopAttack(): Unit = { }

}

class ContactAttack(owner: LivingEntity, val stats:mutable.Map[Statistic, Float], val target:Entity => Boolean)
  extends AttackStrategy {

  // the attack itself is created once and "attached" to the owner body
  val entity:MobileEntity = EntitiesFactoryImpl.createEnemyContactAttack(owner.getSize._1, owner)
  entity.setCollisionStrategy(new ApplyDamage(owner, target))

  override def apply(): Unit = { }

  override def isAttackFinished: Boolean = false

  override def stopAttack(): Unit = {

  }
}

class MeleeAttack(owner: LivingEntity, level:Level, val stats:mutable.Map[Statistic, Float], val target:Entity => Boolean)
  extends AttackStrategy {

  protected val targetEntity:LivingEntity = level.getEntity(target).asInstanceOf[LivingEntity]

  protected val maxDistance:Float = 40.PPM
  protected val visibilityMaxHorizontalAngle:Int = 30
  protected val attackFrequency:Int = 2000
  protected val attackDuration:Int = 1200

  protected var lastAttackTime:Long = 0

  protected var attackInstance: Option[MobileEntity] = Option.empty

  private def canAttack: Boolean =  {
    System.currentTimeMillis() - this.lastAttackTime > this.attackFrequency &&
      checkBodyIsVisible(level.getWorld, owner.getBody, targetEntity.getBody, this.visibilityMaxHorizontalAngle) &&
      getBodiesDistance(owner.getBody, targetEntity.getBody) <= this.maxDistance
  }

  override def apply(): Unit = {
    if (canAttack) {
      this.lastAttackTime = System.currentTimeMillis()
      this.attackInstance = Option(spawnAttack())
    }

    // delete the attack entity
    if (attackInstance.isDefined && System.currentTimeMillis() - this.lastAttackTime >= this.attackDuration) {
      EntitiesFactoryImpl.destroyBody(this.attackInstance.get.getBody)
      EntitiesFactoryImpl.removeEntity(this.attackInstance.get)
      this.attackInstance = Option.empty
    }
  }

  override def isAttackFinished: Boolean = this.attackInstance.isEmpty

  private def spawnAttack(): MobileEntity = {
    val spawnCoordinates: Vector2 = owner.getBody.getWorldCenter.add(
      if (isTargetOnTheRight(owner.getBody, targetEntity.getBody))
        owner.getSize._1 else -owner.getSize._1, 0)

    val entity:MobileEntity = createEnemySwordAttack((20, 20), (spawnCoordinates.x, spawnCoordinates.y), owner)
    entity.getBody.setBullet(true)
    entity
  }
}

class RangedAttack(owner: LivingEntity, level:Level, val stats:mutable.Map[Statistic, Float], val target:Entity => Boolean)
  extends AttackStrategy {

  protected val targetEntity:LivingEntity = level.getEntity(target).asInstanceOf[LivingEntity]

  protected val maxDistance:Float = 50.PPM
  protected val visibilityMaxHorizontalAngle:Int = 10
  protected val attackFrequency:Int = 2000
  protected val attackDuration:Int = 1500

  protected var lastAttackTime:Long = 0

  protected var activeAttacks: mutable.Map[MobileEntity, Long] = mutable.Map.empty

  private def canAttack: Boolean =  {
    System.currentTimeMillis() - this.lastAttackTime > this.attackFrequency &&
      checkBodyIsVisible(level.getWorld, owner.getBody, targetEntity.getBody, this.visibilityMaxHorizontalAngle) &&
      getBodiesDistance(owner.getBody, targetEntity.getBody) <= this.maxDistance
  }

  override def apply(): Unit = {
    if (canAttack) {
      this.lastAttackTime = System.currentTimeMillis()
      this.activeAttacks += (spawnAttack() -> this.lastAttackTime)
    }

    // remove attack entities
    val currentTime:Long = System.currentTimeMillis()
    this.activeAttacks.find(item => currentTime - item._2 >= this.attackDuration).map(item => item._1).foreach(e => {
      EntitiesFactoryImpl.destroyBody(e.getBody)
      EntitiesFactoryImpl.removeEntity(e)
    })
    this.activeAttacks = this.activeAttacks.filter(item => currentTime - item._2 < this.attackDuration)
  }

  override def isAttackFinished: Boolean = System.currentTimeMillis() - this.lastAttackTime >= this.attackDuration

  private def spawnAttack(): MobileEntity = {
    val spawnCoordinates = owner.getBody.getWorldCenter.add(
      if (isTargetOnTheRight(owner.getBody, targetEntity.getBody))
        owner.getSize._1 else -owner.getSize._1, 0)

    val entity:MobileEntity = createEnemyProjectile((5, 5), (spawnCoordinates.x, spawnCoordinates.y), owner)

    entity.setMovementStrategy(new ProjectileTrajectory(entity, owner, targetEntity))
    entity.setCollisionStrategy(new ApplyDamage(entity, (e:Entity) => e.isInstanceOf[Hero]))
//    entity.setCollisionStrategy(new ApplyDamageAndDestroyEntity(entity, targetEntity))
    entity.getBody.setBullet(true)
    entity
  }
}

//class MagicMissleAttack(enemyEntity: Entity, heroEntity:Entity, world:World, level:Level) extends AttackStrategy {
//  private val maxDistance = 10
//
//  val attackFrequency = 1000
//  var lastAttackTime = 0l
//
//  override def attack(): Unit = {
//    lastAttackTime = System.currentTimeMillis()
//    val attackEntity:Attack = entitiesFactory.createProjectile(
//      enemyEntity.getBody.getWorldCenter,
//      heroEntity.getBody.getWorldCenter, enemyEntity)
//    level.addEntity(attackEntity)
//  }
//
//}
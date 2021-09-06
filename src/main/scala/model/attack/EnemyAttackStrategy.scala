package model.attack

import model.entities.{Entity, MobileEntity}
import com.badlogic.gdx.math.Vector2
import model.Level
import model.entities.Statistic.Statistic
import model.entities._
import model.helpers.EntitiesFactoryImpl
import model.helpers.EntitiesFactoryImpl.{createEnemyProjectileAttack, createMeleeSwordAttack}
import model.helpers.WorldUtilities.{checkBodyIsVisible, getBodiesDistance, isTargetOnTheRight}



class DoNotAttack() extends AttackStrategy {
  override def apply(): Unit = { }

  override def isAttackFinished: Boolean = true

  override def stopAttack(): Unit = { }

}

class ContactAttack(val owner: LivingEntity,
                    val stats: Map[Statistic, Float],
                    val target:Entity => Boolean)
  extends AttackStrategy {

  // the attack itself is created once and "attached" to the owner body
  val entity:MobileEntity = EntitiesFactoryImpl.createEnemyContactAttack(owner.getSize._1, owner)

  override def apply(): Unit = { }

  override def isAttackFinished: Boolean = true

  override def stopAttack(): Unit = {

  }
}

class MeleeAttack(val owner: LivingEntity,
                  val level: Level,
                  val stats: Map[Statistic, Float],
                  val target: Entity => Boolean)
  extends AttackStrategy {

  protected val targetEntity:LivingEntity = level.getEntity(target).asInstanceOf[LivingEntity]
  protected val maxDistance:Float = stats(Statistic.HorizontalVisionDistance)
  protected val visibilityMaxHorizontalAngle:Float = stats(Statistic.HorizontalVisionAngle)
  protected val attackFrequency:Float = stats(Statistic.AttackFrequency)
  protected val attackDuration:Float = stats(Statistic.AttackDuration)

  protected var lastAttackTime:Long = 0

  override def apply(): Unit = {
    if (canAttack && isAttackFinished) {
      this.lastAttackTime = System.currentTimeMillis()
      spawnAttack()
    }
  }

  override def isAttackFinished: Boolean = System.currentTimeMillis() - this.lastAttackTime > this.attackDuration

  private def canAttack: Boolean =  {
    System.currentTimeMillis() - this.lastAttackTime > this.attackFrequency &&
      checkBodyIsVisible(level.getWorld, owner.getBody, targetEntity.getBody, this.visibilityMaxHorizontalAngle) &&
      getBodiesDistance(owner.getBody, targetEntity.getBody) <= this.maxDistance
  }

  private def spawnAttack(): MobileEntity = {
    val spawnCoordinates: Vector2 = owner.getBody.getWorldCenter.add(
      if (isTargetOnTheRight(owner.getBody, targetEntity.getBody))
        owner.getSize._1 else -owner.getSize._1, 0)

    val entity:MobileEntity = createMeleeSwordAttack(EntityType.Mobile, (15, 15), (spawnCoordinates.x, spawnCoordinates.y), owner)
    entity.getBody.setBullet(true)
    entity
  }
}

class RangedAttack(val owner: LivingEntity,
                   val level:Level,
                   val stats: Map[Statistic, Float],
                   val target:Entity => Boolean)
  extends AttackStrategy {

  protected val targetEntity:Entity = this.level.getEntity(target)
  protected val maxDistance:Float = stats(Statistic.HorizontalVisionDistance)
  protected val visibilityMaxHorizontalAngle:Float = stats(Statistic.HorizontalVisionAngle)
  protected val attackFrequency:Float = stats(Statistic.AttackFrequency)
  protected val attackDuration:Float = stats(Statistic.AttackDuration)

  protected var lastAttackTime:Long = 0

  override def apply(): Unit = {
    if (canAttack && isAttackFinished) {
      this.lastAttackTime = System.currentTimeMillis()
      spawnAttack()
    }
  }

  override def isAttackFinished: Boolean =
    System.currentTimeMillis() - this.lastAttackTime >= this.attackDuration

  private def canAttack: Boolean =  {
    System.currentTimeMillis() - this.lastAttackTime > this.attackFrequency &&
      checkBodyIsVisible(this.level.getWorld, owner.getBody, targetEntity.getBody,
        this.visibilityMaxHorizontalAngle) &&
      getBodiesDistance(this.owner.getBody, targetEntity.getBody) <= this.maxDistance
  }

  private def spawnAttack(): Attack = {
    val spawnCoordinates = owner.getBody.getWorldCenter.add(
      if (isTargetOnTheRight(owner.getBody, targetEntity.getBody))
        owner.getSize._1 else -owner.getSize._1, 0)
    val entity:Attack = createEnemyProjectileAttack(EntityType.AttackFireBall, (5, 5),
      (spawnCoordinates.x, spawnCoordinates.y), targetEntity.getPosition, owner)
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
package model.attack

import model.Level
import model.entities.Statistic.Statistic
import model.entities.{Entity, _}
import model.helpers.EntitiesFactoryImpl.{createFireballAttack, createMeleeAttack}
import model.helpers.EntitiesUtilities._



class DoNotAttack() extends AttackStrategy {
  override def apply(): Unit = { }

  override def isAttackFinished: Boolean = true

  override def stopAttack(): Unit = { }

}

class MeleeAttack(val sourceEntity: LivingEntity,
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
      createMeleeAttack(sourceEntity, targetEntity)
    }
  }

  override def isAttackFinished: Boolean = System.currentTimeMillis() - this.lastAttackTime > this.attackDuration

  private def canAttack: Boolean =  {
    System.currentTimeMillis() - this.lastAttackTime > this.attackFrequency &&
      isEntityVisible(sourceEntity, targetEntity, this.visibilityMaxHorizontalAngle) &&
      getEntitiesDistance(sourceEntity, targetEntity) <= this.maxDistance
  }
}

class RangedAttack(val sourceEntity: LivingEntity,
                   val level:Level,
                   val stats: Map[Statistic, Float],
                   val target:Entity => Boolean)
  extends AttackStrategy {

  protected val targetEntity: Entity = this.level.getEntity(target)
  protected val maxDistance: Float = stats(Statistic.HorizontalVisionDistance)
  protected val visibilityMaxHorizontalAngle: Float = stats(Statistic.HorizontalVisionAngle)
  protected val attackFrequency: Float = stats(Statistic.AttackFrequency)
  protected val attackDuration: Float = stats(Statistic.AttackDuration)

  protected var lastAttackTime:Long = 0

  override def apply(): Unit = {
    if (canAttack && isAttackFinished) {
      this.lastAttackTime = System.currentTimeMillis()
      createFireballAttack(this.sourceEntity, this.targetEntity)
    }
  }

  override def isAttackFinished: Boolean =
    System.currentTimeMillis() - this.lastAttackTime >= this.attackDuration

  private def canAttack: Boolean =  {
    System.currentTimeMillis() - this.lastAttackTime > this.attackFrequency &&
      isEntityVisible(this.sourceEntity, this.targetEntity, this.visibilityMaxHorizontalAngle) &&
      getEntitiesDistance(this.sourceEntity, this.targetEntity) <= this.maxDistance
  }
}
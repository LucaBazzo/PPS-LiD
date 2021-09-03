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

abstract class AttackStrategyImpl(protected val sourceEntity: LivingEntity,
                                  protected val targetEntity: Entity)
  extends AttackStrategy {

  protected val stats: Map[Statistic, Float] = this.sourceEntity.getStatistics
  protected val attackFrequency: Float = this.stats(Statistic.AttackFrequency)
  protected val attackDuration: Float = this.stats(Statistic.AttackDuration)
  protected val visionAngle: Float = this.stats(Statistic.VisionAngle)
  protected val visionDistance: Float = this.stats(Statistic.VisionDistance)

  protected var lastAttackTime:Long = 0

  override def apply(): Unit = {
    if (this.canAttack) {
      this.lastAttackTime = System.currentTimeMillis()
      this.sourceEntity.setState(State.Attack01)
      spawnAttack()
    }
  }

  override def isAttackFinished: Boolean =
    System.currentTimeMillis() - this.lastAttackTime > this.attackDuration

  protected def canAttack: Boolean = this.isAttackFinished &&
    System.currentTimeMillis() - this.lastAttackTime > this.attackFrequency &&
    isEntityVisible(this.sourceEntity, this.targetEntity, this.visionAngle) &&
    getEntitiesDistance(this.sourceEntity, this.targetEntity) <= this.visionDistance

  protected def spawnAttack(): Unit
}

class MeleeAttack(override protected val sourceEntity: LivingEntity,
                  override protected val targetEntity: Entity)
  extends AttackStrategyImpl(sourceEntity, targetEntity) {

  protected var attackInstance: Option[MobileEntity] = Option.empty

  override def apply():Unit = {
    if (this.isAttackFinished && this.attackInstance.isDefined) {
      this.stopAttack()
      this.sourceEntity.setState(State.Standing)
    }

    super.apply()
  }

  override def stopAttack(): Unit = {
    if (this.attackInstance.isDefined) {
      this.attackInstance.get.destroyEntity()
      this.attackInstance = Option.empty
    }
  }

  override protected def spawnAttack(): Unit = {
    this.attackInstance = Option(createMeleeAttack(this.sourceEntity, this.targetEntity))
  }
}

class RangedAttack(override protected val sourceEntity: LivingEntity,
                   override protected val targetEntity: Entity)
  extends AttackStrategyImpl(sourceEntity, targetEntity) {

  override protected def spawnAttack(): Unit = createFireballAttack(this.sourceEntity, this.targetEntity)
}

class ContactAttack(protected val sourceEntity: LivingEntity,
                    protected val level: Level,
                    protected val stats: Map[Statistic, Float],
                    protected val target: Entity => Boolean) extends AttackStrategy {



  override def apply(): Unit = {

  }

  override def isAttackFinished: Boolean = ???
}
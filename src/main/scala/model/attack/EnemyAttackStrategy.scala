package model.attack

import model.entities.Statistic.Statistic
import model.entities.{Entity, _}
import model.helpers.EntitiesFactoryImpl.{createEnergyBallAttack, createFireballAttack, createMeleeAttack}
import model.helpers.EntitiesUtilities._
import utils.EnemiesConstants
import utils.EnemiesConstants.{SKELETON_ATTACK_OFFSET, SKELETON_ATTACK_SIZE, SLIME_ATTACK_OFFSET, SLIME_ATTACK_SIZE, WIZARD_BOSS_ATTACK1_OFFSET, WIZARD_BOSS_ATTACK1_SIZE, WIZARD_BOSS_ATTACK2_OFFSET, WIZARD_BOSS_ATTACK2_SIZE}

// TODO: rimpiazzare con AttackStrategy.DoNothingAttackStrategy
class DoNotAttack() extends AttackStrategy {
  override def apply(): Unit = { }

  override def isAttackFinished: Boolean = true

  override def stopAttack(): Unit = { }
}

abstract class EnemyAttackStrategy(protected val sourceEntity: LivingEntity,
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

abstract class MeleeAttackStrategy(override protected val sourceEntity: LivingEntity,
                                   override protected val targetEntity: Entity)
  extends EnemyAttackStrategy(sourceEntity, targetEntity) {

  protected var attackInstance: Option[MobileEntity] = Option.empty
  protected var attackTimer: Long = 0

  override def apply():Unit = {
    // remove attack box
    if (this.isAttackFinished && this.attackInstance.isDefined) {
      this.stopAttack()
      this.sourceEntity.setState(State.Standing)
    }

    // activate the attack box to match the displayed animation
    if (!this.isAttackFinished) {
      val attackProgress: Long = System.currentTimeMillis() - this.attackTimer
      val isAttackActive: Boolean = this.attackInstance.get.getBody.isActive

      this.updateAttack(attackProgress, isAttackActive)
    }

    if (this.canAttack)
      this.attackTimer = System.currentTimeMillis()

    super.apply()
  }

  override def stopAttack(): Unit = {
    if (this.attackInstance.isDefined) {
      this.attackInstance.get.destroyEntity()
      this.attackInstance = Option.empty
      this.attackTimer = 0
    }
  }

  protected def updateAttack(attackProgress:Long, isAttackActive:Boolean): Unit
}

class SkeletonAttack(override protected val sourceEntity: LivingEntity,
                     override protected val targetEntity: Entity)
  extends MeleeAttackStrategy(sourceEntity, targetEntity) {

  override protected def updateAttack(attackProgress:Long, isAttackActive:Boolean):Unit = {
    if (attackProgress > 600 && attackProgress < 1000 && !isAttackActive) {
      this.attackInstance.get.getBody.setActive(true)
    }
    if (attackProgress <= 600 && attackProgress > 1000 && isAttackActive) {
      this.attackInstance.get.getBody.setActive(false)
    }
  }

  override protected def spawnAttack(): Unit = {
    this.sourceEntity.setState(State.Attack01)
    this.attackInstance = Option(createMeleeAttack(this.sourceEntity, this.targetEntity,
      size = SKELETON_ATTACK_SIZE, offset = SKELETON_ATTACK_OFFSET))
    this.attackInstance.get.getBody.setActive(false)
  }
}

class SlimeAttack(override protected val sourceEntity: LivingEntity,
                  override protected val targetEntity: Entity)
  extends MeleeAttackStrategy(sourceEntity, targetEntity) {

  override protected def updateAttack(attackProgress:Long, isAttackActive:Boolean):Unit = {
    if (attackProgress > 600 && attackProgress < 1000 && !isAttackActive) {
      this.attackInstance.get.getBody.setActive(true)
    }
    if (attackProgress <= 600 && attackProgress > 1000 && isAttackActive) {
      this.attackInstance.get.getBody.setActive(false)
    }
  }

  override protected def spawnAttack(): Unit = {
    this.sourceEntity.setState(State.Attack01)
    this.attackInstance = Option(createMeleeAttack(this.sourceEntity, this.targetEntity,
      size = SLIME_ATTACK_SIZE, offset = SLIME_ATTACK_OFFSET))
    this.attackInstance.get.getBody.setActive(false)
  }
}

class WizardFirstAttack(override protected val sourceEntity: LivingEntity,
                        override protected val targetEntity: Entity)
  extends MeleeAttackStrategy(sourceEntity, targetEntity) {

  override protected def updateAttack(attackProgress:Long, isAttackActive:Boolean):Unit = {
    if (attackProgress > 600 && attackProgress < 1000 && !isAttackActive) {
      this.attackInstance.get.getBody.setActive(true)
    }
    if (attackProgress <= 600 && attackProgress > 1000 && isAttackActive) {
      this.attackInstance.get.getBody.setActive(false)
    }
  }

  override protected def spawnAttack(): Unit = {
    this.sourceEntity.setState(State.Attack01)
    this.attackInstance = Option(createMeleeAttack(this.sourceEntity, this.targetEntity,
      size = WIZARD_BOSS_ATTACK1_SIZE, offset = WIZARD_BOSS_ATTACK1_OFFSET))
    this.attackInstance.get.getBody.setActive(false)
  }
}

class WizardSecondAttack(override protected val sourceEntity: LivingEntity,
                         override protected val targetEntity: Entity)
  extends MeleeAttackStrategy(sourceEntity, targetEntity) {

  override protected def updateAttack(attackProgress:Long, isAttackActive:Boolean):Unit = {
    if (attackProgress > 600 && attackProgress < 1000 && !isAttackActive) {
      this.attackInstance.get.getBody.setActive(true)
    }
    if (attackProgress <= 600 && attackProgress > 1000 && isAttackActive) {
      this.attackInstance.get.getBody.setActive(false)
    }
  }

  override protected def spawnAttack(): Unit = {
    this.sourceEntity.setState(State.Attack02)
    this.attackInstance = Option(createMeleeAttack(this.sourceEntity, this.targetEntity,
      size = WIZARD_BOSS_ATTACK2_SIZE, offset = WIZARD_BOSS_ATTACK2_OFFSET))
    this.attackInstance.get.getBody.setActive(false)
  }
}

class WizardEnergyBallAttack(override protected val sourceEntity: LivingEntity,
                             override protected val targetEntity: Entity)
  extends EnemyAttackStrategy(sourceEntity, targetEntity) {

  override protected def spawnAttack(): Unit = {
    this.sourceEntity.setState(State.Standing)
    createEnergyBallAttack(this.sourceEntity, this.targetEntity,
      EnemiesConstants.WIZARD_BOSS_ATTACK3_SIZE,
      EnemiesConstants.WIZARD_BOSS_ATTACK3_OFFSET)
  }

  override def stopAttack(): Unit = { }
}


class WormFireballAttack(override protected val sourceEntity: LivingEntity,
                         override protected val targetEntity: Entity)
  extends EnemyAttackStrategy(sourceEntity, targetEntity) {

  override protected def spawnAttack(): Unit = {
    this.sourceEntity.setState(State.Attack01)
    createFireballAttack(this.sourceEntity, this.targetEntity,
      EnemiesConstants.WORM_FIREBALL_ATTACK_SIZE,
      EnemiesConstants.WORM_FIREBALL_ATTACK_OFFSET)
  }

  override def stopAttack(): Unit = { }
}
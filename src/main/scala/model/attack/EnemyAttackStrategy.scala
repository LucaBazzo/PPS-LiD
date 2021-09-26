package model.attack

import model.collisions.ImplicitConversions.entityToBody
import model.entities.MobileEntity.{createEnergyBallAttack, createFireballAttack, createMeleeAttack}
import model.entities._
import model.helpers.EntitiesFactoryImpl.getEntitiesContainerMonitor
import model.helpers.GeometricUtilities.getBodiesDistance
import model.helpers.WorldUtilities.isBodyVisible
import utils.EnemiesConstants._

abstract class EnemyAttackStrategy(protected val sourceEntity: LivingEntity,
                                   protected val attackSpeed: Float,
                                   protected val attackDuration: Float,
                                   protected val visionAngle: Float,
                                   protected val visionDistance: Float)
  extends AttackStrategyImpl {

  protected val targetEntity:Hero = getEntitiesContainerMonitor.getHero.get

  protected var lastAttackTime: Long = 0
  protected var isAttackFinishedOldCheck: Boolean = true

  override def apply(): Unit = {
    if (this.canAttack) {
      this.lastAttackTime = System.currentTimeMillis()
      spawnAttack()
    }

    val isAttackFinishedNowCheck = this.isAttackFinished
    if (isAttackFinishedNowCheck && !this.isAttackFinishedOldCheck) {
      this.stopAttack()
      this.sourceEntity.setState(State.Standing)
    }
    this.isAttackFinishedOldCheck = isAttackFinishedNowCheck
  }

  override def isAttackFinished: Boolean =
    System.currentTimeMillis() - this.lastAttackTime > this.attackDuration

  protected def canAttack: Boolean = this.isAttackFinished &&
    System.currentTimeMillis() - this.lastAttackTime > this.attackSpeed &&
    isBodyVisible(this.sourceEntity, this.targetEntity, this.visionAngle) &&
    getBodiesDistance(this.sourceEntity, this.targetEntity) <= this.visionDistance

  protected def spawnAttack(): Unit
}

abstract class MeleeAttackStrategy(override protected val sourceEntity: LivingEntity,
                                   override protected val attackSpeed: Float,
                                   override protected val attackDuration: Float,
                                   override protected val visionAngle: Float,
                                   override protected val visionDistance: Float)
  extends EnemyAttackStrategy(sourceEntity, attackSpeed, attackDuration, visionAngle, visionDistance) {

  protected var attackInstance: Option[MobileEntity] = Option.empty
  protected var attackTimer: Long = 0

  override def apply():Unit = {
    // activate the attack box to match the displayed animation
    if (!this.isAttackFinished) {
      val attackProgress: Long = System.currentTimeMillis() - this.attackTimer

      this.updateAttack(attackProgress)
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

  protected def updateAttack(attackProgress:Long): Unit
}

case class SkeletonAttack(override protected val sourceEntity: LivingEntity)
  extends MeleeAttackStrategy(sourceEntity,
    attackSpeed = SKELETON_ATTACK_SPEED, attackDuration = SKELETON_ATTACK_DURATION,
    visionAngle = SKELETON_VISION_ANGLE, visionDistance = SKELETON_VISION_DISTANCE) {

  override protected def updateAttack(attackProgress:Long):Unit = {
    attackProgress match {
      case x if x >= 400 => this.attackInstance.get.getBody.setActive(true)
      case _ => this.attackInstance.get.getBody.setActive(false)
    }
  }

  override protected def spawnAttack(): Unit = {
    this.sourceEntity.setState(State.Attack01)
    this.attackInstance = Option(createMeleeAttack(this.sourceEntity, SKELETON_ATTACK_SIZE, SKELETON_ATTACK_OFFSET))
    this.attackInstance.get.getBody.setActive(false)
  }
}

case class SlimeAttack(override protected val sourceEntity: LivingEntity)
  extends MeleeAttackStrategy(sourceEntity,
    attackSpeed = SLIME_ATTACK_SPEED, attackDuration = SLIME_ATTACK_DURATION,
    visionAngle = SLIME_VISION_ANGLE, visionDistance = SLIME_VISION_DISTANCE) {

  override protected def updateAttack(attackProgress:Long):Unit = {
    attackProgress match {
      case x if x >= 200 => this.attackInstance.get.getBody.setActive(true)
      case _ => this.attackInstance.get.getBody.setActive(false)
    }
  }

  override protected def spawnAttack(): Unit = {
    this.sourceEntity.setState(State.Attack01)
    this.attackInstance = Option(createMeleeAttack(this.sourceEntity, SLIME_ATTACK_SIZE, SLIME_ATTACK_OFFSET))
    this.attackInstance.get.getBody.setActive(false)
  }
}

case class WizardFirstAttack(override protected val sourceEntity: LivingEntity)
  extends MeleeAttackStrategy(sourceEntity,
    attackSpeed = WIZARD_ATTACK1_SPEED, attackDuration = WIZARD_ATTACK1_DURATION,
    visionAngle = WIZARD_VISION_ANGLE, visionDistance = WIZARD_VISION_DISTANCE) {

  override protected def updateAttack(attackProgress:Long):Unit = {
    attackProgress match {
      case x if x >= 400 && x <= 800 => this.attackInstance.get.getBody.setActive(true)
      case _ => this.attackInstance.get.getBody.setActive(false)
    }
  }

  override protected def spawnAttack(): Unit = {
    this.sourceEntity.setState(State.Attack01)
    this.attackInstance = Option(
      createMeleeAttack(this.sourceEntity, WIZARD_ATTACK1_SIZE, WIZARD_ATTACK1_OFFSET))
    this.attackInstance.get.getBody.setActive(false)
  }
}

case class WizardSecondAttack(override protected val sourceEntity: LivingEntity)
  extends MeleeAttackStrategy(sourceEntity,
    attackSpeed = WIZARD_ATTACK2_SPEED, attackDuration = WIZARD_ATTACK2_DURATION,
    visionAngle = WIZARD_VISION_ANGLE, visionDistance = WIZARD_VISION_DISTANCE) {

  override protected def updateAttack(attackProgress:Long):Unit = {
    attackProgress match {
      case x if x >= 300 && x <= 800 => this.attackInstance.get.getBody.setActive(true)
      case _ => this.attackInstance.get.getBody.setActive(false)
    }
  }

  override protected def spawnAttack(): Unit = {
    this.sourceEntity.setState(State.Attack02)
    this.attackInstance = Option(createMeleeAttack(this.sourceEntity,
      WIZARD_ATTACK2_SIZE, WIZARD_ATTACK2_OFFSET))
    this.attackInstance.get.getBody.setActive(false)
  }
}

case class WizardEnergyBallAttack(override protected val sourceEntity: LivingEntity)
  extends EnemyAttackStrategy(sourceEntity,
    attackSpeed = WIZARD_ATTACK3_SPEED, attackDuration = WIZARD_ATTACK3_DURATION,
    visionAngle = WIZARD_VISION_ANGLE, visionDistance = WIZARD_VISION_DISTANCE) {

  override def spawnAttack(): Unit = {
    this.sourceEntity.setState(State.Attack03)
    createEnergyBallAttack(this.sourceEntity, this.targetEntity,
      WIZARD_ATTACK3_SIZE, WIZARD_ATTACK3_OFFSET)
  }

  override protected def canAttack: Boolean = this.isAttackFinished &&
    System.currentTimeMillis() - this.lastAttackTime > this.attackSpeed
}

case class WormFireballAttack(override protected val sourceEntity: LivingEntity)
  extends EnemyAttackStrategy(sourceEntity,
    attackSpeed = WORM_ATTACK_SPEED, attackDuration = WORM_ATTACK_DURATION,
    visionAngle = WORM_VISION_ANGLE, visionDistance = WORM_VISION_DISTANCE) {

  private var attackProgress: Long = 0
  private var targetPoint: Option[(Float, Float)] = None

  override def apply(): Unit = {
    // delay the attack creation
    if (this.sourceEntity.getState == State.Attack01 &&
      this.attackProgress != 0 && System.currentTimeMillis() - this.attackProgress > WORM_ATTACK_CREATION_DELAY ) {
      createFireballAttack(this.sourceEntity, this.targetEntity, this.targetPoint.get,
        WORM_ATTACK_SIZE, WORM_ATTACK_OFFSET)
      this.attackProgress = 0
    }

    super.apply()
  }

  override def spawnAttack(): Unit = {
    this.sourceEntity.setState(State.Attack01)
    this.attackProgress = System.currentTimeMillis()
    this.targetPoint = Option((targetEntity.getBody.getWorldCenter.x, targetEntity.getBody.getWorldCenter.y))
  }
}
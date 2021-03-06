package model.entity.attack

import com.badlogic.gdx.physics.box2d.World
import model.helpers.ImplicitConversions.{entityToBody, RichWorld}
import model.entity.MobileEntity.{createEnergyBallAttack, createFireballAttack, createMeleeAttack}
import model.entity._
import model.helpers.EntitiesFactoryImpl
import model.helpers.EntitiesFactoryImpl.getEntitiesContainerMonitor
import model.helpers.GeometricUtilities.getBodiesDistance
import utils.EnemiesConstants._

/** Implementation of the trait AttackStrategy oriented to enemies attacks.
 *
 * An attack entity is a particular configuration of a MobileEntity, coupled
 * with an appropriate entity type, collision strategy and movement strategy.
 *
 * @see [[model.entity.attack.AttackStrategy]]
 * @see [[model.entity.MobileEntity]]
 * @see [[model.entity.EntityType]]
 *
 * @param sourceEntity the Entity which holds the instance of this abstract
 *                     class
 * @param attackSpeed defined the interval at which an attack can be spawned
 * @param attackDuration defined the duration, in milliseconds, of the spawned
 *                       attack entity
 * @param visionDistance the maximum distance at which an enemy can detect the
 *                       hero entity
 */
abstract class EnemyAttackStrategy(protected val sourceEntity: LivingEntity,
                                   protected val attackSpeed: Long,
                                   protected val attackDuration: Long,
                                   protected val visionDistance: Float) extends AttackStrategy {

  protected val world: World = EntitiesFactoryImpl.getEntitiesContainerMonitor.getWorld.get
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
    world.isBodyVisible(this.sourceEntity, this.targetEntity) &&
    getBodiesDistance(this.sourceEntity, this.targetEntity) <= this.visionDistance

  protected def spawnAttack(): Unit
}

/**
 * A subset of the possible attacks creatable by an enemy entity. Melee attacks
 * are characterized by an attack zone near the enemy entity with no particular
 * movement strategy.
 *
 * @param sourceEntity the Entity which holds the instance of this abstract
 *                     class
 * @param attackSpeed defined the interval at which an attack can be spawned
 * @param attackDuration defined the duration, in milliseconds, of the spawned
 *                       attack entity
 * @param visionDistance the maximum distance at which an enemy can detect the
 *                       hero entity
 */
abstract class MeleeAttackStrategy(override protected val sourceEntity: LivingEntity,
                                   override protected val attackSpeed: Long,
                                   override protected val attackDuration: Long,
                                   override protected val visionDistance: Float)
  extends EnemyAttackStrategy(sourceEntity, attackSpeed, attackDuration, visionDistance) {

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

/** A skeleton attack strategy. It consists on a melee sword attack. The target
 * must be near to be in range for an attack.
 *
 * @param sourceEntity the Entity which holds the instance of this abstract
 *                     class
 */
case class SkeletonAttackStrategy(override protected val sourceEntity: LivingEntity)
  extends MeleeAttackStrategy(sourceEntity,  attackSpeed = SKELETON_ATTACK_SPEED,
    attackDuration = SKELETON_ATTACK_DURATION, visionDistance = SKELETON_VISION_DISTANCE) {

  private val ATTACK_ACTIVATION_DELAY: Int = 200

  override protected def updateAttack(attackProgress:Long):Unit = {
    attackProgress match {
      case x if x >= ATTACK_ACTIVATION_DELAY => this.attackInstance.get.getBody.setActive(true)
      case _ => this.attackInstance.get.getBody.setActive(false)
    }
  }

  override protected def spawnAttack(): Unit = {
    this.sourceEntity.setState(State.Attack01)
    this.attackInstance = Option(createMeleeAttack(this.sourceEntity, SKELETON_ATTACK_SIZE, SKELETON_ATTACK_OFFSET))
    this.attackInstance.get.getBody.setActive(false)
  }
}

/** A slime attack strategy. It consists on a melee body attack. The target
 * must be near to be in range for an attack.
 *
 * @param sourceEntity the Entity which holds the instance of this abstract
 *                     class
 */
case class SlimeAttackStrategy(override protected val sourceEntity: LivingEntity)
  extends MeleeAttackStrategy(sourceEntity, attackSpeed = SLIME_ATTACK_SPEED,
    attackDuration = SLIME_ATTACK_DURATION, visionDistance = SLIME_VISION_DISTANCE) {

  private val ATTACK_ACTIVATION_DELAY: Int = 200

  override protected def updateAttack(attackProgress:Long):Unit = {
    attackProgress match {
      case x if x >= ATTACK_ACTIVATION_DELAY => this.attackInstance.get.getBody.setActive(true)
      case _ => this.attackInstance.get.getBody.setActive(false)
    }
  }

  override protected def spawnAttack(): Unit = {
    this.sourceEntity.setState(State.Attack01)
    this.attackInstance = Option(createMeleeAttack(this.sourceEntity, SLIME_ATTACK_SIZE, SLIME_ATTACK_OFFSET))
    this.attackInstance.get.getBody.setActive(false)
  }
}

/** A bat attack strategy. It consists on a melee body attack. The target
 * must be near to be in range for an attack.
 *
 * @param sourceEntity the Entity which holds the instance of this abstract
 *                     class
 */
case class BatAttackStrategy(override protected val sourceEntity: LivingEntity)
  extends MeleeAttackStrategy(sourceEntity, attackSpeed = BAT_ATTACK_SPEED,
    attackDuration = BAT_ATTACK_DURATION, visionDistance = BAT_VISION_DISTANCE) {

  private val ATTACK_ACTIVATION_DELAY: Int = 200

  override protected def updateAttack(attackProgress:Long):Unit = {
    attackProgress match {
      case x if x >= ATTACK_ACTIVATION_DELAY => this.attackInstance.get.getBody.setActive(true)
      case _ => this.attackInstance.get.getBody.setActive(false)
    }
  }

  override protected def canAttack: Boolean = this.isAttackFinished &&
    System.currentTimeMillis() - this.lastAttackTime > this.attackSpeed &&
    getBodiesDistance(this.sourceEntity, this.targetEntity) <= sourceEntity.getSize._1

  override protected def spawnAttack(): Unit = {
    this.sourceEntity.setState(State.Attack01)
    this.attackInstance = Option(createMeleeAttack(this.sourceEntity, BAT_ATTACK_SIZE, BAT_ATTACK_OFFSET))
    this.attackInstance.get.getBody.setActive(false)
  }
}

/** A wizard attack strategy. It consists on a melee sword attack. The target
 * must be near to be in range for an attack.
 *
 * @param sourceEntity the Entity which holds the instance of this abstract
 *                     class
 */
case class WizardFirstAttackStrategy(override protected val sourceEntity: LivingEntity)
  extends MeleeAttackStrategy(sourceEntity, attackSpeed = WIZARD_ATTACK1_SPEED,
    attackDuration = WIZARD_ATTACK1_DURATION, visionDistance = WIZARD_VISION_DISTANCE) {

  private val ATTACK_ACTIVATION_DELAY: Int = 400
  private val ATTACK_DURATION: Int = 800

  override protected def updateAttack(attackProgress:Long):Unit = {
    attackProgress match {
      case x if x >= ATTACK_ACTIVATION_DELAY && x <= ATTACK_DURATION => this.attackInstance.get.getBody.setActive(true)
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

/** A wizard second attack strategy. It consists on a melee sword attack. The target
 * must be near to be in range for an attack.
 *
 * @param sourceEntity the Entity which holds the instance of this abstract
 *                     class
 */
case class WizardSecondAttackStrategy(override protected val sourceEntity: LivingEntity)
  extends MeleeAttackStrategy(sourceEntity, attackSpeed = WIZARD_ATTACK2_SPEED,
    attackDuration = WIZARD_ATTACK2_DURATION, visionDistance = WIZARD_VISION_DISTANCE) {

  private val ATTACK_ACTIVATION_DELAY: Int = 300
  private val ATTACK_DURATION: Int = 800

  override protected def updateAttack(attackProgress:Long):Unit = {
    attackProgress match {
      case x if x >= ATTACK_ACTIVATION_DELAY && x <= ATTACK_DURATION => this.attackInstance.get.getBody.setActive(true)
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

/** A wizard ranged attack strategy. It consists on an hooming projectile
 * attack. The target can be far or behind walls and floors to be in range for an attack.
 *
 * @param sourceEntity the Entity which holds the instance of this abstract
 *                     class
 */
case class WizardEnergyBallAttackStrategy(override protected val sourceEntity: LivingEntity)
  extends EnemyAttackStrategy(sourceEntity, attackSpeed = WIZARD_ATTACK3_SPEED,
    attackDuration = WIZARD_ATTACK3_DURATION, visionDistance = WIZARD_VISION_DISTANCE) {

  override def spawnAttack(): Unit = {
    this.sourceEntity.setState(State.Attack03)
    createEnergyBallAttack(this.sourceEntity, this.targetEntity,
      WIZARD_ATTACK3_SIZE, WIZARD_ATTACK3_OFFSET)
  }

  override protected def canAttack: Boolean = this.isAttackFinished &&
    System.currentTimeMillis() - this.lastAttackTime > this.attackSpeed
}

/** A worm ranged attack strategy. It consists on an linear projectile
 * attack. The target must be visible and not too far to be in range
 * for an attack.
 *
 * @param sourceEntity the Entity which holds the instance of this abstract
 *                     class
 */
case class WormFireballAttackStrategy(override protected val sourceEntity: LivingEntity)
  extends EnemyAttackStrategy(sourceEntity, attackSpeed = WORM_ATTACK_SPEED,
    attackDuration = WORM_ATTACK_DURATION, visionDistance = WORM_VISION_DISTANCE) {

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

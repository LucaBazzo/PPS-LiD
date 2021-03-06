package model.entity.attack

import controller.GameEvent
import controller.GameEvent.GameEvent
import model.helpers.ImplicitConversions._
import model.entity.State._
import model.entity._
import model.entity.movement.DoNothingMovementStrategy
import utils.HeroConstants._

/** Implementation of the Hero Attack Strategy with sword and bow.
 *
 *  @constructor the main hero attack strategy
 *  @param entity the entity that generates the attacks
 *  @param strength the damage that each attack will do
 */
case class HeroAttackStrategy(private val entity: Hero, private var strength: Float) extends AttackStrategy {

  private var attackPattern: Option[MobileEntity] = Option.empty
  private var attackTimer: Int = 0
  private var timeEventPresent: Boolean = false

  override def apply(command: GameEvent): Unit = {
    if (checkCommand(command)) {
      command match {
        case GameEvent.Attack if isInAir => this.setAirAttack()
        case GameEvent.Attack => this.setSwordAttack()
        case GameEvent.BowAttack => this.setBowAttack()
      }
    }
  }

  override def stopAttack(): Unit = {
    if(this.attackPattern.nonEmpty) {
      this.attackPattern.get.destroyEntity()
      this.attackPattern = Option.empty
    }
  }

  override def isAttackFinished: Boolean = this.attackTimer <= 0

  override def decrementAttackTimer(): Unit = this.attackTimer -= ATTACK_STRATEGY_TIMER_DECREMENT

  override def checkTimeEvent(): Unit = this.entity.getState match {
    case State.Attack03 if !timeEventPresent && this.attackTimer <= THIRD_SWORD_STARTING_TIME
      && this.attackTimer > THIRD_SWORD_STOP_TIME => this.startThirdSwordAttack()
    case State.Attack03 if timeEventPresent && this.attackTimer <= THIRD_SWORD_STOP_TIME =>
      this.stopThirdSwordAttack()
    case State.BowAttacking if timeEventPresent && this.attackTimer <= BOW_ATTACK_STARTING_TIME =>
      this.startBowAttack()
    case _ =>
  }

  override def alterStrength(alteration: Float): Unit = this.strength += alteration

  private def restartTimer(value: Int): Unit = this.attackTimer = value

  private def checkCommand(command: GameEvent): Boolean = command match {
    case GameEvent.Attack => (entity is Running) || (entity is Standing) ||
      (entity is Attack01) || (entity is Attack02) || isInAir
    case GameEvent.BowAttack => isBowPicked &&
      ((entity is Running) || (entity is Standing))
    case _ => false
  }

  private def setSwordAttack(): Unit = {
    this.entity.stopMovement()

    this.entity.getState match {
      case State.Attack01 if this.attackTimer < WAIT_FOR_ANOTHER_CONSECUTIVE_ATTACK => this.secondSwordAttack()
      case State.Attack02 if this.attackTimer < WAIT_FOR_ANOTHER_CONSECUTIVE_ATTACK => this.thirdSwordAttack()
      case s: State if s != State.Attack02 && s != State.Attack03 && this.isAttackFinished => this.firstSwordAttack()
      case _ =>
    }
  }

  private def firstSwordAttack(): Unit = {
    this.entity.setState(State.Attack01)
    this.firstSwordChoice()
    this.attackPattern.get.move()
    this.restartTimer(FIRST_SWORD_ATTACK_DURATION)
  }

  private def secondSwordAttack(): Unit = {
    this.stopAttack()
    this.entity.setState(State.Attack02)
    this.secondSwordChoice()
    this.attackPattern.get.move()
    this.restartTimer(SECOND_SWORD_ATTACK_DURATION)
  }

  private def thirdSwordAttack(): Unit = {
    this.timeEventPresent = false
    this.stopAttack()
    this.entity.setState(State.Attack03)
    this.thirdSwordChoice()
    this.restartTimer(THIRD_SWORD_ATTACK_DURATION)
  }

  private def startThirdSwordAttack(): Unit = {
    this.attackPattern.get.move()
    this.timeEventPresent = true
  }

  private def stopThirdSwordAttack(): Unit = {
    this.attackPattern.get.stopMovement()
    this.timeEventPresent = false
  }

  private def firstSwordChoice(): Unit = {
    if (this.entity.isFacingRight)
      this.setSwordPattern(FIRST_SWORD_ATTACK_SIZE, FIRST_SWORD_ATTACK_OFFSET,
        FIRST_SWORD_ATTACK_ANGULAR_VELOCITY , FIRST_SWORD_ATTACK_STARTING_ANGLE)
    else
      this.setSwordPattern(FIRST_SWORD_ATTACK_SIZE, FIRST_SWORD_ATTACK_OFFSET,
        -FIRST_SWORD_ATTACK_ANGULAR_VELOCITY , -FIRST_SWORD_ATTACK_STARTING_ANGLE)
  }

  private def secondSwordChoice(): Unit = {
    if (this.entity.isFacingRight)
      this.setSwordPattern(SECOND_SWORD_ATTACK_SIZE, SECOND_SWORD_ATTACK_OFFSET,
        SECOND_SWORD_ATTACK_ANGULAR_VELOCITY , SECOND_SWORD_ATTACK_STARTING_ANGLE)
    else
      this.setSwordPattern(SECOND_SWORD_ATTACK_SIZE, SECOND_SWORD_ATTACK_OFFSET,
        -SECOND_SWORD_ATTACK_ANGULAR_VELOCITY, SECOND_SWORD_ATTACK_STARTING_ANGLE)
  }

  private def thirdSwordChoice(): Unit = {
    if (this.entity.isFacingRight)
      this.setSwordPattern(THIRD_SWORD_ATTACK_SIZE, THIRD_SWORD_ATTACK_OFFSET,
        THIRD_SWORD_ATTACK_ANGULAR_VELOCITY)
    else
      this.setSwordPattern(THIRD_SWORD_ATTACK_SIZE, THIRD_SWORD_ATTACK_OFFSET.INV,
        -THIRD_SWORD_ATTACK_ANGULAR_VELOCITY)
  }

  private def setSwordPattern(rotatingBodySize: (Float, Float),
                              rotatingBodyDistance: (Float, Float),
                              angularVelocity: Float, startingAngle: Float = 0): Unit = {
    this.attackPattern = Option.apply(MobileEntity.createSwordAttackPattern(EntityType.Mobile,
      rotatingBodySize, rotatingBodyDistance, angularVelocity, startingAngle, this.entity))
  }

  private def setAirAttack(): Unit = {
    entity.stopMovement()
    entity.setMovementStrategy(DoNothingMovementStrategy())
    entity.setVelocityY(-AIR_DOWN_ATTACK_VELOCITY)
    this.setAirSwordPattern(AIR_SWORD_ATTACK_SIZE, AIR_SWORD_ATTACK_OFFSET)
    entity setState AirDownAttacking
    entity setAirAttacking true
  }

  private def setAirSwordPattern(bodySize: (Float, Float), bodyDistance: (Float, Float)): Unit = {
    this.attackPattern = Option.apply(MobileEntity.createAirAttackPattern(bodySize, bodyDistance, this.entity))
  }

  private def setBowAttack(): Unit = {
    this.entity.stopMovement()
    this.entity.setState(State.BowAttacking)
    this.restartTimer(BOW_ATTACK_DURATION)
    this.timeEventPresent = true
  }

  private def startBowAttack(): Unit = {
    this.attackPattern = Option.apply(MobileEntity.createArrowProjectile(this.entity))
    this.attackPattern.get.move()
    this.attackPattern = Option.empty
    this.timeEventPresent = false
  }

  private def isBowPicked: Boolean = this.entity.isItemPicked(Items.Bow)

  private def isInAir: Boolean = (entity is Jumping) || (entity is Somersault) || (entity is Falling)
}

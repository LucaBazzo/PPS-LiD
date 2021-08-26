package model.attack

import controller.GameEvent
import controller.GameEvent.GameEvent
import model.entities.{Hero, MobileEntity, State, Statistic}
import model.helpers.EntitiesFactoryImpl
import model.movement.ArrowMovementStrategy
import utils.ApplicationConstants.PIXELS_PER_METER
import model.collisions.ImplicitConversions._

class HeroAttackStrategyImpl(private val entity: Hero, private var strength: Float) extends AttackStrategy {

  private var attackPattern: MobileEntity = _
  private var attackTimer: Float = 0
  private var timeEventPresent: Boolean = false

  override def apply(command: GameEvent): Unit = {
    if (checkCommand(command)) {
      command match {
        case GameEvent.Attack => this.setAttack()
        case GameEvent.BowAttack => this.setBowAttack()
      }
    }
  }

  override def stopAttack(): Unit = this.attackPattern.destroyEntity()

  override def isAttackFinished: Boolean = this.attackTimer <= 0

  override def decrementAttackTimer(): Unit = this.attackTimer -= 3

  private def restartTimer(value: Float): Unit = this.attackTimer = value

  private def checkCommand(command: GameEvent): Boolean = {
    command match {
      case GameEvent.Attack => return entity.getState == State.Running || entity.getState == State.Standing ||
        entity.getState == State.Attack01 || entity.getState == State.Attack02
      case GameEvent.BowAttack => return entity.getState == State.Running || entity.getState == State.Standing
      case _ => throw new UnsupportedOperationException
    }
    false
  }

  private def setAttack(): Unit = {
    this.entity.stopMovement()
    if (this.entity.getState == State.Attack01 && this.attackTimer < 75) {
      this.entity.setState(State.Attack02)
      stopAttack()
      this.setAttackPattern()
      this.attackPattern.move()
      this.restartTimer(130)
    }
    else if (this.entity.getState == State.Attack02 && this.attackTimer < 75) {
      this.timeEventPresent = false
      this.entity.setState(State.Attack03)
      stopAttack()
      this.setAttackPattern()
      this.restartTimer(150)
    }
    else if (this.entity.getState != State.Attack02 && this.entity.getState != State.Attack03
      && this.isAttackFinished) {
      this.entity.setState(State.Attack01)
      this.setAttackPattern()
      this.attackPattern.move()
      this.restartTimer(100)
    }
  }

  private def setAttackPattern(): Unit = this.entity.getState match {
    case State.Attack01 if this.entity.isFacingRight =>
      this.attackPattern = EntitiesFactoryImpl.createAttackPattern((1f, 10f),
        this.entity.getPosition, (0, -15f), 60, 100)
    case State.Attack01 if !this.entity.isFacingRight =>
      this.attackPattern = EntitiesFactoryImpl.createAttackPattern((1f, 10f),
        this.entity.getPosition, (0, -15f), -60, -100)
    case State.Attack02 if this.entity.isFacingRight =>
      this.attackPattern = EntitiesFactoryImpl.createAttackPattern((1f, 10f),
        this.entity.getPosition, (0, 15f), -60, 10)
    case State.Attack02 if !this.entity.isFacingRight =>
      this.attackPattern = EntitiesFactoryImpl.createAttackPattern((1f, 10f),
        this.entity.getPosition, (0, 15f), 60, 10)
    case State.Attack03 if this.entity.isFacingRight =>
      this.attackPattern = EntitiesFactoryImpl.createAttackPattern((10f, 2f),
        this.entity.getPosition, (15f, 0), -80)
    case State.Attack03 if !this.entity.isFacingRight =>
      this.attackPattern = EntitiesFactoryImpl.createAttackPattern((10f, 2f),
        this.entity.getPosition, (-15f, 0), 80)
    case _ => throw new UnsupportedOperationException
  }

  private def setBowAttack(): Unit = {
    this.entity.stopMovement()
    this.entity.setState(State.BowAttack)
    //TODO mettere a posto
    val size: (Float, Float) = (10, 1)
    val newPosition: (Float, Float) = this.entity.getPosition * PIXELS_PER_METER + (this.entity.getSize._1 * PIXELS_PER_METER + 10, 0)
    this.attackPattern = EntitiesFactoryImpl.createMobileEntity(size, newPosition , useGravity = false)
    this.attackPattern.setMovementStrategy(new ArrowMovementStrategy(this.attackPattern, this.entity.getStatistics(Statistic.MovementSpeed)))
    this.restartTimer(175)
    println("Bow Attack")
  }

  override def checkTimeEvent(): Unit = {
    if (this.entity.getState == State.Attack03 && !timeEventPresent &&
      this.attackTimer <= 120 && this.attackTimer > 60) {
      this.attackPattern.move()
      this.timeEventPresent = true
    }
    if (timeEventPresent && this.entity.getState == State.Attack03 && this.attackTimer <= 60) {
      this.attackPattern.stopMovement()
      this.timeEventPresent = false
    }

    //TODO riguardare in futuro per refactoring
    if (this.entity.getState == State.BowAttack && !timeEventPresent &&
      this.attackTimer <= 20 && this.attackTimer > 10) {
      this.attackPattern.move()
      this.timeEventPresent = true
    }
    if (timeEventPresent && this.entity.getState == State.BowAttack && this.attackTimer <= 10) {
      this.timeEventPresent = false
    }
  }

  override def alterStrength(alteration: Float): Unit = this.strength += alteration
}

package model.attack

import controller.GameEvent
import controller.GameEvent.GameEvent
import model.entities.State.State
import model.entities.{EntityType, Hero, MobileEntity, State}
import model.helpers.EntitiesFactoryImpl

class HeroAttackStrategy(private val entity: Hero, private var strength: Float) extends AttackStrategy {

  private var attackPattern: Option[MobileEntity] = Option.empty
  private var attackTimer: Float = 0
  private var timeEventPresent: Boolean = false

  override def apply(command: GameEvent): Unit = {
    if (checkCommand(command)) {
      command match {
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

  override def decrementAttackTimer(): Unit = this.attackTimer -= 3

  override def checkTimeEvent(): Unit = this.entity.getState match {
    case State.Attack03 if !timeEventPresent && this.attackTimer <= 120 && this.attackTimer > 60 =>
      this.startThirdSwordAttack()
    case State.Attack03 if timeEventPresent && this.attackTimer <= 60 => this.stopThirdSwordAttack()
    case State.BowAttack if this.attackTimer <= 20 && this.attackTimer > 10 => this.startBowAttack()
    case _ =>
  }

  override def alterStrength(alteration: Float): Unit = this.strength += alteration

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

  private def setSwordAttack(): Unit = {
    this.entity.stopMovement()
    //this.entity.setEnvironmentInteraction(Option.apply(HeroInteraction(GameEvent.Interaction, new LadderInteraction(this.entity))))

    this.entity.getState match {
      case State.Attack01 if this.attackTimer < 75 => this.secondSwordAttack()
      case State.Attack02 if this.attackTimer < 75 => this.thirdSwordAttack()
      case s: State if s != State.Attack02 && s != State.Attack03 && this.isAttackFinished => this.firstSwordAttack()
      case _ =>
    }
  }

  private def firstSwordAttack(): Unit = {
    this.entity.setState(State.Attack01)
    this.swordPatternChoice()
    this.attackPattern.get.move()
    this.restartTimer(100)
  }

  private def secondSwordAttack(): Unit = {
    this.stopAttack()
    this.entity.setState(State.Attack02)
    this.swordPatternChoice()
    this.attackPattern.get.move()
    this.restartTimer(130)
  }

  private def thirdSwordAttack(): Unit = {
    this.timeEventPresent = false
    this.stopAttack()
    this.entity.setState(State.Attack03)
    this.swordPatternChoice()
    this.restartTimer(150)
  }

  private def startThirdSwordAttack(): Unit = {
    this.attackPattern.get.move()
    this.timeEventPresent = true
  }

  private def stopThirdSwordAttack(): Unit = {
    this.attackPattern.get.stopMovement()
    this.timeEventPresent = false
  }

  private def swordPatternChoice(): Unit = this.entity.getState match {
    case State.Attack01 if this.entity.isFacingRight =>
      this.setSwordPattern((1f, 10f), (0, -15f), 60 ,100)
    case State.Attack01 if !this.entity.isFacingRight =>
      this.setSwordPattern((1f, 10f), (0, -15f), -60 ,-100)
    case State.Attack02 if this.entity.isFacingRight =>
      this.setSwordPattern((1f, 10f), (0, 15f), -60 ,10)
    case State.Attack02 if !this.entity.isFacingRight =>
      this.setSwordPattern((1f, 10f), (0, 15f), 60 ,10)
    case State.Attack03 if this.entity.isFacingRight =>
      this.setSwordPattern((10f, 2f), (15f, 0), -80)
    case State.Attack03 if !this.entity.isFacingRight =>
      this.setSwordPattern((10f, 2f), (-15f, 0), 80)
    case _ => throw new UnsupportedOperationException
  }

  private def setSwordPattern(rotatingBodySize: (Float, Float),
                              rotatingBodyDistance: (Float, Float),
                              angularVelocity: Float, startingAngle: Float = 0): Unit = {
    this.attackPattern = Option.apply(EntitiesFactoryImpl.createAttackPattern(EntityType.Mobile, rotatingBodySize,
      this.entity.getPosition, rotatingBodyDistance, angularVelocity, startingAngle, this.entity))
  }

  private def setBowAttack(): Unit = {
    this.entity.stopMovement()
    this.entity.setState(State.BowAttack)
    this.restartTimer(175)
  }

  private def startBowAttack(): Unit = {
    this.attackPattern = Option.apply(EntitiesFactoryImpl.createArrowProjectile(this.entity))
    this.attackPattern.get.move()
    this.attackPattern = Option.empty
  }
}

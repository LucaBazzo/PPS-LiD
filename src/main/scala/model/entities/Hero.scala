package model.entities

import controller.GameEvent
import controller.GameEvent.GameEvent
import model.attack.DoNothingAttackStrategy
import model.collisions.ImplicitConversions._
import model.entities.EntityType.EntityType
import model.entities.Items.Items
import model.entities.State.State
import model.entities.Statistic.Statistic
import model.helpers.EntitiesFactoryImpl
import model.helpers.EntitiesFactoryImpl.createPolygonalShape
import model.movement.DoNothingMovementStrategy
import model.{EntityBody, HeroInteraction}
import utils.ApplicationConstants.HERO_SIZE

trait Hero extends LivingEntity {

  def notifyCommand(command: GameEvent)

  def isLittle: Boolean
  def setLittle(little: Boolean)

  def changeHeroFixture(newSize: (Float, Float), addCoordinates: (Float, Float) = (0,0))

  def itemPicked(itemType: Items)
  def getItemsPicked: List[Items]

  def setEnvironmentInteraction(interaction: Option[HeroInteraction])

  def stopHero(time: Float)

  def setFeet(feet: ImmobileEntity)
  def getFeet: Option[ImmobileEntity]

  def isTouchingGround: Boolean
}

class HeroImpl(private val entityType: EntityType,
               private var entityBody: EntityBody,
               private var size: (Float, Float),
               private val stats: Map[Statistic, Float])
  extends LivingEntityImpl(entityType, entityBody, size, stats) with Hero{

  private var feet: Option[ImmobileEntity] = Option.empty
  private var interaction: Option[HeroInteraction] = Option.empty
  private var itemsPicked: List[Items] = List.empty

  private var little: Boolean = false
  private var waitTimer: Float = 0

  override def notifyCommand(command: GameEvent): Unit = {
    if(this.interaction.nonEmpty && this.interaction.get.command == command)
      this.interaction.get.environmentInteraction.apply()
    else {
      command match {
        case GameEvent.Up | GameEvent.UpReleased | GameEvent.MoveRight | GameEvent.MoveLeft
             | GameEvent.Slide | GameEvent.Down | GameEvent.DownReleased => move(command)
        case GameEvent.Attack | GameEvent.BowAttack => attack(command)
        case GameEvent.Interaction =>
        case _ => throw new UnsupportedOperationException
      }
    }
  }

  def move(command: GameEvent): Unit = {
    if(this.movementStrategy != null)
      this.movementStrategy.apply(command)
  }

  def attack(command: GameEvent): Unit = {
    if(this.attackStrategy != null)
      this.attackStrategy.apply(command)
  }

  override def update(): Unit = {

    if(isNotWaiting) {
      if(this.isSlidingFinished)
        this.stopMovement()

      if(isDead)
        this.setState(State.Dying)
      else if(checkFalling)
        this.setState(State.Falling)
      else if(checkRunning)
        this.setState(State.Running)
      else if(checkIdle)
        this.setState(State.Standing)

      if(checkNotLittle) {
        this.changeHeroFixture(HERO_SIZE, (0, 6f))
        this.setLittle(false)
      }

      if(!this.attackStrategy.isAttackFinished) {
        this.attackStrategy.checkTimeEvent()
        this.attackStrategy.decrementAttackTimer()

        if(this.isBowAttackFinished){
          this.setState(State.Standing)
        }
        else if(this.attackStrategy.isAttackFinished){
          this.attackStrategy.stopAttack()
          this.setState(State.Standing)
        }
      }

    } else this.decrementWaiting(10)  //for sliding and crouch redefinition of body
  }

  override def setState(state: State): Unit = {
    super.setState(state)
  }

  override def setLittle(little: Boolean): Unit = {
    this.little = little
    this.stopHero(150)
  }

  override def isLittle: Boolean = this.little

  override def changeHeroFixture(newSize: (Float, Float), addCoordinates: (Float, Float) = (0,0)): Unit = {
    this.entityBody
      .setShape(createPolygonalShape(newSize.PPM))
      .createFixture()

    this.setSize(newSize.PPM)

    EntitiesFactoryImpl.createHeroFeet(this)

    this.entityBody.addCoordinates(addCoordinates._1.PPM, addCoordinates._2.PPM)
  }

  override def itemPicked(itemType: Items): Unit = {
    this.stopHero(300)
    this.stopMovement()
    this.setState(State.ItemPicked)
    this.itemsPicked = itemType :: this.itemsPicked
  }

  override def getItemsPicked: List[Items] = this.itemsPicked

  override def setEnvironmentInteraction(interaction: Option[HeroInteraction]): Unit =
    this.interaction = interaction

  override def stopHero(time: Float): Unit = this.waitTimer = time

  override def sufferDamage(damage: Float): Unit = {
    super.sufferDamage(damage)
    if (this.getStatistic(Statistic.CurrentHealth) <= 0) {
      this.attackStrategy.stopAttack()
      this.setAttackStrategy(DoNothingAttackStrategy())
      this.setMovementStrategy(DoNothingMovementStrategy())
    }
    else {
      this.stopHero(150)
      this.setState(State.Hurt)
    }
  }

  override def stopMovement(): Unit = super.stopMovement()

  override def isTouchingGround: Boolean = if(this.feet.nonEmpty) this.feet.get.isColliding else false

  private def isNotWaiting: Boolean = this.waitTimer <= 0
  private def decrementWaiting(value: Float): Unit = this.waitTimer -= value

  private def isDead: Boolean = this.getStatistic(Statistic.CurrentHealth) <= 0
  private def isFalling: Boolean = !this.isTouchingGround && this.entityBody.getBody.getLinearVelocity.y < 0
  private def isMovingHorizontally: Boolean = this.entityBody.getBody.getLinearVelocity.x != 0 && this.entityBody.getBody.getLinearVelocity.y == 0
  private def isIdle = this.entityBody.getBody.getLinearVelocity.x == 0 && this.entityBody.getBody.getLinearVelocity.y == 0

  private def checkFalling: Boolean = isFalling && this.state != State.Jumping && this.state != State.LadderDescend
  private def checkRunning: Boolean = isMovingHorizontally && (this.getState == State.Jumping || this.getState == State.Falling)
  private def checkIdle: Boolean = {
    isIdle && !isSwordAttacking && this.getState != State.Crouch &&
      this.getState != State.BowAttack && this.getState != State.LadderIdle
  }

  private def checkNotLittle: Boolean = this.getState != State.Sliding && this.getState != State.Crouch && isLittle
  private def isSwordAttacking: Boolean = this.getState == State.Attack01 || this.getState == State.Attack02 || this.getState == State.Attack03
  private def isSwordAttackFinished: Boolean = this.isSwordAttacking && this.attackStrategy.isAttackFinished
  private def isBowAttackFinished: Boolean = this.getState == State.BowAttack && this.attackStrategy.isAttackFinished
  private def isSlidingFinished: Boolean = {
    (this.entityBody.getBody.getLinearVelocity.x <= 1 && this.getState == State.Sliding && isFacingRight) ||
      (this.entityBody.getBody.getLinearVelocity.x >= -1 && this.getState == State.Sliding && !isFacingRight)
  }

  override def setFeet(feet: ImmobileEntity): Unit = this.feet = Option.apply(feet)

  override def getFeet: Option[ImmobileEntity] = this.feet
}

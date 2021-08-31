package model.entities

import controller.GameEvent
import controller.GameEvent.GameEvent
import model.collisions.ImplicitConversions._
import model.entities.EntityType.EntityType
import model.entities.Items.Items
import model.entities.State.State
import model.entities.Statistic.Statistic
import model.helpers.EntitiesFactoryImpl.createPolygonalShape
import model.{EntityBody, HeroInteraction}
import utils.ApplicationConstants.HERO_SIZE

trait Hero extends LivingEntity {

  def notifyCommand(command: GameEvent)

  def getPreviousState: State
  def getLinearVelocityX: Float

  def isLittle: Boolean
  def setLittle(little: Boolean)

  def changeHeroFixture(newSize: (Float, Float), addCoordinates: (Float, Float) = (0,0))

  def itemPicked(itemType: Items)
  def getItemsPicked: List[Items]

  def setEnvironmentInteraction(interaction: Option[HeroInteraction])
}

class HeroImpl(private val entityType: EntityType,
               private var entityBody: EntityBody,
               private val size: (Float, Float),
               private val stats: Map[Statistic, Float])
  extends LivingEntityImpl(entityType, entityBody, size, stats) with Hero{

  private var previousState: State = State.Standing
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

    //for sliding and crouch redefinition of body
    if(waitTimer > 0) {
      waitTimer -= 10
    }
    else {
      if((this.entityBody.getBody.getLinearVelocity.x <= 1 && this.state == State.Sliding && isFacingRight) ||
        this.entityBody.getBody.getLinearVelocity.x >= -1 && this.state == State.Sliding && !isFacingRight) {
        this.stopMovement()
      }

      //println(this.getState, this.attackTimer)

      if(this.entityBody.getBody.getLinearVelocity.y < 0
        && (this.state != State.Jumping && this.state != State.LadderDescend))
        this.state = State.Falling
      else if(this.entityBody.getBody.getLinearVelocity.y == 0 && this.entityBody.getBody.getLinearVelocity.x != 0
        && (this.state == State.Jumping || this.state == State.Falling))
        this.state = State.Running
      else if((this.entityBody.getBody.getLinearVelocity.y == 0 && this.entityBody.getBody.getLinearVelocity.x == 0)
        && this.state != State.Crouch
        && this.state != State.Attack01 && this.state != State.Attack02 && this.state != State.Attack03
        && this.state != State.BowAttack
        && this.state != State.LadderIdle)
        this.state = State.Standing

      if(this.state != State.Sliding && this.state != State.Crouch && isLittle) {
        this.changeHeroFixture(HERO_SIZE, (0, 5f))
        this.setLittle(false)
      }

      if(!this.attackStrategy.isAttackFinished) {
        this.attackStrategy.checkTimeEvent()
        this.attackStrategy.decrementAttackTimer()
      }

      if((this.state == State.Attack01 || this.state == State.Attack02 || this.state == State.Attack03)
            && this.attackStrategy.isAttackFinished){
        this.attackStrategy.stopAttack()
        this.state = State.Standing
      }

      if(this.state == State.BowAttack && this.attackStrategy.isAttackFinished){
        this.state = State.Standing
      }
    }
  }

  override def getLinearVelocityX: Float = this.entityBody.getBody.getLinearVelocity.x

  override def getPreviousState: State = this.previousState

  override def setState(state: State): Unit = {
    super.setState(state)
    this.previousState = state
  }

  override def setLittle(little: Boolean): Unit = {
    this.little = little
    this.waitTimer = 150
  }

  override def isLittle: Boolean = this.little

  override def changeHeroFixture(newSize: (Float, Float), addCoordinates: (Float, Float) = (0,0)): Unit = {
    this.entityBody
      .setShape(createPolygonalShape(newSize.PPM))
      .createFixture()

    this.entityBody.addCoordinates(addCoordinates._1.PPM, addCoordinates._2.PPM)
  }

  private var itemsPicked: List[Items] = List.empty

  override def itemPicked(itemType: Items): Unit = this.itemsPicked = itemType :: this.itemsPicked

  override def getItemsPicked: List[Items] = this.itemsPicked

  private var interaction: Option[HeroInteraction] = Option.empty

  override def setEnvironmentInteraction(interaction: Option[HeroInteraction]): Unit =
    this.interaction = interaction
}

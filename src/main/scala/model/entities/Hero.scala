package model.entities

import controller.GameEvent
import controller.GameEvent.GameEvent
import model.EntityBody
import model.collisions.ImplicitConversions._
import model.entities.State.State
import model.entities.Statistic.Statistic
import model.helpers.EntitiesFactoryImpl.createPolygonalShape
import utils.ApplicationConstants.{HERO_SIZE, HERO_SIZE_SMALL}

import scala.collection.mutable

trait Hero extends LivingEntity {

  def notifyCommand(command: GameEvent)

  def getPreviousState: State
  def getLinearVelocityX: Float

  def setState(state: State)
  def isLittle: Boolean
  def setLittle(little: Boolean)

  def changeHeroFixture(newSize: (Float, Float), addCoordinates: (Float, Float) = (0,0))
}

class HeroImpl(private var entityBody: EntityBody, private val size: (Float, Float), private val statistics:mutable.Map[Statistic, Float]) extends LivingEntityImpl(entityBody, size, statistics) with Hero{

  private var previousState: State = State.Standing
  private var little: Boolean = false

  private var waitTimer: Float = 0

  override def notifyCommand(command: GameEvent): Unit = command match {
    case GameEvent.Jump | GameEvent.MoveRight | GameEvent.MoveLeft | GameEvent.Slide => move(command)
    case GameEvent.Crouch => this.crouch()
    case GameEvent.StopCrouch => if(this.state == State.Crouch) this.state = State.Standing
    case GameEvent.Attack => attack(command)
    case _ => throw new UnsupportedOperationException
  }

  def move(command: GameEvent): Unit = {
    if(this.movementStrategy != null)
      this.movementStrategy.apply(command)
  }

  def attack(command: GameEvent): Unit = {
    if(this.attackStrategy != null)
      this.attackStrategy.apply(command)
  }

  private def crouch(): Unit = {
    this.state match {
      case State.Standing | State.Running =>
        this.stopMovement()
        this.changeHeroFixture(HERO_SIZE_SMALL, (0, -6f))
        this.state = State.Crouch
        this.setLittle(true)
      case _ =>
    }
  }

  override def update(): Unit = {
    /*this.state match {
      case model.entities.State.Running => {
        if(this.body.getLinearVelocity.y == 0 && this.body.getLinearVelocity.x == 0)
          this.state = State.Standing
        else if(this.body.getLinearVelocity.y < 0) {
          this.state = State.Falling
        }
      }
      case model.entities.State.Jumping => {
        if(this.body.getLinearVelocity.y == 0)
          this.state = State.Standing
      }
      case model.entities.State.Falling => {
        if(this.body.getLinearVelocity.y == 0)
          this.state = State.Standing
      }
      case model.entities.State.Sliding =>
      case model.entities.State.Crouch =>
      case _ =>
    }*/


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

      if(this.entityBody.getBody.getLinearVelocity.y < 0 && this.state != State.Jumping)
        this.state = State.Falling
      else if(this.entityBody.getBody.getLinearVelocity.y == 0 && this.entityBody.getBody.getLinearVelocity.x != 0
        && (this.state == State.Jumping || this.state == State.Falling))
        this.state = State.Running
      else if((this.entityBody.getBody.getLinearVelocity.y == 0 && this.entityBody.getBody.getLinearVelocity.x == 0)
        && this.state != State.Crouch &&
        this.state != State.Attack01 && this.state != State.Attack02 && this.state != State.Attack03)
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
    }
  }

  /*override def update(): Unit = {
    /*if(this.body.getLinearVelocity.y != 0 || this.body.getLinearVelocity.x == 0)
      setSliding(false)*/

    if((this.body.getLinearVelocity.x <= 1 && isSliding && isFacingRight) ||
        this.body.getLinearVelocity.x >= -1 && isSliding && !isFacingRight) {
      this.stopMovement()
      setSliding(false)
    }

    if((this.isLittle && !this.isSliding) && (this.isLittle && this.state != State.Crouch)) {
      EntitiesFactoryImpl.defineNormalHero(this)
      this.isLittle = false
    }

    if(this.body.getLinearVelocity.y > 0 || (this.body.getLinearVelocity.y < 0 && this.previousState == State.Jumping))
      this.state = State.Jumping
    else if(this.body.getLinearVelocity.y < 0)
      this.state = State.Falling
    else if(this.body.getLinearVelocity.x != 0 && !isSliding)
      this.state = State.Running
    else if(this.body.getLinearVelocity.x != 0 && isSliding)
      this.state = State.Sliding
    else if(this.body.getLinearVelocity.x == 0 && isLittle && this.state == State.Crouch)
      this.state = State.Crouch
    else
      this.state = State.Standing
  }*/

  override def getLinearVelocityX: Float = this.entityBody.getBody.getLinearVelocity.x

  override def getPreviousState: State = this.previousState

  override def setState(state: State): Unit = {
    this.previousState = state
    this.state = state
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
}

package model.entities

import com.badlogic.gdx.physics.box2d.Body
import controller.GameEvent
import controller.GameEvent.GameEvent
import model.entities.State.State
import model.helpers.EntitiesFactoryImpl

trait Hero extends LivingEntity {

  def setCommand(command: GameEvent)

  def updatePreviousState(state: State)
  def getPreviousState: State
  def getLinearVelocityX: Float

  def setSliding(slide: Boolean)
  def isSliding: Boolean

  def setBody(body: Body)
}

class HeroImpl(private var body: Body, private val size: (Float, Float)) extends LivingEntityImpl(body, size) with Hero{

  private var sliding: Boolean = false
  private var previousState: State = State.Standing
  private var isLittle: Boolean = false

  override def setBody(body: Body): Unit = this.body = body

  override def setCommand(command: GameEvent): Unit = command match {
    case GameEvent.Jump | GameEvent.MoveRight | GameEvent.MoveLeft | GameEvent.Slide => move(command)
    case GameEvent.Crouch => {
      this.stopMovement()
      this.isLittle = true
      //EntitiesFactoryImpl.defineSlidingHero(this)
      this.state = State.Crouch
    }
    case _ => throw new UnsupportedOperationException
  }

  def move(command: GameEvent): Unit = {
    if(this.movementStrategy != null)
      this.movementStrategy.apply(command)
  }

  override def update(): Unit = {
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
  }

  override def getLinearVelocityX: Float = this.body.getLinearVelocity.x

  override def getPreviousState: State = this.previousState

  def updatePreviousState(state: State): Unit = this.previousState = state

  override def setSliding(slide: Boolean): Unit = {
    this.sliding = slide
    if(slide) this.isLittle = true
  }

  override def isSliding: Boolean = this.sliding

  private def stopMovement(): Unit = {
    this.body.setLinearVelocity(0,0)
  }
}

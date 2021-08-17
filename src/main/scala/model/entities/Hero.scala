package model.entities

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import controller.GameEvent
import controller.GameEvent.GameEvent
import model.entities.State.State

trait Hero {

  def setCommand(command: GameEvent)

  def updatePreviousState(state: State)
  def getPreviousState(): State
  def getLinearVelocityX(): Float
}

class HeroImpl(private var body: Body, private val size: (Float, Float)) extends LivingEntityImpl(body, size) with Hero{

  private var isFacingRight: Boolean = true
  private var isSliding: Boolean = false

  override def setCommand(command: GameEvent): Unit = {

    if(!isSliding) {
      command match {
        case GameEvent.Jump => if(this.state != State.Falling) jump()
        case GameEvent.MoveRight => moveRight()
        case GameEvent.MoveLeft => moveLeft()
        case GameEvent.Slide => if(this.state != State.Jumping && this.state != State.Falling) {
          println(state)
          slide()
        }
      }
    }

  }

  private def jump(): Unit = {
    this.body.applyLinearImpulse(vectorScalar(new Vector2(0, 400f)), this.body.getWorldCenter, true)
  }

  private def moveRight(): Unit = {
    if (this.body.getLinearVelocity.x <= 2) {
      this.body.applyLinearImpulse(vectorScalar(new Vector2(60f, 0)), this.body.getWorldCenter, true)
    }
    this.isFacingRight = true
  }

  private def moveLeft(): Unit = {
    if (this.body.getLinearVelocity.x >= -2) {
      this.body.applyLinearImpulse(vectorScalar(new Vector2(-60f, 0)), this.body.getWorldCenter, true)
    }
    this.isFacingRight = false
  }

  private def slide(): Unit = {
    this.stopMovement()
    if (isFacingRight && this.body.getLinearVelocity.x <= 4) {
      this.body.applyLinearImpulse(vectorScalar(new Vector2(200f, 0)), this.body.getWorldCenter, true)
    }
    if (!isFacingRight && this.body.getLinearVelocity.x >= -4) {
      this.body.applyLinearImpulse(vectorScalar(new Vector2(-200f, 0)), this.body.getWorldCenter, true)
    }
    this.isSliding = true
  }

  private def stopMovement() = {
    this.body.setLinearVelocity(0,0)
  }

  private var previousState: State = State.Standing

  override def update(): Unit = {
    if(this.body.getLinearVelocity.y != 0 || this.body.getLinearVelocity.x == 0)
      this.isSliding = false

    if((this.body.getLinearVelocity.x <= 1 && isSliding && isFacingRight) ||
        this.body.getLinearVelocity.x >= -1 && isSliding && !isFacingRight) {
      this.stopMovement()
      this.isSliding = false
    }

    if(this.body.getLinearVelocity.y > 0 || (this.body.getLinearVelocity.y < 0 && this.previousState == State.Jumping))
      this.state = State.Jumping
    else if(this.body.getLinearVelocity.y < 0)
      this.state = State.Falling
    else if(this.body.getLinearVelocity.x != 0 && !isSliding)
      this.state = State.Running
    else if(this.body.getLinearVelocity.x != 0 && isSliding)
      this.state = State.Sliding
    else
      this.state = State.Standing
  }

  override def getLinearVelocityX(): Float = this.body.getLinearVelocity.x

  override def getPreviousState(): State = this.previousState

  def updatePreviousState(state: State): Unit = this.previousState = state

}

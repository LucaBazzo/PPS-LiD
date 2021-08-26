package model.movement

import com.badlogic.gdx.math.Vector2
import controller.GameEvent
import controller.GameEvent.GameEvent
import model.entities.{Hero, State}

class HeroMovementStrategy(private val entity: Hero) extends MovementStrategy {

  override def apply(command: GameEvent): Unit = {
    if(checkCommand(command)) {
      command match {
        case GameEvent.Jump => jump()
        case GameEvent.MoveRight => moveRight()
        case GameEvent.MoveLeft => moveLeft()
        case GameEvent.Slide => slide()
      }
    }
  }

  override def stopMovement(): Unit = this.entity.getBody.setLinearVelocity(0,0)

  private def checkCommand(command: GameEvent): Boolean = {
    if(entity.getState != State.Sliding && entity.getState != State.Attack01 &&
      entity.getState != State.Attack02 && entity.getState != State.Attack03) {
      command match {
        case GameEvent.Jump => return entity.getState != State.Falling &&
          entity.getState != State.Somersault && entity.getState != State.Crouch
        case GameEvent.MoveRight | GameEvent.MoveLeft => return true
        case GameEvent.Slide => return entity.getState != State.Jumping && entity.getState != State.Falling && entity.getState != State.Somersault
        case _ => throw new UnsupportedOperationException
      }
    }
    false
  }

  private def jump(): Unit = {
    this.applyLinearImpulse(new Vector2(0, 400f))
    if(this.entity.getState == State.Jumping)
      this.entity.setState(State.Somersault)
    else
      this.entity.setState(State.Jumping)
  }

  private def moveRight(): Unit = {
    if(entity.getState != State.Crouch) {
      if (entity.getBody.getLinearVelocity.x <= 3.5f) {
        this.applyLinearImpulse(new Vector2(60f, 0))
      }

      if(this.entity.getState == State.Standing)
        this.entity.setState(State.Running)
    }
    entity.setFacing(right = true)
  }

  private def moveLeft(): Unit = {
    if(entity.getState != State.Crouch) {
      if (entity.getBody.getLinearVelocity.x >= -3.5f) {
        this.applyLinearImpulse(new Vector2(-60f, 0))
      }

      if(this.entity.getState == State.Standing)
        this.entity.setState(State.Running)
    }
    entity.setFacing(right = false)
  }

  private def slide(): Unit = {
    this.entity.stopMovement()

    if(entity.getState != State.Crouch) {
      this.entity.changeHeroFixture((0.85f, 0.9f), (0, -0.5f))
      this.entity.setLittle(true)
    }

    if (entity.isFacingRight) {
      this.applyLinearImpulse(new Vector2(350f, 0))
    }
    else {
      this.applyLinearImpulse(new Vector2(-350f, 0))
    }

    this.entity.setState(State.Sliding)
  }

  private def applyLinearImpulse(vector: Vector2): Unit =
    entity.getBody.applyLinearImpulse(entity.vectorScalar(vector), entity.getBody.getWorldCenter, true)

  override def apply(): Unit = {}
}


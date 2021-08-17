package model

import com.badlogic.gdx.math.Vector2
import controller.GameEvent
import controller.GameEvent.GameEvent
import model.entities.{Hero, State}

trait MovementStrategy {

  def apply(command: GameEvent)
}

class HeroMovementStrategy(private val entity: Hero) extends MovementStrategy {

  override def apply(command: GameEvent): Unit = {
    if(!entity.isSliding) {
      command match {
        case GameEvent.Jump => if(entity.getState != State.Falling) jump()
        case GameEvent.MoveRight => moveRight()
        case GameEvent.MoveLeft => moveLeft()
        case GameEvent.Slide => if(entity.getState != State.Jumping && entity.getState != State.Falling) slide()
        case _ => throw new UnsupportedOperationException
      }
    }
  }

  private def jump(): Unit = {
    entity.getBody.applyLinearImpulse(entity.vectorScalar(new Vector2(0, 400f)), entity.getBody.getWorldCenter, true)
  }

  private def moveRight(): Unit = {
    if (entity.getBody.getLinearVelocity.x <= 2) {
      entity.getBody.applyLinearImpulse(entity.vectorScalar(new Vector2(60f, 0)), entity.getBody.getWorldCenter, true)
    }
    entity.setFacing(right = true)
  }

  private def moveLeft(): Unit = {
    if (entity.getBody.getLinearVelocity.x >= -2) {
      entity.getBody.applyLinearImpulse(entity.vectorScalar(new Vector2(-60f, 0)), entity.getBody.getWorldCenter, true)
    }
    entity.setFacing(right = false)
  }

  private def slide(): Unit = {
    this.stopMovement()
    if (entity.isFacingRight && entity.getBody.getLinearVelocity.x <= 4) {
      entity.getBody.applyLinearImpulse(entity.vectorScalar(new Vector2(200f, 0)), entity.getBody.getWorldCenter, true)
    }
    if (!entity.isFacingRight && entity.getBody.getLinearVelocity.x >= -4) {
      entity.getBody.applyLinearImpulse(entity.vectorScalar(new Vector2(-200f, 0)), entity.getBody.getWorldCenter, true)
    }
    entity.setSliding(true)
  }

  //TODO temporaneo
  private def stopMovement(): Unit = {
    entity.getBody.setLinearVelocity(0,0)
  }
}

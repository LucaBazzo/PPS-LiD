package model

import controller.GameEvent
import controller.GameEvent.GameEvent
import model.entities.{HeroImpl, State}

trait AttackStrategy {

  def apply(command: GameEvent)
}

class HeroAttackStrategy(private val entity: HeroImpl) extends AttackStrategy {

  override def apply(command: GameEvent): Unit = {
    if(checkCommand(command)) {
      command match {
        case GameEvent.Attack => attack()
      }
    }
  }

  private def checkCommand(command: GameEvent): Boolean = {
    command match {
      case GameEvent.Attack => return entity.getState != State.Falling &&
        entity.getState != State.Jumping && entity.getState != State.Crouch && entity.getState != State.Sliding
      case _ => throw new UnsupportedOperationException
    }
    false
  }

  private def attack(): Unit = {
    this.entity.stopMovement()
    if(this.entity.getState == State.Attack01 && this.entity.attackTimer < 75) {
      this.entity.setState(State.Attack02)
      this.entity.attackTimer = 140
    }
    else if(this.entity.getState == State.Attack02 && this.entity.attackTimer < 75) {
      this.entity.setState(State.Attack03)
      this.entity.attackTimer = 140
    }
    else if (this.entity.getState != State.Attack02 && this.entity.getState != State.Attack03
      && this.entity.attackTimer <= 0) {
      this.entity.setState(State.Attack01)
    }
  }
}

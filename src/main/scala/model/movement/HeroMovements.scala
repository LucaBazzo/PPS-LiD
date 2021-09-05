package model.movement

import com.badlogic.gdx.physics.box2d.Body
import controller.GameEvent
import controller.GameEvent.GameEvent
import model.collisions.ImplicitConversions.RichFloat
import model.entities.{Hero, State}
import utils.HeroConstants._

class HeroMovementStrategy(private val entity: Hero, private var speed: Float) extends MovementStrategy {

  private val jumpForce: Float = 7500f.PPM * 5
  private val runningForce: Float = 1000f.PPM * 5
  private val slidingForce: Float = 6000f.PPM * 10
  private val maxRunningVelocity: Float = 35f.PPM * 10

  override def apply(command: GameEvent): Unit = {
    if(this.checkState && checkCommand(command)) {
      command match {
        case GameEvent.Up => this.jump()
        case GameEvent.MoveRight => this.moveRight()
        case GameEvent.MoveLeft => this.moveLeft()
        case GameEvent.Slide => this.slide()
        case GameEvent.Down => this.crouch()
        case GameEvent.DownReleased => this.entity.setState(State.Standing)
      }
    }
  }

  override def stopMovement(): Unit = {
    this.entity.getBody.setLinearVelocity(0,0)
    if(this.entity.getFeet.nonEmpty)
      this.entity.getFeet.get.getBody.setLinearVelocity(0,0)
  }

  private def checkCommand(command: GameEvent): Boolean = command match {
    case GameEvent.Up => entity.getState != State.Falling &&
      entity.getState != State.Somersault && entity.getState != State.Crouch
    case GameEvent.MoveRight | GameEvent.MoveLeft => true
    case GameEvent.Down => entity.getState == State.Running ||
      entity.getState == State.Standing
    case GameEvent.DownReleased => entity.getState == State.Crouch
    case GameEvent.Slide => entity.getState != State.Jumping && entity.getState != State.Falling && entity.getState != State.Somersault
    case GameEvent.UpReleased => false
    case _ => throw new UnsupportedOperationException
  }

  private def jump(): Unit = {
    this.setFixedVelocityY(this.entity.getBody, JUMP_VELOCITY)
    if(this.entity.getState == State.Jumping)
      this.entity.setState(State.Somersault)
    else
      this.entity.setState(State.Jumping)
  }

  private def moveRight(): Unit = {
    this.move(RUN_VELOCITY)
    entity.setFacing(right = true)
  }

  private def moveLeft(): Unit = {
    this.move(-RUN_VELOCITY)
    entity.setFacing(right = false)
  }

  private def move(runVelocity: Float) {
    if(entity.getState != State.Crouch) {
      if(this.entity.isTouchingGround || (!this.entity.isTouchingGround && !this.entity.isColliding)) {
        this.setVelocityX(this.entity.getBody, runVelocity)

        if(this.entity.getState == State.Standing)
          this.entity.setState(State.Running)
      }
    }
  }

  private def crouch(): Unit = {
    this.stopMovement()
    entity.changeHeroFixture(HERO_SIZE_SMALL, CROUCH_OFFSET)
    entity.setState(State.Crouch)
    entity.setLittle(true)
  }

  private def slide(): Unit = {
    this.entity.stopMovement()

    if(entity.getState != State.Crouch) {
      this.entity.changeHeroFixture(HERO_SIZE_SMALL, CROUCH_OFFSET)
      this.entity.setLittle(true)
    }

    if (entity.isFacingRight) {
      this.setVelocityX(this.entity.getBody, SLIDE_VELOCITY)
      this.setVelocityX(this.entity.getFeet.get.getBody, SLIDE_VELOCITY)
    }
    else {
      this.setVelocityX(this.entity.getBody, -SLIDE_VELOCITY)
      this.setVelocityX(this.entity.getFeet.get.getBody, -SLIDE_VELOCITY)
    }

    this.entity.setState(State.Sliding)
  }

  override def alterSpeed(alteration: Float): Unit = this.speed += alteration

  override def apply(): Unit = ???

  private def checkState: Boolean = entity.getState match {
    case State.Sliding | State.Attack01 | State.Attack02
      | State.Attack03 | State.BowAttack | State.Hurt | State.ItemPicked => false
    case _ => true
  }

  private def setVelocityX(body: Body, velocity: Float): Unit =
    body.setLinearVelocity(velocity * speed, body.getLinearVelocity.y)

  private def setVelocityY(body: Body, velocity: Float): Unit =
    body.setLinearVelocity(body.getLinearVelocity.x, velocity * speed)

  private def setFixedVelocityY(body: Body, velocity: Float): Unit =
    body.setLinearVelocity(body.getLinearVelocity.x, velocity)
}

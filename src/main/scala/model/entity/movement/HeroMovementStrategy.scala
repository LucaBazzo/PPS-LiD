package model.entity.movement

import alice.tuprolog.{SolveInfo, Term}
import controller.GameEvent
import controller.GameEvent.GameEvent
import model.entity.State._
import model.entity.{Hero, State}
import utils.HeroConstants._
import utils.Scala2P._

/** Implementation of the normal Hero Movement Strategy
 *
 *  @constructor the main hero movement strategy
 *  @param entity the entity that will be moved in the world
 *  @param speed a multiplier to the running velocity of the hero
 */
case class HeroMovementStrategy(private val entity: Hero,
                                private var speed: Float) extends MovementStrategy {

  private val engine: Term => Iterable[SolveInfo] = mkPrologEngine("""
    checkUp(X):-(X \= state(falling)),(X \= state(somersault)),(X \= state(crouching)).
    checkDown(state(standing)).
    checkDown(state(running)).
    checkDownRelease(state(crouching)).
    checkLeftAndRight(_):-true.
    checkSlide(X):-(X \= state(jumping)),(X \= state(falling)),(X \= state(somersault)).

    checkCommand(C, S) :-
      (C=command(up) -> call(checkUp(S)));
      (C=command(down) -> call(checkDown(S)));
      (C=command(downreleased) -> call(checkDownRelease(S)));
      (C=command(moveleft) -> call(checkLeftAndRight(_)));
      (C=command(moveright) -> call(checkLeftAndRight(_)));
      (C=command(slide) -> call(checkSlide(S))).
  """)

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
    this.entity.setVelocity((0,0))
    if(this.entity.getFeet.nonEmpty)
      this.entity.getFeet.get.setVelocity((0,0))
  }

  override def alterSpeed(alteration: Float): Unit = this.speed += alteration

  private def checkCommand(command: GameEvent): Boolean = {
    val goal: String = "checkCommand(command(" + command.toString.toLowerCase() + "), " +
      "state(" + entity.getState.toString.toLowerCase() + "))"

    solveWithSuccess(engine, goal)
  }

  private def jump(): Unit = {
    this.entity.setVelocityY(JUMP_VELOCITY)
    if(this.entity is Jumping)
      this.entity.setState(State.Somersault)
    else
      this.entity.setState(State.Jumping)
  }

  private def moveRight(): Unit = {
    this.move(RUN_VELOCITY, right = true)
  }

  private def moveLeft(): Unit = {
    this.move(-RUN_VELOCITY, right = false)
  }

  private def move(runVelocity: Float, right: Boolean): Unit = {
    if(entity isNot Crouching) {
      if(canMove(right)) {
        this.entity.setVelocityX(runVelocity, this.speed)

        if(this.entity is Standing)
          this.entity.setState(State.Running)
      }
    }

    this.entity.setFacing(right = right)
  }

  private def crouch(): Unit = {
    this.stopMovement()
    entity.setLittle(true)
    Hero.changeHeroSize(this.entity, HERO_SIZE_SMALL)
    entity.setState(State.Crouching)
  }

  private def slide(): Unit = {
    this.entity.stopMovement()

    if(entity isNot Crouching) {
      this.entity.setLittle(true)
      Hero.changeHeroSize(this.entity, HERO_SIZE_SMALL)
    }

    if (entity.isFacingRight) {
      this.entity.setVelocityX(SLIDE_VELOCITY)
      this.entity.getFeet.get.setVelocityX(SLIDE_VELOCITY)
    }
    else {
      this.entity.setVelocityX(-SLIDE_VELOCITY)
      this.entity.getFeet.get.setVelocityX(-SLIDE_VELOCITY)
    }

    this.entity.setState(State.Sliding)
  }

  private def checkState: Boolean = entity.getState match {
    case State.Sliding | State.Attack01 | State.Attack02
         | State.Attack03 | State.BowAttacking | State.Hurt | State.PickingItem => false
    case _ => true
  }

  private def canMove(right: Boolean): Boolean =
    this.entity.isTouchingGround || (right && !this.entity.isTouchingWallOnSide(right)) ||
    (!right && !this.entity.isTouchingWallOnSide(right))
}

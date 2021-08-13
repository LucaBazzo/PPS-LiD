package model.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import model.entities.State.State

object State extends Enumeration {
  type State = Value
  val Standing, Running, Jumping, Falling = Value
}

trait Entity {

  def update()
  def getState(): State
  def setPosition(position: (Float, Float))
  def getPosition: (Float, Float)
  def getSize: (Float, Float)
  def setCollisionStrategy()
  def destroyEntity()

  //TODO vedere dove metterlo
  def vectorScalar(vector: Vector2, scalar: Float = Gdx.graphics.getDeltaTime) = new Vector2(vector.x * scalar, vector.y * scalar)
}

abstract class EntityImpl(private var body: Body, private val size: (Float, Float)) extends Entity {

  protected var state: State = State.Standing

  def update(): Unit

  override def getState(): State = this.state

  override def setPosition(position: (Float, Float)): Unit = {
    this.body.setTransform(new Vector2(position._1, position._2), 0)
  }

  override def getPosition: (Float, Float) = (this.body.getPosition.x, this.body.getPosition.y)

  override def getSize: (Float, Float) = this.size

  override def setCollisionStrategy(): Unit = ???

  override def destroyEntity(): Unit = ???
}

trait MobileEntity {

  def setMovementStrategy()
  def move()
  def getDirection()
}

class MobileEntityImpl(private var body: Body, private val size: (Float, Float)) extends EntityImpl(body, size) with MobileEntity {

  override def update(): Unit = this.move()

  override def setMovementStrategy(): Unit = ???

  override def move(): Unit = this.body.applyLinearImpulse(vectorScalar(new Vector2(0, 400f)), this.body.getWorldCenter, true)

  override def getDirection(): Unit = ???
}

trait Hero {

  def setCommand(command: Int)

  def updatePreviousState(state: State)
  def getPreviousState(): State
  def getLinearVelocityX(): Float
}

class HeroImpl(private var body: Body, private val size: (Float, Float)) extends MobileEntityImpl(body, size) with Hero{

  override def setCommand(command: Int): Unit = command match {
    case 0 => jump()
    case 1 => moveRight()
    case 2 => moveLeft()
  }

  private def jump(): Unit = {
    this.body.applyLinearImpulse(vectorScalar(new Vector2(0, 400f)), this.body.getWorldCenter, true)
  }

  private def moveRight(): Unit = {
    if (this.body.getLinearVelocity.x <= 2) {
      this.body.applyLinearImpulse(vectorScalar(new Vector2(60f, 0)), this.body.getWorldCenter, true)
    }
  }

  private def moveLeft(): Unit = {
    if (this.body.getLinearVelocity.x >= -2) {
      this.body.applyLinearImpulse(vectorScalar(new Vector2(-60f, 0)), this.body.getWorldCenter, true)
    }
  }

  private var previousState: State = State.Standing

  override def update(): Unit = {
    if(this.body.getLinearVelocity.y > 0 || (this.body.getLinearVelocity.y < 0 && this.previousState == State.Jumping))
      this.state = State.Jumping
    else if(this.body.getLinearVelocity.y < 0)
      this.state = State.Falling
    else if(this.body.getLinearVelocity.x != 0)
      this.state = State.Running
    else
      this.state = State.Standing
  }

  override def getLinearVelocityX(): Float = this.body.getLinearVelocity.x

  override def getPreviousState(): State = this.previousState

  def updatePreviousState(state: State): Unit = this.previousState = state
}


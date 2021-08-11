package model.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body

trait Entity {

  def update()
  def getState()
  def setPosition(position: (Float, Float))
  def getPosition: (Float, Float)
  def setCollisionStrategy()
  def destroyEntity()

  //TODO vedere dove metterlo
  def vectorScalar(vector: Vector2, scalar: Float = Gdx.graphics.getDeltaTime) = new Vector2(vector.x * scalar, vector.y * scalar)
}

abstract class EntityImpl(private var body: Body) extends Entity {
   def update(): Unit

   override def getState(): Unit = ???

   override def setPosition(position: (Float, Float)): Unit = {
     this.body.setTransform(new Vector2(position._1, position._2), 0)
   }

  override def getPosition: (Float, Float) = (this.body.getPosition.x, this.body.getPosition.y)

   override def setCollisionStrategy(): Unit = ???

   override def destroyEntity(): Unit = ???
}

trait MobileEntity {

  def setMovementStrategy()
  def move()
  def getDirection()
}

class MobileEntityImpl(private var body: Body) extends EntityImpl(body) with MobileEntity {

  override def update(): Unit = this.move()

  override def setMovementStrategy(): Unit = ???

  override def move(): Unit = this.body.applyLinearImpulse(vectorScalar(new Vector2(0, 400f)), this.body.getWorldCenter, true)

  override def getDirection(): Unit = ???
}

trait Hero {

  def setCommand(command: Int)
}

class HeroImpl(private var body: Body) extends MobileEntityImpl(body) with Hero {

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
}


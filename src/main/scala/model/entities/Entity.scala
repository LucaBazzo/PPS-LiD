package model.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import model.collisions.CollisionStrategy
import model.entities.State.State

object State extends Enumeration {
  type State = Value
  val Standing, Running, Jumping, Falling = Value
}

trait Entity {

  def update()
  def getState: State
  def setPosition(position: (Float, Float))
  def getPosition: (Float, Float)
  def getSize: (Float, Float)
  def setCollisionStrategy(collisionStrategy: CollisionStrategy)
  def collisionDetected(entity: Entity)
  def destroyEntity()

  //TODO ricontrollare in futuro
  def getBody: Body

  //TODO vedere dove metterlo
  def vectorScalar(vector: Vector2, scalar: Float = Gdx.graphics.getDeltaTime) = new Vector2(vector.x * scalar, vector.y * scalar)
}

abstract class EntityImpl(private var body: Body, private val size: (Float, Float)) extends Entity {

  protected var state: State = State.Standing
  private var collisionStrategy: CollisionStrategy = _

  def update(): Unit

  override def getState: State = this.state

  override def setPosition(position: (Float, Float)): Unit = {
    this.body.setTransform(new Vector2(position._1, position._2), 0)
  }

  override def getPosition: (Float, Float) = (this.body.getPosition.x, this.body.getPosition.y)

  override def getSize: (Float, Float) = this.size

  override def setCollisionStrategy(collisionStrategy: CollisionStrategy): Unit = this.collisionStrategy = collisionStrategy

  override def collisionDetected(entity: Entity): Unit = {
    if(this.collisionStrategy != null) this.collisionStrategy.apply(entity)
  }

  override def destroyEntity(): Unit = ???

  override def getBody: Body = this.body
}

case class ImmobileEntity(private var body: Body, private val size: (Float, Float)) extends EntityImpl(body, size) {
  override def update(): Unit = {}
}
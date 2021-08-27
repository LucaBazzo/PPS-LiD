package model.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import model.EntityBody
import model.collisions.{CollisionStrategy, DoNothingOnCollision}
import model.entities.State.State
import model.helpers.EntitiesFactoryImpl

object State extends Enumeration {
  type State = Value
  val Standing, Crouch, Sliding,
      Running, Jumping, Falling, Somersault,
      Attack01, Attack02, Attack03, Dying = Value
}

trait Entity {

  def update()

  def getState: State

  def setPosition(position: (Float, Float))

  def getPosition: (Float, Float)

  def getSize: (Float, Float)

  def setCollisionStrategy(collisionStrategy: CollisionStrategy)

  def collisionDetected(entity: Entity)

  //TODO ricontrollare in futuro
  def getBody: Body

  //TODO vedere dove metterlo
  def vectorScalar(vector: Vector2, scalar: Float = Gdx.graphics.getDeltaTime) = new Vector2(vector.x * scalar, vector.y * scalar)

  def destroyEntity(): Unit
}

abstract class EntityImpl(private var entityBody: EntityBody, private val size: (Float, Float)) extends Entity {

  protected var state: State = State.Standing
  protected var collisionStrategy: CollisionStrategy = new DoNothingOnCollision()

  override def getState: State = this.state

  override def setPosition(position: (Float, Float)): Unit = {
    this.entityBody.getBody.setTransform(new Vector2(position._1, position._2), 0)
  }

  override def getPosition: (Float, Float) = (this.entityBody.getBody.getPosition.x, this.entityBody.getBody.getPosition.y)

  override def getSize: (Float, Float) = this.size

  override def setCollisionStrategy(collisionStrategy: CollisionStrategy): Unit =
    this.collisionStrategy = collisionStrategy

  override def collisionDetected(entity: Entity): Unit = {
    this.collisionStrategy.apply(entity)
  }

  override def destroyEntity(): Unit = {
    EntitiesFactoryImpl.destroyBody(this.getBody)
    this.getBody.getJointList.toArray().foreach(j => {
      EntitiesFactoryImpl.destroyJoint(j.joint)
      EntitiesFactoryImpl.destroyBody(j.other)
    })
    EntitiesFactoryImpl.removeEntity(this)
  }

  override def getBody: Body = this.entityBody.getBody
}

case class ImmobileEntity(private var entityBody: EntityBody, private val size: (Float, Float)) extends EntityImpl(entityBody, size) {
  override def update(): Unit = {}
}
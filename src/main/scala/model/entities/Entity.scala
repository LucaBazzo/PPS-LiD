package model.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import model.EntityBody
import model.collisions.{CollisionStrategy, DoNothingOnCollision}
import model.entities.EntityType.EntityType
import model.entities.State.State
import model.helpers.EntitiesFactoryImpl

object State extends Enumeration {
  type State = Value
  val Standing, Crouch, Sliding,
      Running, Jumping, Falling, Somersault,
      LadderClimb, LadderDescend, LadderIdle,
      Attack01, Attack02, Attack03, BowAttack,
      Dying, Hurt, ItemPicked, Opening = Value
}

object EntityType extends Enumeration {
  type EntityType = Value
  val Hero,
      Mobile, Immobile, Enemy, //this values will not show any sprite
      Arrow, ArmorItem, CakeItem, BootsItem, ShieldItem, MapItem, WrenchItem, KeyItem,
      SmallPotionItem, PotionItem, LargePotionItem, HugePotionItem, SkeletonKeyItem, BowItem, BFSwordItem,
      EnemySkeleton, EnemySlime, EnemyWorm, Platform, Door,
      AttackFireBall, AttackArrow = Value
}

trait Entity {

  def update()

  def getType: EntityType

  def getState: State

  def setState(state:State): Unit

  def setPosition(position: (Float, Float))

  def getPosition: (Float, Float)

  def getSize: (Float, Float)

  def setCollisionStrategy(collisionStrategy: CollisionStrategy)

  def collisionDetected(entity: Entity)

  def setEndCollisionStrategy(endCollisionStrategy: CollisionStrategy)

  def collisionEnded(entity: Entity)

  //TODO ricontrollare in futuro
  def getBody: Body
  def getEntityBody: EntityBody

  //TODO vedere dove metterlo
  def vectorScalar(vector: Vector2, scalar: Float = Gdx.graphics.getDeltaTime) = new Vector2(vector.x * scalar, vector.y * scalar)

  def destroyEntity(): Unit

  def changeCollisions(entityType: Short): Unit
}

abstract class EntityImpl(private val entityType: EntityType,
                          private var entityBody: EntityBody,
                          private val size: (Float, Float)) extends Entity {

  protected var state: State = State.Standing
  protected var collisionStrategy: CollisionStrategy = new DoNothingOnCollision()
  protected var endCollisionStrategy: CollisionStrategy = new DoNothingOnCollision()

  override def getState: State = this.state

  override def setState(state:State):Unit = this.state = state

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

  override def setEndCollisionStrategy(endCollisionStrategy: CollisionStrategy): Unit =
    this.endCollisionStrategy = endCollisionStrategy

  override def collisionEnded(entity: Entity): Unit =
    this.endCollisionStrategy.apply(entity)

  override def destroyEntity(): Unit = {
    EntitiesFactoryImpl.destroyBody(this.getBody)
    this.getBody.getJointList.toArray().foreach(j => {
      // TODO: to be removed
//      EntitiesFactoryImpl.destroyJoint(j.joint)
      EntitiesFactoryImpl.destroyBody(j.other)
    })
    EntitiesFactoryImpl.removeEntity(this)
  }

  override def getBody: Body = this.entityBody.getBody

  override def getEntityBody: EntityBody = this.entityBody

  override def changeCollisions(entityType: Short): Unit = EntitiesFactoryImpl.changeCollisions(this, entityType)

  override def getType: EntityType = this.entityType
}

case class ImmobileEntity(private var entityType: EntityType,
                          private var entityBody: EntityBody,
                          private val size: (Float, Float))
  extends EntityImpl(entityType, entityBody, size) {

  override def update(): Unit = {}
}
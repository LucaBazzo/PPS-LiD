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
  val Standing, Crouching, Sliding,
      Running, Jumping, Falling, Somersault,
      LadderClimbing, LadderDescending, LadderIdle,
      Attack01, Attack02, Attack03, BowAttacking,
      Dying, Hurt, pickingItem, Opening = Value
}

object EntityType extends Enumeration {
  type EntityType = Value
  val Hero,
      Mobile, Immobile, Enemy, SpawnZone, //this values will not show any sprite
      Arrow, ArmorItem, CakeItem, BootsItem, ShieldItem, MapItem, WrenchItem, KeyItem,
      SmallPotionItem, PotionItem, LargePotionItem, HugePotionItem, SkeletonKeyItem, BowItem, BFSwordItem,

      EnemySkeleton, EnemySlime, EnemyWorm, EnemyBossWizard, EnemyBossReaper, // EnemyGhost
      Platform, Door, Ladder, Water, Lava, Chest,
      AttackFireBall, AttackSmite, AttackArrow = Value
}

trait Entity {

  def update(): Unit

  def getType: EntityType

  def getState: State

  def setState(state:State): Unit

  def is(state: State): Boolean

  def isNot(state: State): Boolean

  def setPosition(position: (Float, Float)): Unit

  def getPosition: (Float, Float)

  def setSize(size: (Float, Float))

  def getSize: (Float, Float)

  def setCollisionStrategy(collisionStrategy: CollisionStrategy): Unit

  def collisionDetected(entity: Option[Entity]): Unit

  def collisionReleased(entity: Option[Entity]): Unit

  //TODO ricontrollare in futuro
  def getBody: Body
  def getEntityBody: EntityBody

  //TODO vedere dove metterlo
  def vectorScalar(vector: Vector2, scalar: Float = Gdx.graphics.getDeltaTime) = new Vector2(vector.x * scalar, vector.y * scalar)

  def destroyEntity(): Unit

  def changeCollisions(entityType: Short): Unit

  def isColliding: Boolean
}

abstract class EntityImpl(private val entityType: EntityType,
                          private var entityBody: EntityBody,
                          private var size: (Float, Float)) extends Entity {

  protected var state: State = State.Standing
  protected var collisionStrategy: CollisionStrategy = new DoNothingOnCollision()
  private var collidingEntities: Int = 0

  override def getState: State = this.state

  override def setState(state: State): Unit = this.state = state

  override def is(state: State): Boolean = this.state equals state

  override def isNot(state: State): Boolean = !(this is state)

  override def setPosition(position: (Float, Float)): Unit = this.entityBody.setPosition(position)

  override def getPosition: (Float, Float) = (this.entityBody.getBody.getPosition.x, this.entityBody.getBody.getPosition.y)

  override def setSize(size: (Float, Float)): Unit = this.size = size

  override def getSize: (Float, Float) = this.size

  override def setCollisionStrategy(collisionStrategy: CollisionStrategy): Unit =
    this.collisionStrategy = collisionStrategy

  override def collisionDetected(entity: Option[Entity]): Unit = {
    if(entity.nonEmpty)
      this.collisionStrategy.apply(entity.get)
    this.collidingEntities += 1
  }

  override def collisionReleased(entity: Option[Entity]): Unit = {
    if(entity.nonEmpty)
      this.collisionStrategy.release(entity.get)
    this.collidingEntities -= 1
  }

  override def isColliding: Boolean = this.collidingEntities > 0

  override def destroyEntity(): Unit = {
    EntitiesFactoryImpl.destroyBody(this.getBody)
    this.getBody.getJointList.toArray().foreach(j => {
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
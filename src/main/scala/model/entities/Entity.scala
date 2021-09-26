package model.entities

import com.badlogic.gdx.physics.box2d.Body
import model.EntityBody
import model.collisions.{CollisionStrategy, DoNothingCollisionStrategy}
import model.entities.EntityType.EntityType
import model.entities.State.State
import model.helpers.EntitiesFactoryImpl.{pendingChangeCollisions, pendingDestroyBody, removeEntity}

object State extends Enumeration {
  type State = Value
  val Standing, Crouching, Sliding,
      Running, Jumping, Falling, Somersault,
      LadderClimbing, LadderDescending, LadderIdle,
      Attack01, Attack02, Attack03, BowAttacking, AirDownAttacking, AirDownAttackingEnd,
      Dying, Hurt, pickingItem, Opening, Closed = Value
}

object EntityType extends Enumeration {
  type EntityType = Value
  val Hero,
      Mobile, Immobile, Enemy, SpawnZone, //this values will not show any sprite
      Arrow, ArmorItem, CakeItem, BootsItem, ShieldItem, MapItem, WrenchItem, KeyItem,
      SmallPotionItem, PotionItem, LargePotionItem, HugePotionItem, SkeletonKeyItem, BowItem, BFSwordItem,
      EnemySkeleton, EnemySlime, EnemyPacman, EnemyWorm, EnemyBat, EnemyBossWizard,
      Platform, PlatformSensor, Door, Ladder, Water, Lava, Chest, Portal,
      AttackFireBall, AttackEnergyBall, AttackArrow = Value
}

trait Entity {

  def update(): Unit

  def getType: EntityType

  def getState: State

  def setState(state:State): Unit

  def is(state: State): Boolean = this.getState equals state

  def isNot(state: State): Boolean = !(this is state)

  def setPosition(position: (Float, Float)): Unit = this.getEntityBody.setPosition(position)

  def getPosition: (Float, Float) = (this.getBody.getPosition.x, this.getBody.getPosition.y)

  def setSize(size: (Float, Float)): Unit

  def getSize: (Float, Float)

  def setCollisionStrategy(collisionStrategy: CollisionStrategy): Unit

  def collisionDetected(entity: Entity): Unit

  def collisionReleased(entity: Entity): Unit

  def getBody: Body

  def getEntityBody: EntityBody

  def destroyEntity(): Unit = {
    pendingDestroyBody(this.getBody)
    this.getBody.getJointList.toArray().foreach(joint => {
      pendingDestroyBody(joint.other)
    })
    removeEntity(this)
  }

  def changeCollisions(entityCollisionBit: Short): Unit = pendingChangeCollisions(this, entityCollisionBit)

  def isColliding: Boolean
}

abstract class EntityImpl(private val entityType: EntityType,
                          private var entityBody: EntityBody,
                          private var size: (Float, Float)) extends Entity {

  private var state: State = State.Standing
  private var collisionStrategy: CollisionStrategy = DoNothingCollisionStrategy()
  private var collidingEntities: Int = 0

  override def getState: State = this.state

  override def setState(state: State): Unit = this.state = state

  override def setSize(size: (Float, Float)): Unit = this.size = size

  override def getSize: (Float, Float) = this.size

  override def setCollisionStrategy(collisionStrategy: CollisionStrategy): Unit =
    this.collisionStrategy = collisionStrategy

  override def collisionDetected(entity: Entity): Unit = {
    this.collisionStrategy.contact(entity)
    this.collidingEntities += 1
  }

  override def collisionReleased(entity: Entity): Unit = {
    this.collisionStrategy.release(entity)
    this.collidingEntities -= 1
  }

  override def isColliding: Boolean = this.collidingEntities > 0

  override def getBody: Body = this.entityBody.getBody

  override def getEntityBody: EntityBody = this.entityBody

  override def getType: EntityType = this.entityType

  override def update(): Unit = {
    this.collisionStrategy.apply()
  }
}

package model.entity

import com.badlogic.gdx.physics.box2d.Body
import model.EntityBody
import model.entity.EntityType.EntityType
import model.entity.State.State
import model.entity.collision.{CollisionStrategy, DoNothingCollisionStrategy}
import model.helpers.EntitiesFactoryImpl.{pendingChangeCollisions, pendingDestroyBody, removeEntity}
import model.helpers.ImplicitConversions.vectorToTuple

/** All the possible states associated to model entities. An entity may define
 * a subset of those states. Each used state is needed both to distinguish
 * phases in the model and also to bind specific animations to specific entity
 * types in the view.
 *
 * The state may change in the life of an entity, especially for complex
 * game entities.
 *
 * @see [[model.entity.Entity]]
 * @see [[model.entity.EntityType]]
 */
object State extends Enumeration {
  type State = Value
  val Standing, Crouching, Sliding,
      Running, Jumping, Falling, Somersault,
      LadderClimbing, LadderDescending, LadderIdle,
      Attack01, Attack02, Attack03, BowAttacking, AirDownAttacking, AirDownAttackingEnd,
      Dying, Hurt, PickingItem, Opening, Closed = Value
}

/** Enumeration containing all the possible entities defined and used in the
 * game model. Each entity defines a single type and most of them defines a
 * corresponding sprite-sheet and animations. Moreover, an explicit type is
 * needed to distinguish model entities derived from the same class or object.
 *
 * Entities type does not change in the life of an entity.
 *
 * @see [[model.entity.Entity]]
 */
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

/** All possible entities are derived from this trait. Generically, an entity
 * is defined by a geometric body, a state, a type and a collision behaviour
 * to apply whenever the entity collides with another one.
 *
 */
trait Entity {

  /** Executes a single update of the entity instance. During this phase the
   * collision behaviour is checked and executed.
   */
  def update(): Unit

  /** Retrieves the type of the entity. An explicit type is needed to
   * distinguish model entities derived from the same class or object.
   *
   * Entities type does not change in the life of an entity.
   */
  def getType: EntityType

  /** Retrieves the current state of the entity. A state is needed both to
   * distinguish phases in the model and also to bind specific animations to
   * specific entity types in the view.
   *
   * @see [[model.entity.State]]
   *
   * @return the current state of the entity
   */
  def getState: State

  /** Set a new state to the entity. A state is needed to manage the execution
   * and prevent inconsistent behaviours.
   *
   * @see [[model.entity.State]]
   *
   * @param state the new state assigned to the entity
   */
  def setState(state:State): Unit

  /** Utility method to check the current state of an entity.
   *
   * @see [[model.entity.State]]
   *
   * @param state the expected entity state
   * @return a boolean value. If true, the entity state matches the provided
   *         one.
   */
  def is(state: State): Boolean = this.getState equals state

  /** Dual functinoality of "is". Utility method to check the current state of an entity.
   *
   * @see [[model.entity.Entity#is(scala.Enumeration.Value)]]
   *
   * @param state the unexpected entity state.
   * @return a boolean value. If true, the entity state does not match the
   *         provided one.
   */
  def isNot(state: State): Boolean = !(this is state)

  /** Set the absolute position of the entity's body. The new position may ignore
   * collisions with impenetrable bodies such as walls or closed doors.
   *
   * @param position Tuple2 representing the new entity position.
   */
  def setPosition(position: (Float, Float)): Unit = this.getEntityBody.setPosition(position)

  /** Retrieves the absolute position of the entity's body.
   *
   * @return Tuple2 representing the current entity position.
   */
  def getPosition: (Float, Float) = this.getBody.getPosition

  /** Set the size of the entity's body. The provided values meaning may vary
   * depending the shape of the entity. For rectangular shaped entities, the
   * size represents the new width and height while for spherical entities
   * it defined the ellipsis axes.
   *
   * This method does not support ChainShape, EdgeShape and PolygonShape for
   * polygons with a number of vertices different from 4.
   *
   * @see [[com.badlogic.gdx.physics.box2d.Shape]]
   *
   * @param size Tuple2 representing the sizes of the entity's body.
   */
  def setSize(size: (Float, Float)): Unit

  /** Retrieves the size of the entity's body. The returned values meaning may
   * vary depending the shape of the entity. For rectangular shaped entities,
   * the size represents the new width and height while for spherical entities
   * it defined the ellipsis axes.
   *
   * This method does not support ChainShape, EdgeShape and PolygonShape for
   * polygons with a number of vertices different from 4.
   *
   * @see [[com.badlogic.gdx.physics.box2d.Shape]]
   *
   * @return Tuple2 representing the sizes of the entity's body.
   */
  def getSize: (Float, Float)

  /** Defines the behaviour to execute when 2 bodies start, stop or are
   * colliding.
   *
   * @see [[model.entity.collision.CollisionStrategy]]
   *
   * @param collisionStrategy the collisionStrategy instance to apply
   */
  def setCollisionStrategy(collisionStrategy: CollisionStrategy): Unit

  /** Defines what to do when two bodies start colliding. This method should
   * use the provided collision strategy defined for the entity
   *
   * @see [[model.entity.collision.CollisionStrategy]]
   *
   * @param entity the Entity colliding with the current entity instance
   */
  def collisionDetected(entity: Entity): Unit

  /** Defines what to do when two bodies stop colliding. This method should
   * use the provided collision strategy defined for the entity
   *
   * @see [[model.entity.collision.CollisionStrategy]]
   *
   * @param entity the Entity colliding with the current entity instance
   */
  def collisionReleased(entity: Entity): Unit

  /** Modifies the entity's body collision category value. This value defines
   * which entities can collide and with whom. An entity category is
   * represented by a short numeric value define in a one-hot fashion.
   *
   * By combining many of those "bits" with a logic OR expression an
   * articulated mask can be defined.
   *
   * @see [[model.entity.collision.EntityCollisionBit]]
   *
   * @param entityCollisionBit a short numeric value defined in a one-hot
   *                           fashion
   */
  def changeCollisions(entityCollisionBit: Short): Unit = pendingChangeCollisions(this, entityCollisionBit)

  /** Checks if the entity is colliding with other entities.
   *
   * @return a boolean value. If true, the entity is colliding with other
   *         entities.
   */
  def isColliding: Boolean

  /** Retrieves the LibGDX body representing the current entity. A body may be
   * manipulated in various ways (moved, rotated, destroyed or reshaped).
   *
   * @see [[com.badlogic.gdx.physics.box2d.Body]]
   *
   * @return a LibGDX Body entity
   */
  def getBody: Body = getEntityBody.getBody

  /** Retrieves an encapsulated version of the LibGDX body representing the
   * current entity.
   *
   * @see [[model.entity.Entity#getBody()]]
   * @see [[com.badlogic.gdx.physics.box2d.Body]]
   *
   * @return a LibGDX Body entity
   */
  def getEntityBody: EntityBody

  /** Destroys the LibGDX body representing the current entity and its
   * optional joints. A joint is a physical binding between two bodies which
   * needs to be destroyed.
   *
   * As soon the body is destroyed, the entity stops to be considered in the
   * emulation of the game world.
   *
   * @see [[com.badlogic.gdx.physics.box2d.JointEdge]]
   * @see [[com.badlogic.gdx.physics.box2d.Body]]
   */
  def destroyEntity(): Unit = {
    pendingDestroyBody(this.getBody)
    this.getBody.getJointList.toArray().foreach(joint => {
      pendingDestroyBody(joint.other)
    })
    removeEntity(this)
  }
}

/**
 *
 * @param entityType
 * @param entityBody
 * @param size
 */
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

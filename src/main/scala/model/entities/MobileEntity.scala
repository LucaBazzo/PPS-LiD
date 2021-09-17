package model.entities

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import model.EntityBody
import model.collisions.ImplicitConversions._
import model.collisions.{ApplyDamage, ApplyDamageAndDestroyEntity, EntityCollisionBit}
import model.entities.EntityType.EntityType
import model.entities.Statistic.Statistic
import model.helpers.EntitiesFactoryImpl
import model.helpers.EntitiesFactoryImpl._
import model.movement.{ArrowMovementStrategy, CircularMovementStrategy, DoNothingMovementStrategy, MovementStrategy}
import utils.CollisionConstants.{ARROW_COLLISIONS, NO_COLLISIONS, SWORD_COLLISIONS}
import utils.HeroConstants.{ARROW_SIZE, PIVOT_SIZE, SWORD_ATTACK_DENSITY}

/** The possible statistics that could be given to a mobile or living entity
 *
 */
object Statistic extends Enumeration {
  type Statistic = Value

  val CurrentHealth, Health, Strength, Defence, MovementSpeed, MaxMovementSpeed, Acceleration, AttackSpeed = Value

  val VisionDistance, VisionAngle, AttackFrequency, AttackDuration = Value
}

/** An Entity that can move based on a Movement Strategy
 *
 */
trait MobileEntity extends Entity {

  /** Define the entity movement
   *
   *  @param strategy the behavior that will call with move()
   */
  def setMovementStrategy(strategy: MovementStrategy)

  /** Call the current movement strategy
   *
   */
  def move()

  /** Stop the entity movement based on what is defined in the movement strategy
   *
   */
  def stopMovement()

  /** Changes the direction in which the entity is looking
   *
   *  @param right true for right, false for left
   */
  def setFacing(right: Boolean)

  /** Check the direction in which the entity is looking
   *
   *  @return true if is facing to the right
   */
  def isFacingRight: Boolean

  /** Return the statistics of the Entity
   *
   *  @return a map with the statistic and it's value
   */
  def getStatistics: Map[Statistic, Float]

  /** Modifies a statistic based on the alteration value
   *
   *  @param statistic the statistic that will be altered
   *  @param alteration the value that will be added
   */
  def alterStatistics(statistic: Statistic, alteration: Float)

  /** Return a specific statistic value
   *
   *  @param statistic the statistic required
   *  @return a float if the statistic is present, Option.empty otherwise
   */
  def getStatistic(statistic: Statistic): Option[Float]

  /** Change the current velocity on x and y of this Entity
   *
   *  @param velocity the new velocity
   *  @param speed a multiplier to the velocity
   */
  def setVelocity(velocity: (Float, Float), speed: Float = 1): Unit =
    this.getBody.setLinearVelocity(velocity * speed)

  /** Changes the current velocity on x and maintains the current y velocity
   *
   *  @param velocity the new x velocity
   *  @param speed a multiplier to the velocity
   */
  def setVelocityX(velocity: Float, speed: Float = 1): Unit =
    this.getBody.setLinearVelocity(velocity * speed, this.getBody.getLinearVelocity.y)

  /** Changes the current velocity on y and maintains the current x velocity
   *
   *  @param velocity the new y velocity
   *  @param speed a multiplier to the velocity
   */
  def setVelocityY(velocity: Float, speed: Float = 1): Unit =
    this.getBody.setLinearVelocity(this.getBody.getLinearVelocity.x, velocity * speed)

  /** Get the current velocity on x and y
   *
   *  @return the velocity of this Entity
   */
  def getVelocity: (Float, Float) = (this.getBody.getLinearVelocity.x, this.getBody.getLinearVelocity.y)
}

object MobileEntity {

  def apply(entityType: EntityType = EntityType.Mobile,
            size: (Float, Float) = (10, 10),
            position: (Float, Float) = (0, 0),
            entityCollisionBit: Short = EntityCollisionBit.Mobile,
            collisions: Short = 0,
            gravityScale: Float = 1.0f,
            stats: Map[Statistic, Float] = Map()): MobileEntity = {

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, entityCollisionBit,
      collisions, createPolygonalShape(size.PPM), position.PPM, gravityScale = gravityScale)

    val mobileEntity: MobileEntity = new MobileEntityImpl(entityType, entityBody, size.PPM, stats)
    addEntity(mobileEntity)
    mobileEntity
  }

  /** Create a Sword Attack Pattern with a pivot point and a rotating body that can collide
   *  with another entity
   *
   *  @param entityType the texture that will be attached to this Entity by the View
   *  @param rotatingBodySize the size of the sword
   *  @param rotatingBodyDistance the distance between the sword and the center of the source entity
   *  @param angularVelocity how fast will rotate around the pivot point
   *  @param startingAngle how is inclined the sword
   *  @param sourceEntity the entity that has generated this attack
   *
   *  @return a Mobile Entity representing the sword
   */
  def createSwordAttackPattern(entityType: EntityType = EntityType.Mobile,
                               rotatingBodySize: (Float, Float),
                               rotatingBodyDistance: (Float, Float),
                               angularVelocity: Float,
                               startingAngle: Float = 0,
                               sourceEntity: LivingEntity): MobileEntity = {

    val pivotBody: EntityBody = createPivotPoint(sourceEntity.getPosition)

    val swordBody: EntityBody = createSword(rotatingBodySize, rotatingBodyDistance,
      sourceEntity.getPosition, pivotBody, startingAngle)

    val circularMobileEntity =
      new MobileEntityImpl(entityType, swordBody, rotatingBodySize.PPM, sourceEntity.getStatistics)
    circularMobileEntity.setMovementStrategy(
      new CircularMovementStrategy(circularMobileEntity, angularVelocity))
    circularMobileEntity.setCollisionStrategy(
      new ApplyDamage((e:Entity) => e.isInstanceOf[EnemyImpl], sourceEntity.getStatistics))

    addEntity(circularMobileEntity)
    circularMobileEntity
  }

  /** Create a Sword Attack Pattern on the Air with a pivot point and a rotating body that can collide
   *  with another entity
   *
   *  @param size the size of the sword
   *  @param distance the distance between the sword and the center of the source entity
   *  @param sourceEntity the entity that has generated this attack
   *
   *  @return a Mobile Entity representing the sword
   */
  def createAirAttackPattern(size: (Float, Float),
                             distance: (Float, Float),
                             sourceEntity: LivingEntity): MobileEntity = {

    val swordBody: EntityBody = createSword(size, distance, sourceEntity.getPosition, sourceEntity.getEntityBody)

    val airSword: MobileEntity = new AirSwordMobileEntity(EntityType.Mobile, swordBody, size.PPM)
    airSword.setCollisionStrategy(new ApplyDamage((e:Entity) => e.isInstanceOf[Enemy], sourceEntity.getStatistics))

    addEntity(airSword)
    airSword
  }

  /** Create an Arrow Projectile with a linear velocity that can collide with other entities
   *
   *  @param entity the entity that has generated this attack
   *
   *  @return a Mobile Entity representing the arrow
   */
  def createArrowProjectile(entity: LivingEntity): MobileEntity = {
    val arrow: MobileEntity = MobileEntity(EntityType.Arrow, ARROW_SIZE, newArrowPosition(entity),
      EntityCollisionBit.Arrow, ARROW_COLLISIONS , gravityScale = 0)
    arrow.setFacing(entity.isFacingRight)
    arrow.setMovementStrategy(new ArrowMovementStrategy(arrow, entity.getStatistic(Statistic.MovementSpeed).get))
    arrow.setCollisionStrategy(new ApplyDamageAndDestroyEntity(arrow, (e:Entity) => e.isInstanceOf[EnemyImpl] , entity.getStatistics))
    arrow
  }

  private def newArrowPosition(sourceEntity: LivingEntity): (Float, Float) = {
    var newPosition: (Float, Float) = sourceEntity.getPosition.MPP
    if(sourceEntity.isFacingRight)
      newPosition += (sourceEntity.getSize.MPP.x + ARROW_SIZE.x, 0)
    else
      newPosition -= (sourceEntity.getSize.MPP.x + ARROW_SIZE.x, 0)
    newPosition
  }

  private def createPivotPoint(pivotPoint: (Float, Float)): EntityBody = {
    defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Immobile,
      NO_COLLISIONS, createPolygonalShape(PIVOT_SIZE.PPM), pivotPoint, isSensor = true)
  }

  private def createSword(size: (Float, Float),
                          distance: (Float, Float),
                          pivotPoint: (Float, Float),
                          sourceBody: EntityBody,
                          startingAngle: Float = 0): EntityBody = {

    val rotatingBodyPosition: (Float, Float) = pivotPoint + distance.PPM
    val swordBody: EntityBody = createSwordEntityBody(size, rotatingBodyPosition, startingAngle)
    pendingJointCreation(sourceBody.getBody, swordBody.getBody)
    swordBody
  }

  private def createSwordEntityBody(size: (Float, Float), position: (Float, Float),
                                    startingAngle: Float = 0): EntityBody = {
    defineEntityBody(BodyType.DynamicBody, EntityCollisionBit.Sword, SWORD_COLLISIONS,
      createPolygonalShape(size.PPM), position, startingAngle,
      gravityScale = 0, SWORD_ATTACK_DENSITY, isSensor = true)
  }
}

/** Represents a entity that can move based on a movement strategy and can collide with other entities
 *
 *  @param entityType the texture that will be attached to this Entity by the View
 *  @param entityBody the body of this entity that is affected by physics and collisions
 *  @param size the size of the entity
 *  @param stats the statistics of this entity
 *
 *  @return a Mobile Entity representing the sword
 */
class MobileEntityImpl(private val entityType: EntityType,
                       private var entityBody: EntityBody,
                       private val size: (Float, Float),
                       private var stats: Map[Statistic, Float] = Map()) extends EntityImpl(entityType, entityBody, size) with MobileEntity {

  private var facingRight: Boolean = true

  protected var movementStrategy: MovementStrategy = DoNothingMovementStrategy()

  override def update(): Unit = {
    // TODO: chiedere a luca perchè non si può fare la move qui
  }

  override def setMovementStrategy(strategy: MovementStrategy): Unit = this.movementStrategy = strategy

  override def move(): Unit = {
    this.movementStrategy.apply()
  }

  override def stopMovement(): Unit = if(this.movementStrategy != null) this.movementStrategy.stopMovement()

  override def setFacing(right: Boolean): Unit = this.facingRight = right

  override def isFacingRight: Boolean = this.facingRight

  override def getStatistics: Map[Statistic, Float] = stats

  override def alterStatistics(statistic: Statistic, alteration: Float): Unit = {
    if(stats.contains(statistic)) {
      val newValue = stats(statistic) + alteration
      this.stats += (statistic -> newValue)

      statistic match {
        case Statistic.MovementSpeed => this.movementStrategy.alterSpeed(alteration)
        case _ =>
      }
    }
  }

  override def getStatistic(statistic:Statistic): Option[Float] = {
    if (this.stats.contains(statistic))
      Option.apply(this.stats(statistic))
    else
      Option.empty
  }
}

/** Represents a sword in the air that is able to move
 *
 *  @param entityType the texture that will be attached to this Entity by the View
 *  @param entityBody the body of this entity that is affected by physics and collisions
 *  @param size the size of the entity
 *  @param stats the statistics of this entity
 *
 *  @return a Mobile Entity representing the sword
 */
class AirSwordMobileEntity(private val entityType: EntityType,
                           private var entityBody: EntityBody,
                           private val size: (Float, Float),
                           private val stats: Map[Statistic, Float] = Map()) extends MobileEntityImpl(entityType, entityBody, size, stats) {

  override def destroyEntity(): Unit = {
    EntitiesFactoryImpl.pendingDestroyBody(this.getBody)
    EntitiesFactoryImpl.removeEntity(this)
  }
}

package model.entity

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.Shape
import model.EntityBody
import model.helpers.ImplicitConversions.{entityToBody, _}
import model.entity.collision.{AttackCollisionStrategy, ArrowCollisionStrategy, BulletCollisionStrategy, EntityCollisionBit}
import model.entity.EntityType.EntityType
import model.entity.Statistic.Statistic
import model.helpers.EntitiesFactoryImpl._
import model.helpers.GeometricUtilities.isBodyOnTheRight
import model.entity.movement._
import utils.CollisionConstants._
import utils.EnemiesConstants.{PROJECTILE_DYING_STATE_DURATION, PROJECTILE_ENTITIES_DURATION, WIZARD_ATTACK3_PROJECTILE_SPEED, WORM_ATTACK_PROJECTILE_SPEED}
import utils.HeroConstants.{ARROW_SIZE, PIVOT_SIZE, SWORD_ATTACK_DENSITY}

/** The possible statistics that could be given to a mobile or living entity.
 * Those statistics may be used indiscriminately by attack or movement
 * strategies. For instance the VisionDistance value could be used by an enemy
 * entity both to define movement or attack strategies (move towards or attack
 * hero if in near enough)
 *
 * @see [[model.entity.MobileEntity]]
 * @see [[model.entity.LivingEntity]]
 */
object Statistic extends Enumeration {
  type Statistic = Value

  val CurrentHealth, Health, Strength, Defence, MovementSpeed, AttackSpeed = Value

  val VisionDistance, AttackFrequency, AttackDuration = Value
}

/** An Entity that can move. The movement behaviour is extracted and managed by
 * a MovementStrategy class object.
 *
 * A movement strategy may be derived by simpler movements and may consider the
 * entity state to personalize it's behaviour.
 *
 * @see [[model.entity.movement.MovementStrategy]]
 */
trait MobileEntity extends Entity {

  /** Define the entity movement strategy. A movement strategy may be derived
   * by simpler movements and may consider the entity state to personalize it's
   * behaviour.
   *
   * @see [[model.entity.movement.MovementStrategy]]
   *
   * @param strategy the behavior that will call with move()
   */
  def setMovementStrategy(strategy: MovementStrategy): Unit

  /** Execute the entity movement behaviour.
   */
  def move(): Unit

  /** Stop the entity movement based on what is defined in the movement
   * strategy. A movement interruption may imply different things based
   * on different movement policies.
   */
  def stopMovement(): Unit

  /** Changes the direction in which the entity is looking. The direction faced
   * by an entity may involve different strategies. For instance, an enemy
   * entity may not be able to see the hero approaching from its back.
   *
   * @param right true if the entity is facing to the right false otherwise.
   */
  def setFacing(right: Boolean): Unit

  /** Check the direction in which the entity is looking. The direction faced
   * by an entity may involve different strategies. For instance, an enemy
   * entity may not be able to see the hero approaching from its back.
   *
   * @return true if the entity is facing to the right false otherwise.
   */
  def isFacingRight: Boolean

  /** Return the statistics of the Entity. Mobile and living entities define
   * a set of statistics.
   *
   * @return a map of Statistic and value pairs
   */
  def getStatistics: Map[Statistic, Float]

  /** Modifies a statistic based on the alteration value. The alteration is not
   * the new value but the modifier to sum to the entity statistic. This method
   * is used both to manage the hero statistics (modified when he picks up an
   * item) and the enemies statistics (which increase proportionally to the
   * nmber of level explored.
   *
   * @see [[model.entity.Statistic]]
   *
   * @param statistic the statistic that will be altered
   * @param alteration the value that will be added
   */
  def alterStatistics(statistic: Statistic, alteration: Float): Unit

  /** Return a specific statistic value if present. A mobile entity may define
   * a subset of statistics available
   *
   * @see [[model.entity.Statistic]]
   *
   * @param statistic the statistic required
   * @return a float if the statistic is present, Option.empty otherwise
   */
  def getStatistic(statistic: Statistic): Option[Float]

  /** Change the current velocity on x and y of this Entity. An entity velocity
   * is defined by a force vector (a tuple of float value).
   *
   * @see [[com.badlogic.gdx.physics.box2d.Body]]
   *
   * @param velocity the new velocity
   * @param speed a multiplier to the velocity
   */
  def setVelocity(velocity: (Float, Float), speed: Float = 1): Unit =
    this.getBody.setLinearVelocity(velocity * speed)

  /** Changes the current velocity on x and maintains the current y velocity. 
   * An entity velocity is defined by a force vector (a tuple of float value).
   *
   * @see [[com.badlogic.gdx.physics.box2d.Body]]
   *      
   * @param velocity the new x velocity
   * @param speed a multiplier to the velocity
   */
  def setVelocityX(velocity: Float, speed: Float = 1): Unit =
    this.getBody.setLinearVelocity(velocity * speed, this.getBody.getLinearVelocity.y)

  /** Changes the current velocity on y and maintains the current x velocity.
   * An entity velocity is defined by a force vector (a tuple of float value).
   *
   * @see [[com.badlogic.gdx.physics.box2d.Body]]
   * @param velocity the new y velocity
   * @param speed a multiplier to the velocity
   */
  def setVelocityY(velocity: Float, speed: Float = 1): Unit =
    this.getBody.setLinearVelocity(this.getBody.getLinearVelocity.x, velocity * speed)

  /** Get the current velocity on x and y. An entity velocity is defined by a
   * force vector (a tuple of float value).
   *
   * @return the velocity of this Entity
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
   * @param entityType the texture that will be attached to this Entity by the View
   * @param rotatingBodySize the size of the sword
   * @param rotatingBodyDistance the distance between the sword and the center of the source entity
   * @param angularVelocity how fast will rotate around the pivot point
   * @param startingAngle how is inclined the sword
   * @param sourceEntity the entity that has generated this attack
   *
   * @return a Mobile Entity representing the sword
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
      CircularMovementStrategy(circularMobileEntity, angularVelocity))
    circularMobileEntity.setCollisionStrategy(
      AttackCollisionStrategy((e:Entity) => e.isInstanceOf[EnemyImpl],
        sourceEntity.getStatistic(Statistic.Strength).get))

    addEntity(circularMobileEntity)
    circularMobileEntity
  }

  /** Create a Sword Attack Pattern on the Air with a pivot point and a rotating body that can collide
   *  with another entity
   *
   * @param size the size of the sword
   * @param distance the distance between the sword and the center of the source entity
   * @param sourceEntity the entity that has generated this attack
   *
   * @return a Mobile Entity representing the sword
   */
  def createAirAttackPattern(size: (Float, Float),
                             distance: (Float, Float),
                             sourceEntity: LivingEntity): MobileEntity = {

    val swordBody: EntityBody = createSword(size, distance, sourceEntity.getPosition, sourceEntity.getEntityBody)

    val airSword: MobileEntity = new AirSwordMobileEntity(EntityType.Mobile, swordBody, size.PPM)
    airSword.setCollisionStrategy(AttackCollisionStrategy((e:Entity) => e.isInstanceOf[Enemy],
      sourceEntity.getStatistic(Statistic.Strength).get))

    addEntity(airSword)
    airSword
  }

  /** Create an Arrow Projectile with a linear velocity that can collide with other entities
   *
   * @param entity the entity that has generated this attack
   *
   * @return a Mobile Entity representing the arrow
   */
  def createArrowProjectile(entity: LivingEntity): MobileEntity = {
    val arrow: MobileEntity = MobileEntity(EntityType.Arrow, ARROW_SIZE, newArrowPosition(entity),
      EntityCollisionBit.Arrow, ARROW_COLLISIONS , gravityScale = 0)
    arrow.setFacing(entity.isFacingRight)
    arrow.setMovementStrategy(ArrowMovementStrategy(arrow, entity.getStatistic(Statistic.MovementSpeed).get))
    arrow.setCollisionStrategy(ArrowCollisionStrategy(arrow, (e:Entity) => e.isInstanceOf[EnemyImpl] ,
      entity.getStatistic(Statistic.Strength).get))
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

  private def computeAttackPosition(sourceEntity: MobileEntity, offset: (Float, Float) = (0f, 0f)): (Float, Float) = {
    val attackXOffset:Float = if (sourceEntity.isFacingRight) offset._1.PPM else -offset._1.PPM
    val attackYOffset:Float = offset._2.PPM
    (sourceEntity.getBody.getWorldCenter.x + attackXOffset, sourceEntity.getBody.getWorldCenter.y + attackYOffset)
  }

  private def createAttackEntity(entityType:EntityType,
                                 entityCollisionBit: Short,
                                 collisions: Short,
                                 shape: Shape,
                                 position: (Float, Float),
                                 size: (Float, Float),
                                 stats: Map[Statistic, Float],
                                 bullet: Boolean = false): MobileEntity = {
    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, entityCollisionBit,
      collisions, shape, position, isSensor = true)

    var attack: MobileEntity = null
    if (bullet)
      attack = new BulletMobileEntity(entityType, entityBody, size.PPM, stats)
    else
      attack = new MobileEntityImpl(entityType, entityBody, size.PPM, stats)

    attack
  }

  private def defineBulletAttack(attack:MobileEntity, sourceEntity:MobileEntity, targetEntity:Entity): Unit = {
    attack.getBody.setBullet(true)
    attack.setFacing(isBodyOnTheRight(sourceEntity, targetEntity))

    attack.setCollisionStrategy(BulletCollisionStrategy(attack, (e:Entity) => e.isInstanceOf[Hero],
      sourceEntity.getStatistic(Statistic.Strength).get))
  }

  def createFireballAttack(sourceEntity: LivingEntity,
                           targetEntity: Entity,
                           targetPoint: (Float, Float),
                           size: (Float, Float) = (1f, 1f),
                           offset: (Float, Float) = (0f, 0f)): MobileEntity = {

    val attackPosition = computeAttackPosition(sourceEntity, offset)
    val attack: MobileEntity = createAttackEntity(EntityType.AttackFireBall,
      EntityCollisionBit.EnemyAttack, FIREBALL_COLLISIONS, createCircleShape(size._1.PPM), attackPosition,
      size, sourceEntity.getStatistics, bullet = true)

    defineBulletAttack(attack, sourceEntity, targetEntity)

    attack.setMovementStrategy(FireBallMovementStrategy(attack, attackPosition,
      targetPoint, WORM_ATTACK_PROJECTILE_SPEED))

    addEntity(attack)
    attack
  }

  def createEnergyBallAttack(sourceEntity: LivingEntity,
                             targetEntity: Entity,
                             size: (Float, Float) = (1f, 1f),
                             offset: (Float, Float) = (0f, 0f)): MobileEntity = {
    val attackPosition = computeAttackPosition(sourceEntity, offset)
    val attack: MobileEntity = createAttackEntity(EntityType.AttackEnergyBall,
      EntityCollisionBit.EnemyAttack, ENERGY_BALL_COLLISIONS, createCircleShape(size._1.PPM),
      attackPosition, size, sourceEntity.getStatistics, bullet = true)

    defineBulletAttack(attack, sourceEntity, targetEntity)

    attack.setMovementStrategy(EnergyBallMovementStrategy(attack, attackPosition,
      targetEntity, WIZARD_ATTACK3_PROJECTILE_SPEED))

    addEntity(attack)
    attack
  }

  def createMeleeAttack(sourceEntity: LivingEntity,
                        size: (Float, Float) = (23, 23),
                        offset: (Float, Float) = (20, 5)): MobileEntity = {
    val attack: MobileEntity = createAttackEntity(EntityType.Mobile,
      EntityCollisionBit.EnemyAttack, ENEMY_MELEE_ATTACK_COLLISIONS, createPolygonalShape(size.PPM),
      computeAttackPosition(sourceEntity, offset), size, sourceEntity.getStatistics)

    createJoint(createPivotPoint(sourceEntity.getPosition).getBody, attack.getBody)

    attack.setCollisionStrategy(AttackCollisionStrategy(e => e.isInstanceOf[Hero],
      sourceEntity.getStatistic(Statistic.Strength).get))

    addEntity(attack)
    attack
  }
}

/** Implementation of interface MobileEntity mixed in with EntityImpl.
 * Represents a entity that can move based on a movement strategy and can
 * collide with other entities.
 *
 * @param entityType the texture that will be attached to this Entity by the View
 * @param entityBody the body of this entity that is affected by physics and collisions
 * @param size the size of the entity
 * @param stats the statistics of this entity
 *
 * @return a Mobile Entity representing the sword
 */
class MobileEntityImpl(private val entityType: EntityType,
                       private var entityBody: EntityBody,
                       private val size: (Float, Float),
                       private var stats: Map[Statistic, Float] = Map())
  extends EntityImpl(entityType, entityBody, size) with MobileEntity {

  private var facingRight: Boolean = true

  protected var movementStrategy: MovementStrategy = DoNothingMovementStrategy()

  override def update(): Unit = { }

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
 * @param entityType the texture that will be attached to this Entity by the View
 * @param entityBody the body of this entity that is affected by physics and collisions
 * @param size the size of the entity
 * @param stats the statistics of this entity
 *
 * @return a Mobile Entity representing the sword
 */
class AirSwordMobileEntity(private val entityType: EntityType,
                           private var entityBody: EntityBody,
                           private val size: (Float, Float),
                           private val stats: Map[Statistic, Float] = Map())
  extends MobileEntityImpl(entityType, entityBody, size, stats) {

  override def destroyEntity(): Unit = {
    pendingDestroyBody(this.getBody)
    removeEntity(this)
  }
}

/**
 *
 * @param entityType the texture that will be attached to this Entity by the View
 * @param entityBody the body of this entity that is affected by physics and collisions
 * @param size the size of the entity
 * @param stats the statistics of this entity
 *
 */
class BulletMobileEntity(private val entityType: EntityType,
                         private var entityBody: EntityBody,
                         private val size: (Float, Float),
                         private val stats: Map[Statistic, Float] = Map())
  extends MobileEntityImpl(entityType, entityBody, size, stats) {

  private var dyingStateTime: Long = 0
  private val creationTime: Long = System.currentTimeMillis()

  override def update(): Unit = {
    super.update()

    val now:Long = System.currentTimeMillis()
    if (now - this.creationTime > PROJECTILE_ENTITIES_DURATION)
      this.setState(State.Dying)

    if (this.getState != State.Dying)
      this.move()
    else {
      if (dyingStateTime == 0) {
        this.dyingStateTime = now
        this.movementStrategy.stopMovement()
      }

      if (dyingStateTime != 0 && now - this.dyingStateTime > PROJECTILE_DYING_STATE_DURATION)
        this.destroyEntity()
    }
  }
}
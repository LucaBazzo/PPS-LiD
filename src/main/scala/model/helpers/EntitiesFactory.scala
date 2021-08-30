package model.helpers

import _root_.utils.ApplicationConstants._
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d._
import com.badlogic.gdx.physics.box2d.joints.{RevoluteJointDef, WeldJointDef}
import model._
import model.attack._
import model.collisions.ImplicitConversions._
import model.collisions._
import model.entities.EntityId.EntityId
import model.entities.ItemPools.ItemPools
import model.entities.Statistic.Statistic
import model.entities.{Entity, Statistic, _}
import model.movement._

import scala.collection.immutable.HashMap

trait EntitiesFactory {

  def setLevel(level: Level, pool: ItemPool)

  def createMobileEntity(entityType: EntityId = EntityId.Mobile,
                         size: (Float, Float) = (10, 10),
                         position: (Float, Float) = (0, 0),
                         entityCollisionBit: Short = EntityType.Mobile,
                         collisions: Short = 0,
                         useGravity: Boolean = true): MobileEntity

  def createHeroEntity(): Hero

  def createEnemyEntity(position: (Float, Float),
                        size: (Float, Float),
                        stats: Map[Statistic, Float],
                        score: Int,
                        entityId: EntityId): Enemy

  def createSlimeEnemy(position: (Float, Float)): Enemy

  def createSkeletonEnemy(position: (Float, Float)): Enemy

  def createWormEnemy(position: (Float, Float)): Enemy

  def createItem(PoolName: ItemPools,
                 size: (Float, Float) = (5f, 5f),
                 position: (Float, Float) = (0, 0),
                 collisions: Short = EntityType.Hero): Item

  def createPolygonalShape(size: (Float, Float)): Shape

  def createCircleShape(radius: Float): Shape

  def createImmobileEntity(entityType: EntityId = EntityId.Immobile,
                           size: (Float, Float) = (10, 10),
                           position: (Float, Float) = (0, 0),
                           entityCollisionBit: Short = EntityType.Immobile,
                           collisions: Short = 0): Entity


  def createDoor(size: (Float, Float) = (10, 10),
                          position: (Float, Float) = (0, 0),
                          collisions: Short = 0): Entity

  def createAttackPattern(entityType: EntityId = EntityId.Mobile,
                          rotatingBodySize: (Float, Float) = (1,1),
                          pivotPoint: (Float, Float) = (0,0),
                          rotatingBodyDistance: (Float, Float) = (0,0),
                          angularVelocity: Float = 0,
                          startingAngle: Float = 0,
                          sourceEntity: LivingEntity): MobileEntity

  def createEnemyProjectileAttack(entityType: EntityId = EntityId.AttackFireBall,
                                  size: (Float, Float) = (10, 10),
                                  position: (Float, Float) = (0, 0),
                                  targetPosition: (Float, Float) = (0, 0),
                                  sourceEntity: LivingEntity): Attack

  def createMeleeSwordAttack(entityType: EntityId,
                             size: (Float, Float) = (1, 1),
                             position: (Float, Float) = (0, 0),
                             sourceEntity: LivingEntity): Attack

  def createEnemyContactAttack(radius: Float,
                               sourceEntity: LivingEntity): Attack

  def createArrowProjectile(entity: LivingEntity): MobileEntity

  def createJoint(pivotBody: Body, rotatingBody: Body): Joint

  def removeEntity(entity: Entity)

  def createBody(bodyDef: BodyDef): Body

  def destroyBody(body: Body)

  def destroyJoint(joint: Joint)

  def destroyBodies(): Unit

  def destroyJoints(): Unit

  def changeCollisions(entity: Entity, entityType: Short): Unit

  def applyEntityCollisionChanges(): Unit
}

object EntitiesFactoryImpl extends EntitiesFactory {
  private var level: Level = _
  private var entitiesToBeChanged: List[(Entity, Short)] = List.empty
  private var itemPool: ItemPool = _
  private var bodiesToBeDestroyed: List[Body] = List.empty
  private var jointsToBeDestroyed: List[Joint] = List.empty

  override def setLevel(level: Level, pool: ItemPool): Unit = {
    this.level = level
    this.itemPool = pool
  }

  override def createMobileEntity(entityType: EntityId = EntityId.Mobile,
                                  size: (Float, Float) = (10, 10),
                                  position: (Float, Float) = (0, 0),
                                  entityCollisionBit: Short = EntityType.Mobile,
                                  collisions: Short = 0,
                                  useGravity: Boolean = true): MobileEntity = {

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, entityCollisionBit,
      collisions, createPolygonalShape(size.PPM), position.PPM, gravity = useGravity)

    val mobileEntity: MobileEntity = new MobileEntityImpl(entityType, entityBody, size.PPM, new HashMap[Statistic, Float]())
    this.level.addEntity(mobileEntity)
    mobileEntity
  }

  override def createHeroEntity(): Hero = {
    val position: (Float, Float) = (200f, 400f)
    val size: (Float, Float) = HERO_SIZE

    val statistic: Map[Statistic, Float] = Map(
      Statistic.Health -> 1000,
      Statistic.CurrentHealth -> 1000,
      Statistic.Strength -> 100,
      Statistic.MovementSpeed -> 1,
      Statistic.Defence -> 0)

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.Hero,
      EntityType.Immobile | EntityType.Enemy | EntityType.Item | EntityType.Door | EntityType.EnemyAttack, createPolygonalShape(size.PPM), position.PPM, friction = 0.8f)

    val hero: Hero = new HeroImpl(EntityId.Hero, entityBody, size.PPM, statistic)

    hero.setCollisionStrategy(new CollisionStrategyImpl())
    hero.setMovementStrategy(new HeroMovementStrategy(hero, statistic(Statistic.MovementSpeed)))
    hero.setAttackStrategy(new HeroAttackStrategyImpl(hero, statistic(Statistic.Strength)))

    this.level.addEntity(hero)
    hero
  }

  override def createSkeletonEnemy(position: (Float, Float)): Enemy = {
    val stats:Map[Statistic, Float] = Map(
      Statistic.Strength -> 100f,
      Statistic.Health -> 11f,
      Statistic.CurrentHealth -> 11f,
      Statistic.Defence -> 0f,

      Statistic.MaxMovementSpeed -> 40f.PPM,
      Statistic.Acceleration -> 5f.PPM,

      Statistic.HorizontalVisionDistance -> 40.PPM,
      Statistic.HorizontalVisionAngle -> 30,
      Statistic.AttackFrequency -> 2000,
      Statistic.AttackDuration -> 1200)

    val size:(Float, Float) = (13f, 23f)
    val score: Int = 100

    val enemy:Enemy = createEnemyEntity(position, size, stats, score, EntityId.EnemySkeleton)

    enemy.setMovementStrategy(new PatrolAndStop(enemy, this.level, stats,
      (e:Entity) => e.isInstanceOf[Hero]))
    enemy.setCollisionStrategy(new DoNothingOnCollision())
    enemy.setAttackStrategy(new MeleeAttack(enemy, this.level, stats,
      (e:Entity) => e.isInstanceOf[Hero]))
    enemy
  }

  override def createSlimeEnemy(position: (Float, Float)): Enemy = {
    val stats:Map[Statistic, Float] = Map(
      Statistic.Strength -> 10f,
      Statistic.Health -> 11f,
      Statistic.CurrentHealth -> 11f,
      Statistic.Defence -> 0f,

      Statistic.MaxMovementSpeed -> 40f.PPM,
      Statistic.Acceleration -> 5f.PPM,

      Statistic.HorizontalVisionDistance -> 20.PPM,
      Statistic.HorizontalVisionAngle -> 20,
      Statistic.AttackFrequency -> 2000,
      Statistic.AttackDuration -> 1000,
    )
    val size:(Float, Float) = (13f, 13f)
    val score: Int = 100

    val enemy:Enemy = createEnemyEntity(position, size, stats, score, EntityId.EnemySlime)

    val collisionStrategy:CollisionStrategy = new DoNothingOnCollision()
    val attackStrategy:AttackStrategy = new MeleeAttack(enemy, this.level, stats,
      (e:Entity) => e.isInstanceOf[Hero])
    val movementStrategy:MovementStrategy = new PatrolAndStop(enemy, this.level, stats,
      (e:Entity) => e.isInstanceOf[Hero])

    enemy.setMovementStrategy(movementStrategy)
    enemy.setCollisionStrategy(collisionStrategy)
    enemy.setAttackStrategy(attackStrategy)
    enemy
  }

  override def createWormEnemy(position: (Float, Float)): Enemy = {
    val stats:Map[Statistic, Float] = Map(
      Statistic.Strength -> 5f,
      Statistic.Health -> 11f,
      Statistic.CurrentHealth -> 11f,
      Statistic.Defence -> 0f,

      Statistic.MaxMovementSpeed -> 40f.PPM,
      Statistic.Acceleration -> 5f.PPM,

      Statistic.HorizontalVisionDistance -> 100.PPM,
      Statistic.HorizontalVisionAngle -> 70,
      Statistic.AttackFrequency -> 1500,
      Statistic.AttackDuration -> 900,
    )
    val size:(Float, Float) = (15f, 11f)
    val score: Int = 100

    val enemy:Enemy = createEnemyEntity(position, size, stats, score, EntityId.EnemyWorm)

    val collisionStrategy:CollisionStrategy = new DoNothingOnCollision()
    val attackStrategy:AttackStrategy = new RangedAttack(enemy, this.level, stats,
      (e:Entity) => e.isInstanceOf[Hero])
    val movementStrategy:MovementStrategy = new PatrolAndStop(enemy, this.level, stats,
      (e:Entity) => e.isInstanceOf[Hero])

    enemy.setMovementStrategy(movementStrategy)
    enemy.setCollisionStrategy(collisionStrategy)
    enemy.setAttackStrategy(attackStrategy)
    enemy
  }

  override def createEnemyEntity(position: (Float, Float),
                                 size: (Float, Float),
                                 stats:Map[Statistic, Float],
                                 score: Int,
                                 entityId: EntityId): Enemy = {


    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.Enemy,
      EntityType.Immobile | EntityType.Sword, createPolygonalShape(size.PPM), position.PPM)
    val enemy:Enemy = new Enemy(entityId, entityBody, size.PPM, stats, score)
    this.level.addEntity(enemy)
    enemy
  }

  override def createItem(PoolName: ItemPools,
                          size: (Float, Float) = (5f, 5f),
                          position: (Float, Float) = (0, 0),
                          collisions: Short = EntityType.Hero): Item = {
    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityType.Item,
      collisions, createPolygonalShape(size.PPM), position.PPM)
    val item: Item = itemPool.getItem(entityBody, size, PoolName)
    item.setCollisionStrategy(new ItemCollisionStrategy(item))
    this.level.addEntity(item)
    item
  }

  override def createPolygonalShape(size: (Float, Float)): Shape = {
    val shape: PolygonShape = new PolygonShape()
    shape.setAsBox(size._1, size._2)
    shape
  }

  override def createCircleShape(radius: Float): Shape = {
    val shape: CircleShape = new CircleShape()
    shape.setRadius(radius)
    shape
  }

  override def createImmobileEntity(entityType: EntityId = EntityId.Immobile,
                                    size: (Float, Float) = (10, 10),
                                    position: (Float, Float) = (0, 0),
                                    entityCollisionBit: Short = EntityType.Immobile,
                                    collisions: Short = 0): Entity = {

    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, entityCollisionBit,
      collisions, createPolygonalShape(size.PPM), position.PPM)

    val immobileEntity: Entity = ImmobileEntity(entityType, entityBody, size.PPM)
    immobileEntity.setCollisionStrategy(new CollisionStrategyImpl())
    immobileEntity
  }

  override def createDoor(size: (Float, Float) = (10, 10),
                          position: (Float, Float) = (0, 0),
                          collisions: Short = 0): Entity = {

    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityType.Door,
      EntityType.Hero | EntityType.Sword, createPolygonalShape(size.PPM), position.PPM)

    val immobileEntity: Entity = ImmobileEntity(EntityId.Immobile, entityBody, size.PPM)
    immobileEntity.setCollisionStrategy(new DoorCollisionStrategy(immobileEntity.asInstanceOf[ImmobileEntity]))
    this.level.addEntity(immobileEntity)
    immobileEntity
  }

  override def createAttackPattern(entityType: EntityId = EntityId.Mobile,
                                   rotatingBodySize: (Float, Float),
                                   pivotPoint: (Float, Float),
                                   rotatingBodyDistance: (Float, Float),
                                   angularVelocity: Float,
                                   startingAngle: Float = 0,
                                   sourceEntity: LivingEntity): MobileEntity = {

    val pivotSize = (2f, 2f)

    val pivotBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityType.Immobile,
      0, createPolygonalShape(pivotSize.PPM), pivotPoint, isSensor = true)

    val rotatingBodyPosition = (pivotPoint._1 + rotatingBodyDistance._1.PPM, pivotPoint._2 + rotatingBodyDistance._2.PPM)
    val rotatingBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.Sword,
      EntityType.Enemy, createPolygonalShape(rotatingBodySize.PPM), rotatingBodyPosition,
      startingAngle, gravity = false, 1, 0.3f, 0.5f, isSensor = true)


    val circularMobileEntity =
      new CircularMobileEntity(entityType, rotatingBody, rotatingBodySize.PPM, sourceEntity.getStatistics, pivotBody)
    circularMobileEntity.setMovementStrategy(
      new CircularMovementStrategy(circularMobileEntity, angularVelocity))
    circularMobileEntity.setCollisionStrategy(
      new ApplyDamage((e:Entity) => e.isInstanceOf[Enemy], sourceEntity.getStatistics))


    this.level.addEntity(circularMobileEntity)
    circularMobileEntity
  }

  override def createEnemyProjectileAttack(entityType: EntityId = EntityId.AttackFireBall,
                                           size: (Float, Float) = (10, 10),
                                           position: (Float, Float) = (0, 0),
                                           targetPosition: (Float, Float) = (0, 0),
                                           sourceEntity: LivingEntity): Attack = {

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.EnemyAttack,
      EntityType.Immobile | EntityType.Hero | EntityType.Door, this.createCircleShape(size._1.PPM),
      position, isSensor = true)
    entityBody.getBody.setBullet(true)

    val arrowAttack: Attack = new Attack(entityType, entityBody, size, sourceEntity.getStatistics, Option(1000))

    arrowAttack.setCollisionStrategy(new ApplyDamageAndDestroyEntity(arrowAttack, (e:Entity) => e.isInstanceOf[Hero],
      sourceEntity.getStatistics))
    arrowAttack.setMovementStrategy(new WeightlessProjectileTrajectory(arrowAttack, sourceEntity.getPosition,
      targetPosition, sourceEntity.getStatistics))

    this.level.addEntity(arrowAttack)
    arrowAttack
  }

  override def createMeleeSwordAttack(entityType: EntityId,
                                      size: (Float, Float) = (1, 1),
                                      position: (Float, Float) = (0, 0),
                                      sourceEntity:LivingEntity): Attack = {

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.EnemyAttack,
      EntityType.Hero, this.createPolygonalShape(size.PPM), position, isSensor = true)

    val jointDef:WeldJointDef = new WeldJointDef()
    jointDef.initialize(sourceEntity.getBody, entityBody.getBody, sourceEntity.getBody.getPosition)
    this.level.getWorld.createJoint(jointDef)

    val swordAttack: Attack = new Attack(entityType, entityBody, size, sourceEntity.getStatistics, Option(100))
    swordAttack.setCollisionStrategy(new ApplyDamage(e => e.isInstanceOf[Hero], sourceEntity.getStatistics))
    this.level.addEntity(swordAttack)
    swordAttack
  }

  override def createEnemyContactAttack(radius: Float,
                                        sourceEntity:LivingEntity): Attack = {
    val entityBody:EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.EnemyAttack,
      EntityType.Immobile | EntityType.Hero, this.createCircleShape(radius),
      (sourceEntity.getBody.getPosition.x, sourceEntity.getBody.getPosition.y), isSensor=true)

    val jointDef:WeldJointDef = new WeldJointDef()
    jointDef.initialize(sourceEntity.getBody, entityBody.getBody, sourceEntity.getBody.getPosition)
    this.level.getWorld.createJoint(jointDef)

    val contactAttack:Attack = new Attack(EntityId.Mobile, entityBody, (radius, radius), sourceEntity.getStatistics)
    contactAttack.setCollisionStrategy(new ApplyDamage(e => e.isInstanceOf[Hero], sourceEntity.getStatistics))
    level.addEntity(contactAttack)
    contactAttack
  }


  override def createArrowProjectile(entity: LivingEntity): MobileEntity = {
    //TODO mettere a posto
    val size: (Float, Float) = (8, 1)

    var newPosition: (Float, Float) = entity.getPosition * PIXELS_PER_METER
    if(entity.isFacingRight)
      newPosition += (entity.getSize._1 * PIXELS_PER_METER + size._1, 0)
    else
      newPosition -= (entity.getSize._1 * PIXELS_PER_METER + size._1, 0)
    val arrow: MobileEntity = EntitiesFactoryImpl.createMobileEntity(EntityId.Arrow, size, newPosition, EntityType.Arrow,
      EntityType.Immobile | EntityType.Enemy , useGravity = false)
    arrow.setFacing(entity.isFacingRight)
    arrow.setMovementStrategy(new ArrowMovementStrategy(arrow, entity.getStatistics(Statistic.MovementSpeed)))
    arrow.setCollisionStrategy(new ApplyDamageAndDestroyEntity(arrow, (e:Entity) => e.isInstanceOf[Enemy] , entity.getStatistics))
    arrow
  }

  override def createJoint(pivotBody: Body, rotatingBody: Body): Joint = {
    val rjd: RevoluteJointDef = new RevoluteJointDef()

    rjd.initialize(pivotBody, rotatingBody, pivotBody.getWorldCenter)

    this.level.getWorld.createJoint(rjd)
  }

  override def removeEntity(entity: Entity): Unit = this.level.removeEntity(entity)

  override def destroyBody(body: Body): Unit = synchronized {
    this.bodiesToBeDestroyed = body :: this.bodiesToBeDestroyed
  }

  override def destroyBodies(): Unit = synchronized {
    for(body <- bodiesToBeDestroyed) {
      this.level.getWorld.destroyBody(body)
    }
    this.bodiesToBeDestroyed = List.empty
  }

  // TODO: to be removed
  override def destroyJoint(joint: Joint): Unit = synchronized {
    this.jointsToBeDestroyed = joint :: this.jointsToBeDestroyed
    println(this.jointsToBeDestroyed)
  }

  override def destroyJoints(): Unit = synchronized {
    for(joint <- jointsToBeDestroyed) {
      this.level.getWorld.destroyJoint(joint)
    }
    this.jointsToBeDestroyed = List.empty
  }

  override def createBody(bodyDef: BodyDef): Body = this.level.getWorld.createBody(bodyDef)

  private def defineEntityBody(bodyType: BodyType,
                               entityType: Short,
                               collisions: Short,
                               shape: Shape,
                               position: (Float, Float),
                               angle: Float = 0,
                               gravity: Boolean = true,
                               density: Float = 0,
                               friction: Float = 0.2f,
                               restitution: Float = 0,
                               isSensor: Boolean = false): EntityBody = {

    val entityBody: EntityBody = new EntityBodyImpl()

    entityBody.createBody(bodyType, position, angle, gravity)
      .setEntityType(entityType)
      .setCollisions(collisions)
      .setShape(shape)
      .setFixtureValues(density, friction, restitution, isSensor)
      .createFixture()

    entityBody
  }

  override def changeCollisions(entity: Entity, entityType: Short): Unit = synchronized {
    this.entitiesToBeChanged = (entity, entityType) :: this.entitiesToBeChanged
  }

  override def applyEntityCollisionChanges(): Unit = synchronized {
    for (change <- this.entitiesToBeChanged) {
      change._1.getEntityBody.setCollisions(change._2)
      change._1.getEntityBody.createFixture()
    }
    this.entitiesToBeChanged = List.empty
  }
}


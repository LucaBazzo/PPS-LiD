package model.helpers

import _root_.utils.ApplicationConstants.HERO_SIZE
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d._
import com.badlogic.gdx.physics.box2d.joints.{RevoluteJointDef, WeldJointDef}
import model._
import model.attack.{AttackStrategy, ContactAttack, HeroAttackStrategyImpl, MeleeAttack, RangedAttack}
import model.collisions.ImplicitConversions._
import model.collisions._
import model.entities.EnemyType.EnemyType
import model.entities.ItemPools.ItemPools
import model.entities.Statistic.Statistic
import model.entities.{Entity, Statistic, _}
import model.movement.{CircularMovementStrategy, HeroMovementStrategy, MovementStrategy, PatrolAndStop, ProjectileTrajectory}

import scala.collection.mutable

trait EntitiesFactory {

  def setLevel(level: Level, pool: ItemPool)

  def createMobileEntity(size: (Float, Float) = (10, 10),
                         position: (Float, Float) = (0, 0)): Entity

  def createHeroEntity(): Hero

  def createEnemyEntity(position: (Float, Float),
                        size: (Float, Float),
                        statistics: mutable.Map[Statistic, Float],
                        score: Int,
                        enemyType: EnemyType): Enemy

  def createSlimeEnemy(position: (Float, Float)): Enemy

  def createSkeletonEnemy(position: (Float, Float)): Enemy

  def createWormEnemy(position: (Float, Float)): Enemy

  def createItem(PoolName: ItemPools,
                 size: (Float, Float) = (5f, 5f),
                 position: (Float, Float) = (0, 0),
                 collisions: Short = EntityType.Hero): Item

  def createPolygonalShape(size: (Float, Float)): Shape

  def createCircleShape(radius: Float): Shape

  def createImmobileEntity(size: (Float, Float) = (10, 10),
                           position: (Float, Float) = (0, 0),
                           collisions: Short = 0): Entity

  def createAttackPattern(rotatingBodySize: (Float, Float),
                          pivotPoint: (Float, Float),
                          rotatingBodyDistance: (Float, Float),
                          angularVelocity: Float,
                          startingAngle: Float,
                          owner: LivingEntity): MobileEntity

  def createEnemyProjectileAttack(size: (Float, Float) = (10, 10),
                                  position: (Float, Float) = (0, 0),
                                  targetPosition: (Float, Float) = (0, 0),
                                  owner: LivingEntity,
                                  attackStrategy: AttackStrategy): MobileEntity

  def createEnemySwordAttack(size: (Float, Float) = (1, 1),
                             position: (Float, Float) = (0, 0),
                             owner: LivingEntity): MobileEntity

  def createEnemyContactAttack(radius: Float,
                               owner: LivingEntity): MobileEntity

  def createJoint(pivotBody: Body, rotatingBody: Body): Joint

  def removeEntity(entity: Entity)

  def createBody(bodyDef: BodyDef): Body

  def destroyBody(body: Body)

  def destroyJoint(joint: Joint)

  def destroyBodies(): Unit

  def destroyJoints(): Unit

}

object EntitiesFactoryImpl extends EntitiesFactory {
  private var level: Level = _

  private var itemPool: ItemPool = _

  private var bodiesToBeDestroyed: List[Body] = List.empty
  private var jointsToBeDestroyed: List[Joint] = List.empty

  override def setLevel(level: Level, pool: ItemPool): Unit = {
    this.level = level
    this.itemPool = pool
  }

  override def createMobileEntity(size: (Float, Float) = (10, 10),
                                  position: (Float, Float) = (0, 0)): Entity = {

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.Mobile,
      EntityType.Immobile | EntityType.Enemy | EntityType.Hero, createPolygonalShape(size.PPM), position.PPM)

    val mobileEntity: MobileEntity = new MobileEntityImpl(EntityType.Mobile, entityBody, size.PPM)
    this.level.addEntity(mobileEntity)
    mobileEntity
  }

  override def createHeroEntity(): Hero = {
    val position: (Float, Float) = (10, 10)
    val size: (Float, Float) = HERO_SIZE

    val stats:mutable.Map[Statistic, Float] = mutable.Map(
      Statistic.Strength -> 10f,
      Statistic.MaxMovementSpeed -> 10f,
      Statistic.Acceleration -> 2f,
      Statistic.AttackSpeed -> 1f,
      Statistic.Health -> 100f,
      Statistic.CurrentHealth -> 100f
    )

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.Hero,
      EntityType.Immobile | EntityType.EnemyAttack | EntityType.Item, createPolygonalShape(size.PPM), position.PPM, friction = 0.8f)

    val hero: Hero = new HeroImpl(entityBody, size.PPM, stats)

    hero.setCollisionStrategy(new CollisionStrategyImpl())
    hero.setMovementStrategy(new HeroMovementStrategy(hero))
    hero.setAttackStrategy(new HeroAttackStrategyImpl(hero))

    this.level.addEntity(hero)
    hero
  }

  override def createSkeletonEnemy(position: (Float, Float)): Enemy = {
    val stats:mutable.Map[Statistic, Float] = mutable.Map(
      Statistic.Strength -> 10f,
      Statistic.MaxMovementSpeed -> 10f,
      Statistic.Acceleration -> 2f,
      Statistic.AttackSpeed -> 1f,
      Statistic.Health -> 11f,
      Statistic.CurrentHealth -> 11f
    )
    val size:(Float, Float) = (10f, 17f)
    val score: Int = 100

    val enemy:Enemy = createEnemyEntity(position, size, stats, score, EnemyType.Skeleton)

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

  override def createSlimeEnemy(position: (Float, Float)): Enemy = {
    val stats:mutable.Map[Statistic, Float] = mutable.Map(
      Statistic.Strength -> 10f,
      Statistic.MaxMovementSpeed -> 10f,
      Statistic.Acceleration -> 2f,
      Statistic.AttackSpeed -> 1f,
      Statistic.Health -> 11f,
      Statistic.CurrentHealth -> 11f
    )
    val size:(Float, Float) = (13f, 13f)
    val score: Int = 100

    val enemy:Enemy = createEnemyEntity(position, size, stats, score, EnemyType.Slime)

    val collisionStrategy:CollisionStrategy = new DoNothingOnCollision()
    val attackStrategy:AttackStrategy = new ContactAttack(enemy, stats,
      (e:Entity) => e.isInstanceOf[Hero])
    val movementStrategy:MovementStrategy = new PatrolAndStop(enemy, this.level, stats,
      (e:Entity) => e.isInstanceOf[Hero])

    enemy.setMovementStrategy(movementStrategy)
    enemy.setCollisionStrategy(collisionStrategy)
    enemy.setAttackStrategy(attackStrategy)
    enemy
  }

  override def createWormEnemy(position: (Float, Float)): Enemy = {
    val stats:mutable.Map[Statistic, Float] = mutable.Map(
      Statistic.Strength -> 5f,
      Statistic.MaxMovementSpeed -> 10f,
      Statistic.Acceleration -> 2f,
      Statistic.AttackSpeed -> 1f,
      Statistic.Health -> 11f,
      Statistic.CurrentHealth -> 11f
    )
    val size:(Float, Float) = (15f, 15f)
    val score: Int = 100

    val enemy:Enemy = createEnemyEntity(position, size, stats, score, EnemyType.Worm)

    val collisionStrategy:CollisionStrategy = new DoNothingOnCollision()
//    val attackStrategy:AttackStrategy = new RangedAttack(enemy, this.level, stats,
//      (e:Entity) => e.isInstanceOf[Hero])
//    val movementStrategy:MovementStrategy = new PatrolAndStop(enemy, this.level, stats,
//      (e:Entity) => e.isInstanceOf[Hero])
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
                                 stats:mutable.Map[Statistic, Float],
                                 score: Int,
                                 enemyType: EnemyType): Enemy = {

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.Enemy,
      EntityType.Immobile | EntityType.Sword, createPolygonalShape(size.PPM), position.PPM)
    val enemy:Enemy = new EnemyImpl(entityBody, size.PPM, stats, score, enemyType)
    this.level.addEntity(enemy)
    enemy
  }

  override def createItem(PoolName: ItemPools,
                          size: (Float, Float) = (5f, 5f),
                          position: (Float, Float) = (0, 0),
                          collisions: Short = EntityType.Hero): Item = {
    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityType.Item,
      collisions, createPolygonalShape(size.PPM), position.PPM)
    val item: Item = itemPool.getItem(entityBody, size.PPM, PoolName)
    item.setCollisionStrategy(new ItemCollisionStrategy())
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

  override def createImmobileEntity(size: (Float, Float) = (10, 10),
                                    position: (Float, Float) = (0, 0),
                                    collisions: Short = 0): Entity = {

    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityType.Immobile,
      collisions, createPolygonalShape(size.PPM), position.PPM)

    val immobileEntity: Entity = ImmobileEntity(EntityType.Immobile, entityBody, size.PPM)
    immobileEntity.setCollisionStrategy(new CollisionStrategyImpl())
    immobileEntity
  }

  override def createAttackPattern(rotatingBodySize: (Float, Float),
                                   pivotPoint: (Float, Float),
                                   rotatingBodyDistance: (Float, Float),
                                   angularVelocity: Float,
                                   startingAngle: Float = 0,
                                   owner:LivingEntity): MobileEntity = {

    val pivotSize = (2f, 2f)

    val pivotBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityType.Immobile,
      0, createPolygonalShape(pivotSize.PPM), pivotPoint, isSensor = true)

    val rotatingBodyPosition = (pivotPoint._1 + rotatingBodyDistance._1.PPM, pivotPoint._2 + rotatingBodyDistance._2.PPM)
    val rotatingBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.Sword,
      EntityType.Enemy, createPolygonalShape(rotatingBodySize.PPM), rotatingBodyPosition,
      startingAngle, gravity = false, 1, 0.3f, 0.5f, isSensor = true)

    val circularMobileEntity = new CircularMobileEntity(EntityType.Sword, rotatingBody, rotatingBodySize.PPM, pivotBody)
    circularMobileEntity.setMovementStrategy(
      new CircularMovementStrategy(circularMobileEntity, angularVelocity))
    circularMobileEntity.setCollisionStrategy(
      new ApplyDamage(circularMobileEntity, (e:Entity) => e.isInstanceOf[Enemy], owner.getStatistics))

    this.level.addEntity(circularMobileEntity)
    circularMobileEntity
  }

  override def createEnemyProjectileAttack(size: (Float, Float) = (10, 10),
                                           position: (Float, Float) = (0, 0),
                                           targetPosition: (Float, Float) = (0, 0),
                                           owner:LivingEntity,
                                           attackStrategy: AttackStrategy): MobileEntity = {
    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.EnemyAttack,
      EntityType.Immobile | EntityType.Hero, this.createCircleShape(size._1.PPM),
      position, isSensor = true)
    entityBody.getBody.setBullet(true)

    val arrowAttack: MobileEntity = new MobileEntityImpl(EntityType.EnemyAttack, entityBody, size)
    arrowAttack.setCollisionStrategy(new ApplyDamageAndDestroyOwner(arrowAttack, (e:Entity) => e.isInstanceOf[Hero],
      owner.getStatistics))
    arrowAttack.setMovementStrategy(new ProjectileTrajectory(arrowAttack, owner.getPosition,
      targetPosition, owner.getStatistics))

    this.level.addEntity(arrowAttack)
    arrowAttack
  }

  override def createEnemySwordAttack(size: (Float, Float) = (1, 1),
                                      position: (Float, Float) = (0, 0),
                                      owner:LivingEntity): MobileEntity = {

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.EnemyAttack,
      EntityType.Hero, this.createPolygonalShape(size.PPM), position, isSensor = true)

    val jointDef:WeldJointDef = new WeldJointDef()
    jointDef.initialize(owner.getBody, entityBody.getBody, owner.getBody.getPosition)
    this.level.getWorld.createJoint(jointDef)

    val swordAttack: MobileEntity = new MobileEntityImpl(EntityType.EnemyAttack, entityBody, size)
    swordAttack.setCollisionStrategy(new ApplyDamage(swordAttack, e => e.isInstanceOf[Hero], owner.getStatistics))
    this.level.addEntity(swordAttack)
    swordAttack
  }

  override def createEnemyContactAttack(radius: Float,
                                        owner:LivingEntity): MobileEntity = {
    val entityBody:EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.EnemyAttack,
      EntityType.Immobile | EntityType.Hero, this.createCircleShape(radius),
      (owner.getBody.getPosition.x, owner.getBody.getPosition.y), isSensor=true)

    val jointDef:WeldJointDef = new WeldJointDef()
    jointDef.initialize(owner.getBody, entityBody.getBody, owner.getBody.getPosition)
    this.level.getWorld.createJoint(jointDef)

    val contactAttack:MobileEntity = new MobileEntityImpl(EntityType.EnemyAttack, entityBody, (radius, radius))
    contactAttack.setCollisionStrategy(new ApplyDamage(contactAttack, e => e.isInstanceOf[Hero], owner.getStatistics))
    level.addEntity(contactAttack)
    contactAttack
  }

  override def createJoint(pivotBody: Body, rotatingBody: Body): Joint = {
    val rjd: RevoluteJointDef = new RevoluteJointDef()

    rjd.initialize(pivotBody, rotatingBody, pivotBody.getWorldCenter)

    this.level.getWorld.createJoint(rjd)
  }

  override def removeEntity(entity: Entity): Unit = this.level.removeEntity(entity)


  override def destroyJoint(joint: Joint): Unit = synchronized {
    this.jointsToBeDestroyed = joint :: this.jointsToBeDestroyed
  }

  override def destroyBody(body: Body): Unit = synchronized {
    this.bodiesToBeDestroyed = body :: this.bodiesToBeDestroyed
  }

  override def destroyBodies(): Unit = synchronized {
    for(body <- bodiesToBeDestroyed) {
      this.level.getWorld.destroyBody(body)
    }
    this.bodiesToBeDestroyed = List.empty
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

}
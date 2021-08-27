package model.helpers

import _root_.utils.ApplicationConstants.HERO_SIZE
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d._
import com.badlogic.gdx.physics.box2d.joints.{RevoluteJointDef, WeldJointDef}
import model._
import model.attack.{AttackStrategy, ContactAttack, HeroAttackStrategyImpl, MeleeAttack}
import model.collisions.ImplicitConversions._
import model.collisions._
import model.entities.EnemyType.EnemyType
import model.entities.ItemPools.ItemPools
import model.entities.Statistic.Statistic
import model.entities.{Entity, _}
import model.movement.{CircularMovementStrategy, HeroMovementStrategy, MovementStrategy, PatrolAndStop}

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

  def createEnemyProjectile(size: (Float, Float) = (10, 10),
                            position: (Float, Float) = (0, 0),
                            owner: LivingEntity): MobileEntity

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

    val mobileEntity: MobileEntity = new MobileEntityImpl(entityBody, size.PPM)
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
      Statistic.Health -> 10f
    )

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.Hero,
      EntityType.Immobile | EntityType.Enemy | EntityType.Item, createPolygonalShape(size.PPM), position.PPM, friction = 0.8f)

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
      Statistic.Health -> 100f
    )
    val size:(Float, Float) = (10f, 17.5f)
    val score: Int = 100

    val enemy:Enemy = createEnemyEntity(position, size, stats, score, EnemyType.Skeleton)

    val collisionStrategy:CollisionStrategy = new DoNothingOnCollision()
    val attackStrategy:AttackStrategy = new MeleeAttack(enemy, this.level, mutable.Map.empty,
      (e:Entity) => e.isInstanceOf[Hero])
    val movementStrategy:MovementStrategy = new PatrolAndStop(enemy, this.level, mutable.Map.empty,
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
      Statistic.Health -> 11f
    )
    val size:(Float, Float) = (9f, 7f)
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

    val immobileEntity: Entity = ImmobileEntity(entityBody, size.PPM)
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

    val circularMobileEntity = new CircularMobileEntity(rotatingBody, rotatingBodySize.PPM, pivotBody)
    circularMobileEntity.setMovementStrategy(
      new CircularMovementStrategy(circularMobileEntity, angularVelocity))
    circularMobileEntity.setCollisionStrategy(
      new ApplyDamage(owner, (e:Entity) => e.isInstanceOf[Enemy]))

    this.level.addEntity(circularMobileEntity)
    circularMobileEntity
  }

  override def createEnemyProjectile(size: (Float, Float) = (10, 10),
                                     position: (Float, Float) = (0, 0),
                                     owner:LivingEntity): MobileEntity = {

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.Mobile,
      EntityType.Immobile | EntityType.Hero | EntityType.Sword, this.createCircleShape(size._1.PPM),
      position, isSensor = true)

    val arrowEntity: MobileEntity = new MobileEntityImpl(entityBody, size.PPM)
    arrowEntity.setCollisionStrategy(new ApplyDamage(owner, (e:Entity) => e.isInstanceOf[Hero]))
    this.level.addEntity(arrowEntity)
    arrowEntity
  }

  override def createEnemySwordAttack(size: (Float, Float) = (1, 1),
                                      position: (Float, Float) = (0, 0),
                                      owner:LivingEntity): MobileEntity = {

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.Sword,
      EntityType.Hero, this.createPolygonalShape(size.PPM), position, isSensor = true)

    val jointDef:WeldJointDef = new WeldJointDef()
    jointDef.initialize(owner.getBody, entityBody.getBody, owner.getBody.getPosition)
    this.level.getWorld.createJoint(jointDef)

    val swordAttack: MobileEntity = new MobileEntityImpl(entityBody, size)
    swordAttack.setCollisionStrategy(new ApplyDamage(owner, e => e.isInstanceOf[Hero]))
    this.level.addEntity(swordAttack)
    swordAttack
  }

  override def createEnemyContactAttack(radius: Float,
                                        owner:LivingEntity): MobileEntity = {
    val entityBody:EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.Enemy,
      EntityType.Immobile | EntityType.Hero, this.createCircleShape(radius),
      (owner.getBody.getPosition.x, owner.getBody.getPosition.y), isSensor=true)

    val jointDef:WeldJointDef = new WeldJointDef()
    jointDef.initialize(owner.getBody, entityBody.getBody, owner.getBody.getPosition)
    this.level.getWorld.createJoint(jointDef)

    val contactAttack:MobileEntity = new MobileEntityImpl(entityBody, (radius, radius))
    contactAttack.setCollisionStrategy(new ApplyDamage(owner, e => e.isInstanceOf[Hero]))
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
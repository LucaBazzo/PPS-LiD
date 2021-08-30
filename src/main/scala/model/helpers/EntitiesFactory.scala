package model.helpers

import _root_.utils.ApplicationConstants._
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d._
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef
import model._
import model.attack.{HeroAttackStrategyImpl, RangedArrowAttack}
import model.collisions.ImplicitConversions._
import model.collisions.{ApplyDamageAndDestroyEntity, CollisionStrategyImpl, DoNothingOnCollision, EntityType, ItemCollisionStrategy}
import model.entities.EntityId.EntityId
import model.collisions.{CollisionStrategyImpl, DoNothingOnCollision, DoorCollisionStrategy, EntityType, ItemCollisionStrategy}
import model.entities.ItemPools.ItemPools
import model.entities.Statistic.Statistic
import model.entities.Statistic
import model.entities.Statistic.{Defence, Statistic}
import model.entities.{Entity, _}
import model.movement.{ArrowMovementStrategy, CircularMovementStrategy, HeroMovementStrategy, PatrolAndStopIfFacingHero}

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

  def createEnemyEntity(): Enemy

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

  def createImmobileEnemy(entityType: EntityId = EntityId.Immobile,
                          size: (Float, Float) = (10, 10),
                          position: (Float, Float) = (0, 0),
                          entityCollisionBit: Short = EntityType.Enemy,
                          collisions: Short = 0): Entity

  def createDoor(size: (Float, Float) = (10, 10),
                          position: (Float, Float) = (0, 0),
                          collisions: Short = 0): Entity

  def createAttackPattern(entityType: EntityId = EntityId.Mobile,
                          rotatingBodySize: (Float, Float) = (1,1),
                          pivotPoint: (Float, Float) = (0,0),
                          rotatingBodyDistance: (Float, Float) = (0,0),
                          angularVelocity: Float = 0,
                          startingAngle: Float = 0): MobileEntity

  def createEnemyProjectile(entityType: EntityId = EntityId.Mobile,
                            size: (Float, Float) = (10, 10),
                            position: (Float, Float) = (0, 0)): MobileEntity

  def createArrowProjectile(entity: LivingEntity): MobileEntity

  def createJoint(pivotBody: Body, rotatingBody: Body): Joint

  def removeEntity(entity: Entity)

  def createBody(bodyDef: BodyDef): Body
  def destroyBody(body: Body)
  def destroyJoint(joint: Joint)
  def destroyBodies(): Unit
  def changeCollisions(entity: Entity, entityType: Short): Unit
  def applyEntityCollisionChanges(): Unit
}

object EntitiesFactoryImpl extends EntitiesFactory {

  private var level: Level = _
  private var bodiesToBeDestroyed: List[Body] = List.empty
  private var entitiesToBeChanged: List[(Entity, Short)] = List.empty
  private var itemPool: ItemPool = _

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
    val position: (Float, Float) = (10, 10)
    val size: (Float, Float) = HERO_SIZE

    val statistic: Map[Statistic, Float] = Map(
      Statistic.Health -> 1000,
      Statistic.CurrentHealth -> 1000,
      Statistic.Strength -> 10,
      Statistic.MovementSpeed -> 1,
      Statistic.Defence -> 0)

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.Hero,
      EntityType.Immobile | EntityType.Enemy | EntityType.Item | EntityType.Door, createPolygonalShape(size.PPM), position.PPM, friction = 0.8f)

    val hero: Hero = new HeroImpl(EntityId.Hero, entityBody, size.PPM, statistic)

    hero.setCollisionStrategy(new CollisionStrategyImpl())
    hero.setMovementStrategy(new HeroMovementStrategy(hero, statistic(Statistic.MovementSpeed)))
    hero.setAttackStrategy(new HeroAttackStrategyImpl(hero, statistic(Statistic.Strength)))

    this.level.addEntity(hero)
    hero
  }

  override def createEnemyEntity(): Enemy = {
    val position: (Float, Float) = (40f, 150f)
    val size: (Float, Float) = (10f, 10f)

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.Enemy,
      EntityType.Immobile | EntityType.Sword | EntityType.Hero | EntityType.Door, createPolygonalShape(size.PPM), position.PPM)

    val enemy:Enemy = new EnemyImpl(EntityId.Enemy, entityBody, size.PPM, new HashMap[Statistic, Float]())
    enemy.setCollisionStrategy(new DoNothingOnCollision())
//    enemy.setAttackStrategy(new DoNotAttack())
//    enemy.setAttackStrategy(new ContactAttackStrategy(enemy, level.getEntity(e => e.isInstanceOf[HeroImpl]), world, level))
//    enemy.setAttackStrategy(new MeleeAttackStrategy(enemy, level.getEntity(e => e.isInstanceOf[HeroImpl]), world, level))
    enemy.setAttackStrategy(new RangedArrowAttack(enemy,
      level.getEntity(e => e.isInstanceOf[Hero]), this.level.getWorld))
    enemy.setMovementStrategy(new PatrolAndStopIfFacingHero(enemy, this.level.getWorld,
      level.getEntity(e => e.isInstanceOf[Hero]) ))

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

    val immobileEntity: Entity = ImmobileEntity(entityBody, size.PPM)
    immobileEntity.setCollisionStrategy(new DoorCollisionStrategy(immobileEntity.asInstanceOf[ImmobileEntity]))
    this.level.addEntity(immobileEntity)
    immobileEntity
  }

  //TODO temporanero, solo per test sulle collisioni
  override def createImmobileEnemy(entityType: EntityId = EntityId.Immobile,
                                   size: (Float, Float) = (10, 10),
                                   position: (Float, Float) = (0, 0),
                                   entityCollisionBit: Short = EntityType.Enemy,
                                   collisions: Short = 0): Entity = {

    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, entityCollisionBit,
      collisions, createPolygonalShape(size.PPM), position.PPM)

    val immobileEntity: Entity = ImmobileEntity(entityType, entityBody, size.PPM)
    immobileEntity.setCollisionStrategy(new CollisionStrategyImpl())
    immobileEntity
  }

  override def createAttackPattern(entityType: EntityId = EntityId.Mobile,
                                   rotatingBodySize: (Float, Float),
                                   pivotPoint: (Float, Float),
                                   rotatingBodyDistance: (Float, Float),
                                   angularVelocity: Float,
                                   startingAngle: Float = 0): MobileEntity = {

    val pivotSize = (2f, 2f)

    val pivotBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityType.Immobile,
      0, createPolygonalShape(pivotSize.PPM), pivotPoint)

    val rotatingBodyPosition = (pivotPoint._1 + rotatingBodyDistance._1.PPM, pivotPoint._2 + rotatingBodyDistance._2.PPM)
    val rotatingBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.Sword,
      EntityType.Enemy, createPolygonalShape(rotatingBodySize.PPM), rotatingBodyPosition,
      startingAngle, gravity = false, 1, 0.3f, 0.5f)

    val circularMobileEntity = new CircularMobileEntity(entityType, rotatingBody, rotatingBodySize.PPM, new HashMap[Statistic, Float](), pivotBody)
    circularMobileEntity.setMovementStrategy(new CircularMovementStrategy(circularMobileEntity, angularVelocity))
    circularMobileEntity.setCollisionStrategy(new CollisionStrategyImpl)

    this.level.addEntity(circularMobileEntity)
    circularMobileEntity
  }

  override def createEnemyProjectile(entityType: EntityId = EntityId.Mobile,
                                     size: (Float, Float) = (10, 10),
                                     position: (Float, Float) = (0, 0)): MobileEntity = {

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.Mobile,
      EntityType.Immobile | EntityType.Hero | EntityType.Sword, this.createCircleShape(size._1.PPM), position, isSensor = true)

    val arrowEntity: TimedAttack = new TimedAttack(entityType, entityBody, size.PPM, 1000, new HashMap[Statistic, Float]())
    this.level.addEntity(arrowEntity)
    arrowEntity
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
    arrow.setCollisionStrategy(new ApplyDamageAndDestroyEntity(arrow, null))
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

  override def destroyJoint(joint: Joint): Unit = this.level.getWorld.destroyJoint(joint)

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

//  override def createEnemyEntity(position: (Float, Float), size:Float): EnemyImpl = {
//
//    val bodyDef: BodyDef = new BodyDef()
//    bodyDef.position.set(position._1, position._2)
//    bodyDef.`type` = BodyDef.BodyType.DynamicBody
//
//    val body: Body = world.createBody(bodyDef)
//
//    val fixtureDef: FixtureDef = new FixtureDef()
//    val shape: CircleShape = new CircleShape()
//    shape.setRadius(size)
//    fixtureDef.shape = shape
//    fixtureDef.filter.categoryBits = EntitiesBits.ENEMY_CATEGORY_BIT
//    fixtureDef.filter.maskBits = EntitiesBits.ENEMY_COLLISIONS_MASK
//
//    body.createFixture(fixtureDef)
//
//    val enemy:EnemyImpl = EnemyImpl(body, (size,size))
//
//    enemy.setCollisionStrategy(new DoNothingOnCollision())
//
////    enemy.setAttackStrategy(new DoNotAttack())
////    enemy.setAttackStrategy(new ContactAttackStrategy(enemy, level.getEntity(e => e.isInstanceOf[HeroImpl]), world, level))
////    enemy.setAttackStrategy(new MeleeAttackStrategy(enemy, level.getEntity(e => e.isInstanceOf[HeroImpl]), world, level))
//    enemy.setAttackStrategy(new RangedArrowAttack(enemy, level.getEntity(e => e.isInstanceOf[HeroImpl]), world, level))
//
//    enemy.setMovementStrategy(new PatrolAndStopIfFacingHero(enemy, world, level.getEntity(e => e.isInstanceOf[HeroImpl]) ))
//
//    enemy
//  }
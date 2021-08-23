package model.helpers

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d._
import model.attack.{HeroAttackStrategyImpl, RangedArrowAttack}
import model.movement.{CircularMovementStrategy, HeroMovementStrategy, PatrolAndStopIfFacingHero}
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef
import model._
import model.collisions.ImplicitConversions._
import model.collisions.{CollisionStrategyImpl, DoNothingOnCollision, EntityType, ItemCollisionStrategy}
import model.entities.ItemPools.ItemPools
import model.entities.{Entity, _}

trait EntitiesFactory {

  def setLevel(level: Level, pool: ItemPool)

  def createMobileEntity(size: (Float, Float) = (1, 1),
                         position: (Float, Float) = (0, 0)): Entity

  def createHeroEntity(): Hero

  def createEnemyEntity(): Enemy

  def createItem(PoolName: ItemPools,
                 size: (Float, Float) = (0.5f, 0.5f),
                 position: (Float, Float) = (0, 0),
                 collisions: Short = EntityType.Hero): Item

  def createPolygonalShape(size: (Float, Float)): Shape
  def createCircleShape(radius: Float): Shape

  def createImmobileEntity(size: (Float, Float) = (1, 1),
                           position: (Float, Float) = (0, 0),
                           collisions: Short = 0): Entity

  def createImmobileEnemy(size: (Float, Float) = (1, 1),
                          position: (Float, Float) = (0, 0),
                          collisions: Short = 0): Entity

  def createAttackPattern(rotatingBodySize: (Float, Float),
                          pivotPoint: (Float, Float),
                          rotatingBodyDistance: (Float, Float),
                          angularVelocity: Float,
                          startingAngle: Float): MobileEntity

  def createEnemyProjectile(size: (Float, Float) = (1, 1),
                            position: (Float, Float) = (0, 0)): MobileEntity

  def createJoint(pivotBody: Body, rotatingBody: Body): Joint

  def removeEntity(entity: Entity)

  def createBody(bodyDef: BodyDef): Body
  def destroyBody(body: Body)
  def destroyJoint(joint: Joint)
}

object EntitiesFactoryImpl extends EntitiesFactory {

  private var level: Level = _

  private var itemPool: ItemPool = _

  override def setLevel(level: Level, pool: ItemPool): Unit = {
    this.level = level
    this.itemPool = pool
  }

  override def createMobileEntity(size: (Float, Float) = (1, 1),
                                  position: (Float, Float) = (0, 0)): Entity = {

    val shape: PolygonShape = new PolygonShape()
    shape.setAsBox(size._1, size._2)

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.Mobile,
      EntityType.Immobile | EntityType.Enemy | EntityType.Hero, shape, size, position)

    val mobileEntity: MobileEntity = new MobileEntityImpl(entityBody, size)
    this.level.addEntity(mobileEntity)
    mobileEntity
  }

  override def createHeroEntity(): Hero = {
    val position: (Float, Float) = (1, 1)
    val size: (Float, Float) = (0.85f, 1.4f)

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.Hero,
      EntityType.Immobile | EntityType.Enemy | EntityType.Item, createPolygonalShape(size), size, position, friction = 0.8f)

    val hero: Hero = new HeroImpl(entityBody, size)

    hero.setCollisionStrategy(new CollisionStrategyImpl())
    hero.setMovementStrategy(new HeroMovementStrategy(hero))
    hero.setAttackStrategy(new HeroAttackStrategyImpl(hero))

    this.level.addEntity(hero)
    hero
  }

  override def createEnemyEntity(): Enemy = {
    val position: (Float, Float) = (4, 15)
    val size: (Float, Float) = (1f, 1f)

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.Enemy,
      EntityType.Immobile | EntityType.Sword | EntityType.Hero, createPolygonalShape(size), size, position)

    val enemy:Enemy = new EnemyImpl(entityBody, size)
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
                          size: (Float, Float) = (0.5f, 0.5f),
                          position: (Float, Float) = (0, 0),
                          collisions: Short = EntityType.Hero): Item = {
    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityType.Item,
      collisions, createPolygonalShape(size), size, position)
    val item: Item = itemPool.getItem(entityBody, size, PoolName)
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

  override def createImmobileEntity(size: (Float, Float) = (1, 1),
                                    position: (Float, Float) = (0, 0),
                                    collisions: Short = 0): Entity = {

    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityType.Immobile,
      collisions, createPolygonalShape(size), size, position)

    val immobileEntity: Entity = ImmobileEntity(entityBody, size)
    immobileEntity.setCollisionStrategy(new CollisionStrategyImpl())
    immobileEntity
  }

  //TODO temporanero, solo per test sulle collisioni
  override def createImmobileEnemy(size: (Float, Float) = (1, 1),
                                    position: (Float, Float) = (0, 0),
                                    collisions: Short = 0): Entity = {

    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityType.Enemy,
      collisions, createPolygonalShape(size), size, position)

    val immobileEntity: Entity = ImmobileEntity(entityBody, size)
    immobileEntity.setCollisionStrategy(new CollisionStrategyImpl())
    immobileEntity
  }

  override def createAttackPattern(rotatingBodySize: (Float, Float),
                                   pivotPoint: (Float, Float),
                                   rotatingBodyDistance: (Float, Float),
                                   angularVelocity: Float,
                                   startingAngle: Float = 0): MobileEntity = {

    val pivotSize = (0.2f, 0.2f)

    val pivotBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityType.Immobile,
      0, createPolygonalShape(pivotSize), pivotSize, pivotPoint)

    val rotatingBodyPosition = (pivotPoint._1 + rotatingBodyDistance._1, pivotPoint._2 + rotatingBodyDistance._2)
    val rotatingBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.Sword,
      EntityType.Enemy, createPolygonalShape(rotatingBodySize), rotatingBodySize, rotatingBodyPosition,
      startingAngle, gravity = false, 1, 0.3f, 0.5f)

    val circularMobileEntity = new CircularMobileEntity(rotatingBody, rotatingBodySize, pivotBody)
    circularMobileEntity.setMovementStrategy(new CircularMovementStrategy(circularMobileEntity, angularVelocity))
    circularMobileEntity.setCollisionStrategy(new CollisionStrategyImpl)

    this.level.addEntity(circularMobileEntity)
    circularMobileEntity
  }

override def createEnemyProjectile(size: (Float, Float) = (1, 1),
                                  position: (Float, Float) = (0, 0)): MobileEntity = {

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityType.Mobile,
      EntityType.Immobile | EntityType.Hero | EntityType.Sword, this.createCircleShape(size._1), size, position, isSensor = true)

    val arrowEntity: TimedAttack = new TimedAttack(entityBody, size, 1000)
    this.level.addEntity(arrowEntity)
    arrowEntity
  }

  override def createJoint(pivotBody: Body, rotatingBody: Body): Joint = {
    val rjd: RevoluteJointDef = new RevoluteJointDef()

    rjd.initialize(pivotBody, rotatingBody, pivotBody.getWorldCenter)

    this.level.getWorld.createJoint(rjd)
  }

  override def removeEntity(entity: Entity): Unit = this.level.removeEntity(entity)

  override def destroyBody(body: Body): Unit = this.level.getWorld.destroyBody(body)

  override def destroyJoint(joint: Joint): Unit = this.level.getWorld.destroyJoint(joint)

  override def createBody(bodyDef: BodyDef): Body = this.level.getWorld.createBody(bodyDef)


  private def defineEntityBody(bodyType: BodyType,
                               entityType: Short,
                               collisions: Short,
                               shape: Shape,
                               size: (Float, Float),
                               position: (Float, Float),
                               angle: Float = 0,
                               gravity: Boolean = true,
                               density: Float = 0,
                               friction: Float = 0.2f,
                               restitution: Float = 0,
                               isSensor: Boolean = false): EntityBody = {

    val entityBody: EntityBody = new EntityBodyImpl()

    entityBody.createBody(bodyType, size, position, angle, gravity)
      .setEntityType(entityType)
      .setCollisions(collisions)
      .setShape(shape)
      .setFixtureValues(density, friction, restitution, isSensor)
      .createFixture()

    entityBody
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
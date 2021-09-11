package model.helpers

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d._
import com.badlogic.gdx.physics.box2d.joints.{RevoluteJointDef, WeldJointDef}
import model._
import model.attack._
import model.collisions.ImplicitConversions._
import model.collisions.{EntityCollisionBit, _}
import model.entities.EntityType.EntityType
import model.entities.ItemPools.ItemPools
import model.entities.Statistic.Statistic
import model.entities.{Entity, Statistic, _}
import model.helpers.EntitiesUtilities.isEntityOnTheLeft
import model.movement._
import _root_.utils.EnemiesConstants._
import _root_.utils.ApplicationConstants._
import _root_.utils.HeroConstants._

import scala.collection.immutable.HashMap

trait EntitiesFactory {

  def setLevel(level: Level, pool: ItemPool): Unit

  def createMobileEntity(entityType: EntityType = EntityType.Mobile,
                         size: (Float, Float) = (10, 10),
                         position: (Float, Float) = (0, 0),
                         entityCollisionBit: Short = EntityCollisionBit.Mobile,
                         collisions: Short = 0,
                         gravityScale: Float = 1.0f): MobileEntity

  def createHeroEntity(statistics: Option[Map[Statistic, Float]]): Hero

  def createHeroFeet(hero: Hero): Unit

  def createEnemyEntity(position: (Float, Float),
                        size: (Float, Float),
                        stats: Map[Statistic, Float],
                        score: Int,
                        entityId: EntityType): Enemy

  def createSlimeEnemy(position: (Float, Float)): Enemy

  def createSkeletonEnemy(position: (Float, Float)): Enemy

  def createWormEnemy(position: (Float, Float)): Enemy

  def createWizardBossEnemy(position: (Float, Float)): Enemy

  def createItem(PoolName: ItemPools,
                 size: (Float, Float) = (10f, 10f),
                 position: (Float, Float) = (0, 0),
                 collisions: Short = EntityCollisionBit.Hero): Item

  def createPolygonalShape(size: (Float, Float)): Shape

  def createCircleShape(radius: Float): Shape

  def createEdgeShape(size: (Float, Float)): Shape

  def createImmobileEntity(entityType: EntityType = EntityType.Immobile,
                           size: (Float, Float) = (10, 10),
                           position: (Float, Float) = (0, 0),
                           entityCollisionBit: Short = EntityCollisionBit.Immobile,
                           collisions: Short = 0): Entity

  def createDoor(size: (Float, Float) = (10, 10),
                 position: (Float, Float) = (0, 0),
                 collisions: Short = 0): Entity

  def createChest(size: (Float, Float) = (70, 70),
                  position: (Float, Float) = (0,0)): Entity

  def createPlatform(position: (Float, Float),
                     size: (Float, Float)): Entity

  def createLadder(position: (Float, Float),
                     size: (Float, Float)): Entity

  def createWaterPool(position: (Float, Float),
                      size: (Float, Float)): Entity

  def createLavaPool(position: (Float, Float),
                      size: (Float, Float)): Entity

  def createAttackPattern(entityType: EntityType = EntityType.Mobile,
                          rotatingBodySize: (Float, Float) = (1, 1),
                          pivotPoint: (Float, Float) = (0, 0),
                          rotatingBodyDistance: (Float, Float) = (0, 0),
                          angularVelocity: Float = 0,
                          startingAngle: Float = 0,
                          sourceEntity: LivingEntity): MobileEntity

  def createFireballAttack(sourceEntity: LivingEntity, targetEntity: Entity): MobileEntity

  def createMeleeAttack(sourceEntity: LivingEntity,
                        targetEntity: Entity,
                        size: (Float, Float) = (23, 23),
                        offset: (Float, Float) = (20, 5)): MobileEntity

  def createArrowProjectile(entity: LivingEntity): MobileEntity

  def createJoint(pivotBody: Body, rotatingBody: Body): Joint

  def removeEntity(entity: Entity): Unit

  def createBody(bodyDef: BodyDef): Body

  def destroyBody(body: Body): Unit

  def destroyBodies(): Unit

  def changeCollisions(entity: Entity, entityType: Short): Unit

  def applyEntityCollisionChanges(): Unit

  // TODO: convertire createEnemies in createSpawnZone e lasicare a levelImpl la generazione dei nemici nelle zone di spawn
  def createEnemies(size: (Float, Float) = (10, 10),
                    position: (Float, Float) = (0, 0)): Unit

  def addPendingEntityCreation(r:() => Unit): Unit
  def createPendingEntities(): Unit

  def setEntitiesSetter(entitySetter: EntitiesSetter)

  def changeHeroFixture(hero: Hero, newSize: (Float, Float), addCoordinates: (Float, Float) = (0,0))
}

object EntitiesFactoryImpl extends EntitiesFactory {
  private var level: Level = _
  private var entitiesToBeChanged: List[(Entity, Short)] = List.empty
  private var itemPool: ItemPool = _
  private var bodiesToBeDestroyed: List[Body] = List.empty
  private val collisionMonitor: CollisionMonitor = new CollisionMonitorImpl
  private var pendingEntitiesCreation: List[() => Unit] = List.empty
  private var entitiesSetter: EntitiesSetter = _

  override def setLevel(level: Level, pool: ItemPool): Unit = {
    this.level = level
    this.itemPool = pool
  }

  override def createMobileEntity(entityType: EntityType = EntityType.Mobile,
                                  size: (Float, Float) = (10, 10),
                                  position: (Float, Float) = (0, 0),
                                  entityCollisionBit: Short = EntityCollisionBit.Mobile,
                                  collisions: Short = 0,
                                  gravityScale: Float = 1.0f): MobileEntity = {

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, entityCollisionBit,
      collisions, createPolygonalShape(size.PPM), position.PPM, gravityScale = gravityScale)

    val mobileEntity: MobileEntity = new MobileEntityImpl(entityType, entityBody, size.PPM, new HashMap[Statistic, Float]())
    this.level.addEntity(mobileEntity)
    mobileEntity
  }

  override def createHeroEntity(statistics: Option[Map[Statistic, Float]]): Hero = {
    val position: (Float, Float) = HERO_OFFSET

    val size: (Float, Float) = HERO_SIZE

    var stats: Map[Statistic, Float] = HERO_STATISTICS_DEFAULT
    if(statistics.nonEmpty){
      stats = statistics.get
    }

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityCollisionBit.Hero,
      EntityCollisionBit.Immobile | EntityCollisionBit.Ladder | EntityCollisionBit.Platform | EntityCollisionBit.Pool |
        EntityCollisionBit.Item | EntityCollisionBit.Door | EntityCollisionBit.EnemyAttack, createPolygonalShape(size.PPM), position.PPM, friction = 0.8f)

    val hero: Hero = new HeroImpl(EntityType.Hero, entityBody, size.PPM, stats)

    hero.setCollisionStrategy(new CollisionStrategyImpl())
    hero.setMovementStrategy(new HeroMovementStrategy(hero, stats(Statistic.MovementSpeed)))
    hero.setAttackStrategy(new HeroAttackStrategy(hero, stats(Statistic.Strength)))

    this.createHeroFeet(hero)

    this.level.addEntity(hero)
    hero
  }

  override def changeHeroFixture(hero: Hero, newSize: (Float, Float), addCoordinates: (Float, Float) = (0,0)): Unit = {
    hero.getEntityBody
      .setShape(createPolygonalShape(newSize.PPM))
      .createFixture()

    hero.getEntityBody.addCoordinates(0, -hero.getSize._2 + newSize._2.PPM)

    hero.setSize(newSize.PPM)

    EntitiesFactoryImpl.createHeroFeet(hero)
  }

  override def createHeroFeet(hero: Hero): Unit = {
    if(hero.getFeet.nonEmpty) {
      EntitiesFactoryImpl.destroyBody(hero.getFeet.get.getBody)
      EntitiesFactoryImpl.removeEntity(hero.getFeet.get)
    }

    val feetSize: (Float, Float) = (8.0f, 0.2f)
    val bodyPosition = hero.getPosition - (0, hero.getSize._2)
    val feetBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityCollisionBit.Hero,
      EntityCollisionBit.Immobile | EntityCollisionBit.Platform | EntityCollisionBit.Door, createEdgeShape(feetSize),
      bodyPosition, gravityScale = 0, friction = 0.8f)
    EntitiesFactoryImpl.createJoint(hero.getBody, feetBody.getBody)

    val heroFeet: MobileEntity = new MobileEntityImpl(EntityType.Mobile, feetBody, feetSize.PPM)
    heroFeet.setCollisionStrategy(new FeetCollisionStrategy())

    hero.setFeet(heroFeet)
    this.level.addEntity(heroFeet)
  }

  override def createPlatform(position: (Float, Float),
                              size: (Float, Float)): Entity = {

    val upperSize = (size._1 - 2, size._2)
    val upperPosition = (position._1, position._2 + 1)

    val lowerSize = (size._1 - 2, size._2)
    val lowerPosition = (position._1, position._2 - 5)

    //messo per riferimento a prima da rimuovere quando si vede che non servirà a nulla
    /*val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Platform,
      EntityCollisionBit.Hero | EntityCollisionBit.Enemy, createPolygonalShape((size._1.PPM , size._2.PPM + 2.PPM )),
      (position._1.PPM, position._2.PPM - 2.PPM))*/

    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Platform,
      EntityCollisionBit.Hero | EntityCollisionBit.Enemy, createPolygonalShape((size._1.PPM , size._2.PPM)),
      (position._1.PPM, position._2.PPM))

    val entityBodyUpper: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Platform,
      EntityCollisionBit.Hero | EntityCollisionBit.Enemy, createPolygonalShape(upperSize.PPM), upperPosition.PPM, isSensor = true)

    val entityBodyLower: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Platform,
      EntityCollisionBit.Hero | EntityCollisionBit.Enemy, createPolygonalShape(lowerSize.PPM), lowerPosition.PPM, isSensor = true)

    val immobileEntity: ImmobileEntity = ImmobileEntity(EntityType.Platform, entityBody, size.PPM)
    immobileEntity.setCollisionStrategy(new DoNothingOnCollision)
    this.level.addEntity(immobileEntity)

    val immobileEntityUpper: ImmobileEntity = ImmobileEntity(EntityType.Platform, entityBodyUpper, upperSize.PPM)
    val immobileEntityLower: ImmobileEntity = ImmobileEntity(EntityType.Platform, entityBodyLower, lowerSize.PPM)

    immobileEntityUpper.setCollisionStrategy(new UpperPlatformCollisionStrategy(immobileEntity, immobileEntityUpper, immobileEntityLower, this.collisionMonitor))
    this.level.addEntity(immobileEntityUpper)

    immobileEntityLower.setCollisionStrategy(new LowerPlatformCollisionStrategy(immobileEntity, immobileEntityUpper, immobileEntityLower))
    this.level.addEntity(immobileEntityLower)

    immobileEntity
  }

  override def createLadder(position: (Float, Float),
                            size: (Float, Float)): Entity = {
    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Ladder,
      EntityCollisionBit.Hero | EntityCollisionBit.Enemy, createPolygonalShape(size.PPM), position.PPM, isSensor = true)

    val immobileEntity: ImmobileEntity = ImmobileEntity(EntityType.Ladder, entityBody, size.PPM)
    immobileEntity.setCollisionStrategy(new LadderCollisionStrategy(this.collisionMonitor))
    this.level.addEntity(immobileEntity)

    immobileEntity
  }

  override def createSkeletonEnemy(position: (Float, Float)): Enemy = {
    val size: (Float, Float) = (13f, 23f)
    val score: Int = 100
    val spawnPosition = (position._1, position._2+size._2*2)

    val enemy:Enemy = createEnemyEntity(spawnPosition, size, SKELETON_STATS, score, EntityType.EnemySkeleton)

    val behaviours:EnemyBehaviour = new EnemyBehaviourImpl(enemy, "",
      new DoNothingOnCollision(),
      new PatrolAndStop(enemy, this.level.getEntity(e => e.isInstanceOf[Hero])),
      new SkeletonAttack(enemy, this.level.getEntity(e => e.isInstanceOf[Hero])))
    enemy.setBehaviour(behaviours)
    enemy
  }

  override def createSlimeEnemy(position: (Float, Float)): Enemy = {
    val size:(Float, Float) = (13f, 13f)
    val score: Int = 100

    val enemy:Enemy = createEnemyEntity(position, size, SLIME_STATS, score, EntityType.EnemySlime)

    val behaviours:EnemyBehaviour = new EnemyBehaviourImpl(enemy, "",
      new DoNothingOnCollision(),
      new PatrolAndStop(enemy, this.level.getEntity(e => e.isInstanceOf[Hero])),
      new SlimeAttack(enemy, this.level.getEntity(e => e.isInstanceOf[Hero])))
    enemy.setBehaviour(behaviours)
    enemy
  }

  override def createWormEnemy(position: (Float, Float)): Enemy = {
    val size:(Float, Float) = (15f, 11f)
    val score: Int = 100

    val enemy:Enemy = createEnemyEntity(position, size, WORM_STATS, score, EntityType.EnemyWorm)

    val behaviours:EnemyBehaviour = new EnemyBehaviourImpl(enemy, "",
      new DoNothingOnCollision(),
      new PatrolPlatform(enemy, this.level.getEntity(e => e.isInstanceOf[Hero])),
      new WormAttack(enemy, this.level.getEntity(e => e.isInstanceOf[Hero])))

    enemy.setBehaviour(behaviours)
    enemy
  }

  override def createWizardBossEnemy(position: (Float, Float)): Enemy = {
    val size:(Float, Float) = (15f, 30f)
    val score: Int = 1000

    val enemy:Enemy = createEnemyEntity(position, size, WIZARD_BOSS_STATS, score, EntityType.EnemyBossWizard)

    val behaviours:EnemyBehaviour = new EnemyBehaviourImpl(enemy, "1", new DoNothingOnCollision, new DoNotMove(), new DoNotAttack() )
    val p2AttackStrategy = new WormAttack(enemy, this.level.getEntity(e => e.isInstanceOf[Hero]))
    behaviours.addBehaviour("2", new DoNothingOnCollision, new DoNotMove(), p2AttackStrategy)
    behaviours.addBehaviour("3",
      new DoNothingOnCollision,
      new PatrolAndStop(enemy, this.level.getEntity(e => e.isInstanceOf[Hero])),
      new SkeletonAttack(enemy, this.level.getEntity(e => e.isInstanceOf[Hero])))
    behaviours.addTransition("1", "2", new TimePredicate(5000))
    behaviours.addTransition("2", "3", new CompletedAttackPredicate(p2AttackStrategy, 3))
    behaviours.addTransition("3", "1", new HealthThresholdPredicate(enemy, 50))

    enemy.setBehaviour(behaviours)
    enemy
  }

  override def createEnemyEntity(position: (Float, Float),
                                 size: (Float, Float),
                                 stats: Map[Statistic, Float],
                                 score: Int,
                                 entityId: EntityType): Enemy = {

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityCollisionBit.Enemy,
      EntityCollisionBit.Immobile | EntityCollisionBit.Sword | EntityCollisionBit.Arrow,
      createPolygonalShape(size.PPM), position.PPM)
    val enemy:Enemy = new Enemy(entityId, entityBody, size.PPM, stats, score)
    this.level.addEntity(enemy)
    enemy
  }


  override def createItem(PoolName: ItemPools,
                          size: (Float, Float) = (5f, 5f),
                          position: (Float, Float) = (0, 0),
                          collisions: Short = EntityCollisionBit.Hero): Item = {
    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Item,
      collisions, createPolygonalShape(size.PPM), position.PPM)
    val item: Item = itemPool.getItem(entityBody, size, PoolName)
    item.setCollisionStrategy(new ItemCollisionStrategy(item, this.entitiesSetter))
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

  override def createEdgeShape(size: (Float, Float)): Shape = {
    val shape: EdgeShape = new EdgeShape()
    shape.set(size.PPM * -1, size.PPM * (1, -1))
    shape
  }

  override def createImmobileEntity(entityType: EntityType = EntityType.Immobile,
                                    size: (Float, Float) = (10, 10),
                                    position: (Float, Float) = (0, 0),
                                    entityCollisionBit: Short = EntityCollisionBit.Immobile,
                                    collisions: Short = 0): Entity = {

    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, entityCollisionBit,
      collisions, createPolygonalShape(size.PPM), position.PPM)

    val immobileEntity: Entity = ImmobileEntity(entityType, entityBody, size.PPM)
    immobileEntity.setCollisionStrategy(new CollisionStrategyImpl())
    this.level.addEntity(immobileEntity)
    immobileEntity
  }


  // TODO: convertire createEnemies in createSpawnZone e lasciare a levelImpl la generazione dei nemici nelle zone di spawn
  override def createEnemies(size: (Float, Float) = (10, 10),
                             position: (Float, Float) = (0, 0)):Unit =  {
    this.createSkeletonEnemy((position.x, position.y))
  }

  override def createDoor(size: (Float, Float) = (10, 10),
                          position: (Float, Float) = (0, 0),
                          collisions: Short = 0): Entity = {

    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Door,
      EntityCollisionBit.Hero | EntityCollisionBit.Sword, createPolygonalShape(size.PPM), position.PPM)

    val immobileEntity: Entity = ImmobileEntity(EntityType.Door, entityBody, size.PPM)
    immobileEntity.setCollisionStrategy(new DoorCollisionStrategy(immobileEntity.asInstanceOf[ImmobileEntity]))
    this.level.addEntity(immobileEntity)
    immobileEntity
  }

  override def createChest(size: (Float, Float), position: (Float, Float)): Entity = {
    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Door,
      EntityCollisionBit.Hero | EntityCollisionBit.Enemy, createPolygonalShape(size.PPM), position.PPM)

    val immobileEntity: ImmobileEntity = ImmobileEntity(EntityType.Chest, entityBody, size.PPM)
    immobileEntity.setCollisionStrategy(new ChestCollisionStrategy(immobileEntity))
    this.level.addEntity(immobileEntity)
    immobileEntity
  }


  override def createWaterPool(position: (Float, Float),
                               size: (Float, Float)): Entity = {
    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Pool,
      EntityCollisionBit.Hero | EntityCollisionBit.Sword, createPolygonalShape(size.PPM), position.PPM, isSensor = true)

    val immobileEntity: Entity = ImmobileEntity(EntityType.Water, entityBody, size.PPM)
    immobileEntity.setCollisionStrategy(new WaterCollisionStrategy)
    this.level.addEntity(immobileEntity)
    immobileEntity
  }

  override def createLavaPool(position: (Float, Float),
                               size: (Float, Float)): Entity = {
    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Pool,
      EntityCollisionBit.Hero | EntityCollisionBit.Sword, createPolygonalShape(size.PPM), position.PPM, isSensor = true)

    val immobileEntity: Entity = ImmobileEntity(EntityType.Lava, entityBody, size.PPM)

    immobileEntity.setCollisionStrategy(new LavaCollisionStrategy(this.collisionMonitor))
    this.level.addEntity(immobileEntity)
    immobileEntity
  }

  override def createAttackPattern(entityType: EntityType = EntityType.Mobile,
                                   rotatingBodySize: (Float, Float),
                                   pivotPoint: (Float, Float),
                                   rotatingBodyDistance: (Float, Float),
                                   angularVelocity: Float,
                                   startingAngle: Float = 0,
                                   sourceEntity: LivingEntity): MobileEntity = {

    val pivotSize = (2f, 2f)

    val pivotBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Immobile,
      0, createPolygonalShape(pivotSize.PPM), pivotPoint, isSensor = true)

    val rotatingBodyPosition = (pivotPoint._1 + rotatingBodyDistance._1.PPM, pivotPoint._2 + rotatingBodyDistance._2.PPM)
    val rotatingBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityCollisionBit.Sword,
      EntityCollisionBit.Enemy, createPolygonalShape(rotatingBodySize.PPM), rotatingBodyPosition,
      startingAngle, gravityScale = 0, 1, 0.3f, 0.5f, isSensor = true)

    val circularMobileEntity =
      new CircularMobileEntity(entityType, rotatingBody, rotatingBodySize.PPM, sourceEntity.getStatistics, pivotBody)
    circularMobileEntity.setMovementStrategy(
      new CircularMovementStrategy(circularMobileEntity, angularVelocity))
    circularMobileEntity.setCollisionStrategy(
      new ApplyDamage((e:Entity) => e.isInstanceOf[Enemy], sourceEntity.getStatistics))

    this.level.addEntity(circularMobileEntity)
    circularMobileEntity
  }

  override def createFireballAttack(sourceEntity: LivingEntity, targetEntity: Entity): MobileEntity = {

    val size = (10f, 10f)

    // compute bullet spawn point
    val offset:Float = if (isEntityOnTheLeft(sourceEntity, targetEntity))
      sourceEntity.getSize._1.PPM else -sourceEntity.getSize._1.PPM
    val position = sourceEntity.getBody.getWorldCenter.add(offset, 0)

    // create a body inside the game world
    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityCollisionBit.EnemyAttack,
      EntityCollisionBit.Immobile | EntityCollisionBit.Hero | EntityCollisionBit.Door,
      this.createCircleShape(size._1.PPM), (position.x, position.y), isSensor = true)
    entityBody.getBody.setBullet(true)

    // create an entity representing the bullet
    val attack: MobileEntity = new MobileEntityImpl(EntityType.AttackFireBall, entityBody,
      size.PPM, sourceEntity.getStatistics)
    attack.setFacing(attack.isFacingRight)

    // set entity behaviours
    attack.setCollisionStrategy(new ApplyDamageAndDestroyEntity(attack, (e:Entity) => e.isInstanceOf[Hero],
      sourceEntity.getStatistics))
    attack.setMovementStrategy(new WeightlessProjectileTrajectory(attack, (position.x, position.y),
      (targetEntity.getBody.getWorldCenter.x, targetEntity.getBody.getWorldCenter.y), sourceEntity.getStatistics))

    this.level.addEntity(attack)
    attack
  }

  override def createMeleeAttack(sourceEntity:LivingEntity,
                                 targetEntity:Entity,
                                 size: (Float, Float) = (23, 23),
                                 offset: (Float, Float) = (20, 5)): MobileEntity = {

    val pivotSize: (Float, Float) = (2f, 2f)

    // compute attack spawn point
    val attackXOffset:Float = if (isEntityOnTheLeft(sourceEntity, targetEntity)) -offset._1.PPM else +offset._1.PPM
    val attackYOffset:Float = offset._2.PPM
    val position = (sourceEntity.getBody.getWorldCenter.x+attackXOffset,
      sourceEntity.getBody.getWorldCenter.y+attackYOffset)

    // create a body inside the game world
    val pivotBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Immobile,
      0, createPolygonalShape(pivotSize.PPM), sourceEntity.getPosition, isSensor = true)

    // TODO rendere la entityBody shape configurabile
    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityCollisionBit.EnemyAttack,
      EntityCollisionBit.Hero, this.createPolygonalShape(size.PPM), position, isSensor = true)

    val jointDef:WeldJointDef = new WeldJointDef()
    jointDef.initialize(pivotBody.getBody, entityBody.getBody, pivotBody.getBody.getPosition)
    this.level.getWorld.createJoint(jointDef)

    // create an entity representing the melee attack
    val attack: MobileEntity = new MobileEntityImpl(EntityType.Mobile, entityBody, size.PPM, sourceEntity.getStatistics)

    // set entity behaviours
    attack.setCollisionStrategy(new ApplyDamage(e => e.isInstanceOf[Hero], sourceEntity.getStatistics))

    this.level.addEntity(attack)
    attack
  }

  override def createArrowProjectile(entity: LivingEntity): MobileEntity = {
    //TODO mettere a posto
    val size: (Float, Float) = (8, 1)

    var newPosition: (Float, Float) = entity.getPosition * PIXELS_PER_METER
    if(entity.isFacingRight)
      newPosition += (entity.getSize._1 * PIXELS_PER_METER + size._1, 0)
    else
      newPosition -= (entity.getSize._1 * PIXELS_PER_METER + size._1, 0)
    val arrow: MobileEntity = EntitiesFactoryImpl.createMobileEntity(EntityType.Arrow, size, newPosition, EntityCollisionBit.Arrow,
      EntityCollisionBit.Immobile | EntityCollisionBit.Enemy , gravityScale = 0)
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

  override def createBody(bodyDef: BodyDef): Body = this.level.getWorld.createBody(bodyDef)

  private def defineEntityBody(bodyType: BodyType,
                               entityType: Short,
                               collisions: Short,
                               shape: Shape,
                               position: (Float, Float),
                               angle: Float = 0,
                               gravityScale: Float = 1.0f,
                               density: Float = 0,
                               friction: Float = 0.2f,
                               restitution: Float = 0,
                               isSensor: Boolean = false): EntityBody = {

    val entityBody: EntityBody = new EntityBodyImpl()

    entityBody.createBody(bodyType, position, angle, gravityScale)
      .setEntityCollisionBit(entityType)
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

  override def addPendingEntityCreation(r:() => Unit):Unit = synchronized {
    this.pendingEntitiesCreation = r :: this.pendingEntitiesCreation
  }

  override def createPendingEntities(): Unit = synchronized {
    for(func <- this.pendingEntitiesCreation) {
      func.apply()
    }
    this.pendingEntitiesCreation = List.empty
  }

  override def setEntitiesSetter(entitySetter: EntitiesSetter): Unit = this.entitiesSetter = entitySetter
}


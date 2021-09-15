package model.helpers

import _root_.utils.ApplicationConstants._
import _root_.utils.EnemiesConstants._
import _root_.utils.HeroConstants._
import _root_.utils.CollisionConstants._
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d._
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef
import model._
import model.attack._
import model.collisions.ImplicitConversions._
import model.collisions.{EntityCollisionBit, _}
import model.entities.EntityType.EntityType
import model.entities.ItemPools.ItemPools
import model.entities.Statistic.Statistic
import model.entities.{Entity, Statistic, _}
import model.helpers.EntitiesUtilities.{isEntityOnTheLeft, isEntityOnTheRight}
import model.movement._

import scala.collection.immutable.HashMap

trait EntitiesFactory {

  def setEntitiesContainerMonitor(entitiesContainerMonitor: EntitiesContainerMonitor): Unit

  def setItemPool(pool: ItemPool): Unit

  def createMobileEntity(entityType: EntityType = EntityType.Mobile,
                         size: (Float, Float) = (10, 10),
                         position: (Float, Float) = (0, 0),
                         entityCollisionBit: Short = EntityCollisionBit.Mobile,
                         collisions: Short = 0,
                         gravityScale: Float = 1.0f): MobileEntity

  def createHeroEntity(statistics: Option[Map[Statistic, Float]]): Hero

  def createEnemyEntity(position: (Float, Float),
                        size: (Float, Float),
                        stats: Map[Statistic, Float],
                        statsModifiers: Map[Statistic, Float],
                        levelNumber: Int = 0,
                        score: Int,
                        entityId: EntityType): EnemyImpl

  def createSlimeEnemy(position: (Float, Float)): EnemyImpl

  def createSkeletonEnemy(position: (Float, Float)): EnemyImpl

  def createWormEnemy(position: (Float, Float)): EnemyImpl

  def createWizardBossEnemy(position: (Float, Float)): EnemyImpl

  def createItem(PoolName: ItemPools,
                 size: (Float, Float) = (10f, 10f),
                 position: (Float, Float) = (0, 0),
                 collisions: Short = EntityCollisionBit.Hero): Item

  def createPolygonalShape(size: (Float, Float), rounder:Boolean = false): Shape

  def createCircleShape(radius: Float): Shape

  def createImmobileEntity(entityType: EntityType = EntityType.Immobile,
                           size: (Float, Float) = (10, 10),
                           position: (Float, Float) = (0, 0),
                           entityCollisionBit: Short = EntityCollisionBit.Immobile,
                           collisions: Short = 0): Entity

  def createDoor(size: (Float, Float) = (10, 10),
                 position: (Float, Float) = (0, 0),
                 collisions: Short = 0): Entity

  def createBossDoor(size: (Float, Float) = (10, 10),
                 position: (Float, Float) = (0, 0),
                 collisions: Short = 0): Entity

  def createChest(size: (Float, Float) = (70, 70),
                  position: (Float, Float) = (0,0)): Entity

  def createPortal(size: (Float, Float) = (10,30),
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

  def createFireballAttack(sourceEntity: LivingEntity,
                           targetEntity: Entity,
                           size: (Float, Float) = (23, 23),
                           offset: (Float, Float) = (20, 5)): MobileEntity

  def createEnergyBallAttack(sourceEntity: LivingEntity,
                             targetEntity: Entity,
                             size: (Float, Float) = (1f, 1f),
                             offset: (Float, Float) = (0f, 0f)): MobileEntity

  def createMeleeAttack(sourceEntity: LivingEntity,
                        targetEntity: Entity,
                        size: (Float, Float) = (23, 23),
                        offset: (Float, Float) = (20, 5)): MobileEntity

  def createArrowProjectile(entity: LivingEntity): MobileEntity

  def pendingJointCreation(pivotBody: Body, rotatingBody: Body): Unit

  def removeEntity(entity: Entity): Unit

  def createBody(bodyDef: BodyDef): Body

  def pendingDestroyBody(body: Body): Unit

  def pendingChangeCollisions(entity: Entity, entityType: Short): Unit

  // TODO: convertire createEnemies in createSpawnZone e lasicare a levelImpl la generazione dei nemici nelle zone di spawn
  def spawnEnemies(size: (Float, Float) = (10, 10),
                   position: (Float, Float) = (0, 0)): Unit

  def spawnBoss(size: (Float, Float) = (10, 10),
                position: (Float, Float) = (0, 0)): Unit

  def addPendingFunction(function: () => Unit): Unit

  def applyPendingFunctions(): Unit

  def changeHeroFixture(hero: Hero, newSize: (Float, Float), addCoordinates: (Float, Float) = (0,0)): Unit

  def createAirAttackPattern(entityType: EntityType = EntityType.Mobile,
                             bodySize: (Float, Float),
                             bodyDistance: (Float, Float),
                             sourceEntity: LivingEntity): MobileEntity
}

object EntitiesFactoryImpl extends EntitiesFactory {
  private var itemPool: ItemPool = _
  private val collisionMonitor: CollisionMonitor = new CollisionMonitorImpl
  private var pendingFunctions: List[() => Unit] = List.empty
  private var entitiesContainer: EntitiesContainerMonitor = _

  override def setEntitiesContainerMonitor(entitiesContainerMonitor: EntitiesContainerMonitor): Unit =
    this.entitiesContainer = entitiesContainerMonitor

  override def setItemPool(pool: ItemPool): Unit = {
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
    this.entitiesContainer.addEntity(mobileEntity)
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
      HERO_COLLISIONS, createPolygonalShape(size.PPM), position.PPM, friction = 1.2f)

    val hero: Hero = new HeroImpl(EntityType.Hero, entityBody, size.PPM, stats)

    hero.setCollisionStrategy(DoNothingOnCollision())
    hero.setMovementStrategy(new HeroMovementStrategy(hero, stats(Statistic.MovementSpeed)))
    hero.setAttackStrategy(new HeroAttackStrategy(hero, stats(Statistic.Strength)))

    this.createHeroFeet(hero)

    this.entitiesContainer.addEntity(hero)
    hero
  }

  override def changeHeroFixture(hero: Hero, newSize: (Float, Float), addCoordinates: (Float, Float) = (0,0)): Unit = {
    hero.getEntityBody
      .setShape(createPolygonalShape(newSize.PPM))
      .createFixture()

    hero.getEntityBody.addCoordinates(0, -hero.getSize._2 + newSize._2.PPM)

    hero.setSize(newSize.PPM)

    this.createHeroFeet(hero)
  }

  private def createHeroFeet(hero: Hero): Unit = {
    if(hero.getFeet.nonEmpty) {
      this.destroyBody(hero.getFeet.get.getBody)
      this.removeEntity(hero.getFeet.get)
    }

    val feetSize: (Float, Float) = FEET_SIZE
    val bodyPosition = hero.getPosition - (0, hero.getSize._2)
    val feetBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityCollisionBit.Hero,
      HERO_FEET_COLLISIONS, createPolygonalShape(feetSize.PPM, rounder = true),
      bodyPosition, gravityScale = 0, friction = 1.2f)
    this.createJoint(hero.getBody, feetBody.getBody)

    val heroFeet: MobileEntity = new MobileEntityImpl(EntityType.Mobile, feetBody, feetSize.PPM)
    heroFeet.setCollisionStrategy(DoNothingOnCollision())

    hero.setFeet(heroFeet)
    this.entitiesContainer.addEntity(heroFeet)
  }

  override def createPlatform(position: (Float, Float),
                              size: (Float, Float)): Entity = {

    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Platform,
      PLATFORM_COLLISIONS, createPolygonalShape(size.PPM), position.PPM)

    val immobileEntity: ImmobileEntity = ImmobileEntity(EntityType.Platform, entityBody, size.PPM)
    immobileEntity.setCollisionStrategy(DoNothingOnCollision())
    this.entitiesContainer.addEntity(immobileEntity)

    createPlatformSensor(size, position, immobileEntity, sizeXOffset = -2f, sizeYOffset = -size._2, positionYOffset = size._2 + 1f, isTopSensor = true)
    createPlatformSensor(size, position, immobileEntity, sizeXOffset = -2f, sizeYOffset = -size._2, positionYOffset = - size._2 - 5f)
    createPlatformSensor(size, position, immobileEntity, sizeXOffset = -size._1, sizeYOffset = 1, positionXOffset = +size._1 + 2f, positionYOffset = -2)
    createPlatformSensor(size, position, immobileEntity, sizeXOffset = -size._1, sizeYOffset = 1, positionXOffset = -size._1 - 2f, positionYOffset = -2)

    immobileEntity
  }

  private def createPlatformSensor(size: (Float, Float), position: (Float, Float), mainPlatform: ImmobileEntity, sizeXOffset: Float = 0, sizeYOffset: Float = 0,
                                   positionXOffset: Float = 0, positionYOffset: Float = 0, isTopSensor: Boolean = false): Unit = {

    val realSize: (Float, Float) = size + (sizeXOffset, sizeYOffset)
    val realPosition: (Float, Float) = position + (positionXOffset, positionYOffset)

    val sensorBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.PlatformSensor,
      PLATFORM_SENSOR_COLLISIONS, createPolygonalShape(realSize.PPM),
      realPosition.PPM, isSensor = true)

    val sensorEntity: ImmobileEntity = ImmobileEntity(EntityType.PlatformSensor, sensorBody, realSize.PPM)

    sensorEntity.setCollisionStrategy(if(isTopSensor)
      new UpperPlatformCollisionStrategy(mainPlatform, this.collisionMonitor)
        else
      new LowerPlatformCollisionStrategy(mainPlatform, this.collisionMonitor))

    this.entitiesContainer.addEntity(sensorEntity)

  }

  override def createLadder(position: (Float, Float),
                            size: (Float, Float)): Entity = {
    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Ladder,
      LADDER_COLLISIONS, createPolygonalShape(size.PPM), position.PPM, isSensor = true)

    val immobileEntity: ImmobileEntity = ImmobileEntity(EntityType.Ladder, entityBody, size.PPM)
    immobileEntity.setCollisionStrategy(new LadderCollisionStrategy(this.collisionMonitor))
    this.entitiesContainer.addEntity(immobileEntity)

    immobileEntity
  }

  override def createSkeletonEnemy(position: (Float, Float)): EnemyImpl = {
    val enemy:EnemyImpl = createEnemyEntity(position, SKELETON_SIZE,
      SKELETON_STATS, STATS_MODIFIER, this.entitiesContainer.getLevelNumber, SKELETON_SCORE, EntityType.EnemySkeleton)

    val behaviours:EnemyBehaviour = new EnemyBehaviourImpl(enemy)
    behaviours.addBehaviour("",
      DoNothingOnCollision(),
      new PatrolAndStop(enemy, this.entitiesContainer.getEntity(e => e.isInstanceOf[Hero])),
      new SkeletonAttack(enemy, this.entitiesContainer.getEntity(e => e.isInstanceOf[Hero])))

    enemy.setBehaviour(behaviours)
    enemy
  }

  override def createSlimeEnemy(position: (Float, Float)): EnemyImpl = {
    val enemy:EnemyImpl = createEnemyEntity(position,
      SLIME_SIZE, SLIME_STATS, STATS_MODIFIER, this.entitiesContainer.getLevelNumber, SLIME_SCORE, EntityType.EnemySlime)

    val behaviours:EnemyBehaviour = new EnemyBehaviourImpl(enemy)
    behaviours.addBehaviour("",
      DoNothingOnCollision(),
      new PatrolAndStop(enemy, this.entitiesContainer.getEntity(e => e.isInstanceOf[Hero])),
      new SlimeAttack(enemy, this.entitiesContainer.getEntity(e => e.isInstanceOf[Hero])))
    enemy.setBehaviour(behaviours)
    enemy
  }

  override def createWormEnemy(position: (Float, Float)): EnemyImpl = {
    val enemy:EnemyImpl = createEnemyEntity(position, WORM_SIZE,
      WORM_STATS, STATS_MODIFIER, this.entitiesContainer.getLevelNumber, WORM_SCORE, EntityType.EnemyWorm)

    val behaviours:EnemyBehaviour = new EnemyBehaviourImpl(enemy)
    behaviours.addBehaviour("",
      DoNothingOnCollision(),
      new PatrolAndStop(enemy, this.entitiesContainer.getEntity(e => e.isInstanceOf[Hero])),
      new WormFireballAttack(enemy, this.entitiesContainer.getEntity(e => e.isInstanceOf[Hero])))
    enemy.setBehaviour(behaviours)
    enemy
  }

  override def createWizardBossEnemy(position: (Float, Float)): EnemyImpl = {
    val enemy:EnemyImpl = createEnemyEntity(position, WIZARD_BOSS_SIZE, WIZARD_BOSS_STATS, STATS_MODIFIER,
      this.entitiesContainer.getLevelNumber, WIZARD_BOSS_SCORE, EntityType.EnemyBossWizard)
    val targetEntity:Entity = this.entitiesContainer.getEntity(e => e.isInstanceOf[Hero])

    val behaviours:EnemyBehaviour = new EnemyBehaviourImpl(enemy)

    // first behaviour - do nothing for some time
    behaviours.addBehaviour("1", DoNothingOnCollision(), DoNothingMovementStrategy(), DoNothingAttackStrategy())

    // second behaviour - attack hero if near
    val p2AttackStrategy = new WizardFirstAttack(enemy, targetEntity)
    behaviours.addBehaviour("2", DoNothingOnCollision(), new ChaseTarget(enemy, targetEntity), p2AttackStrategy)

    // third behaviour - attack hero if near (with another attack)
    val p3AttackStrategy = new WizardSecondAttack(enemy, targetEntity)
    behaviours.addBehaviour("3", DoNothingOnCollision(), new ChaseTarget(enemy, targetEntity), p3AttackStrategy)

    // fourth behaviour - attack hero with ranged attacks
    val p4AttackStrategy = new WizardEnergyBallAttack(enemy, targetEntity)
    behaviours.addBehaviour("4", DoNothingOnCollision(), new FaceTarget(enemy, targetEntity), p4AttackStrategy)

    // add conditional transitions between behaviours
    behaviours.addTransition("1", "2", new TargetIsNearPredicate(enemy, targetEntity, 100f.PPM))
    behaviours.addTransition("1", "3", new TargetIsNearPredicate(enemy, targetEntity, 100f.PPM))

    behaviours.addTransition("2", "3", new RandomTruePredicate(0.5f))
    behaviours.addTransition("2", "4", new TargetIsFarPredicate(enemy, targetEntity, 100f.PPM))

    behaviours.addTransition("3", "2", new RandomTruePredicate(0.5f))
    behaviours.addTransition("3", "4", new TargetIsFarPredicate(enemy, targetEntity, 100f.PPM))

    behaviours.addTransition("4", "2", new TargetIsNearPredicate(enemy, targetEntity, 100f.PPM))
    behaviours.addTransition("4", "3", new TargetIsNearPredicate(enemy, targetEntity, 100f.PPM))

    enemy.setBehaviour(behaviours)
    enemy
  }

  override def createEnemyEntity(position: (Float, Float),
                                 size: (Float, Float),
                                 stats: Map[Statistic, Float],
                                 statsModifiers: Map[Statistic, Float],
                                 levelNumber: Int = 0,
                                 score: Int,
                                 entityId: EntityType): EnemyImpl = {

    val spawnPoint = (position._1, position._2+size._2)
    val levelBasedStats =
      stats.map {case (key, value) => (key, value + levelNumber * statsModifiers.getOrElse(key, 0f))}

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityCollisionBit.Enemy,
      ENEMY_COLLISIONS, createPolygonalShape(size.PPM, rounder = true), spawnPoint.PPM)

    val heroEntity: Hero = this.entitiesContainer.getHero.get

    val enemy:EnemyImpl = new EnemyImpl(entityId, entityBody, size.PPM, levelBasedStats, score, heroEntity)
    this.entitiesContainer.addEntity(enemy)
    enemy
  }

  override def createItem(PoolName: ItemPools,
                          size: (Float, Float) = (5f, 5f),
                          position: (Float, Float) = (0, 0),
                          collisions: Short = ITEM_COLLISIONS): Item = {
    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityCollisionBit.Item,
      collisions, createPolygonalShape(size.PPM), position.PPM)
    val item: Item = itemPool.getItem(entityBody, size, PoolName)
    item.setCollisionStrategy(new ItemCollisionStrategy(item, this.entitiesContainer))
    this.entitiesContainer.addEntity(item)
    item
  }

  override def createPolygonalShape(size: (Float, Float), rounder:Boolean = false): Shape = {
    val shape: PolygonShape = new PolygonShape()
    if (!rounder) shape.setAsBox(size._1, size._2)
    else {
      shape.set(Array[Vector2](new Vector2(size._1, size._2),
        new Vector2(size._1, -size._2+1f.PPM),
        new Vector2(size._1-1f.PPM, -size._2),
        new Vector2(-size._1+1f.PPM, -size._2),
        new Vector2(-size._1, -size._2+1f.PPM),
        new Vector2(-size._1, size._2)))
    }
    shape
  }

  override def createCircleShape(radius: Float): Shape = {
    val shape: CircleShape = new CircleShape()
    shape.setRadius(radius)
    shape
  }

  override def createImmobileEntity(entityType: EntityType = EntityType.Immobile,
                                    size: (Float, Float) = (10, 10),
                                    position: (Float, Float) = (0, 0),
                                    entityCollisionBit: Short = EntityCollisionBit.Immobile,
                                    collisions: Short = IMMOBILE_COLLISIONS): Entity = {

    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, entityCollisionBit,
      collisions, createPolygonalShape(size.PPM), position.PPM)

    val immobileEntity: Entity = ImmobileEntity(entityType, entityBody, size.PPM)
    immobileEntity.setCollisionStrategy(DoNothingOnCollision())
    this.entitiesContainer.addEntity(immobileEntity)
    immobileEntity
  }

  override def spawnEnemies(spawnZoneSize: (Float, Float) = (10, 10),
                            spawnZonePosition: (Float, Float) = (0, 0)): Unit =  {

    def spawnEnemy(position: (Float, Float)): EnemyImpl = {
      RANDOM.shuffle(ENEMY_TYPES).head match {
        case EntityType.EnemySkeleton => this.createSkeletonEnemy((position.x, position.y))
        case EntityType.EnemyWorm => this.createWormEnemy((position.x, position.y))
        case EntityType.EnemySlime => this.createSlimeEnemy((position.x, position.y))
      }
    }

    // randomly generate an enemy
    val enemy:LivingEntity = spawnEnemy(spawnZonePosition)
    // TODO: set movement direction inside a movement strategy
    enemy.setFacing(RANDOM.nextBoolean()) // set initial movement direction

    /*
    // compute number of enemies to spawn in the spawnZone
    val spawnCount: Int = Math.floor(spawnZoneSize._1 / ENEMIES_SPAWN_RATIO).toInt
    for (_ <- 0 until 1) {
      // randomy pick spawn position inside the spawn zone. Only the horizontal axis coordinate is generated randomly
      val spawnPosition: (Float, Float) = (RANDOM.between(spawnZonePosition._1 - spawnZoneSize._1 / 2,
        spawnZonePosition._1 + spawnZoneSize._1 / 2), spawnZonePosition._2)

      // randomly generate an enemy
      val enemy:LivingEntity = spawnEnemy(spawnPosition)
      // TODO: set movement direction inside a movement strategy
      enemy.setFacing(RANDOM.nextBoolean()) // set initial movement direction
    }
     */
  }

  override def spawnBoss(spawnZoneSize: (Float, Float) = (10, 10),
                         spawnZonePosition: (Float, Float) = (0, 0)): Unit =  {
    this.createWizardBossEnemy(spawnZonePosition)

//    def spawnEnemy(position: (Float, Float)): EnemyImpl = {
//      RANDOM.shuffle(ENEMY_TYPES).head match {
//        case EntityType.EnemyBossWizard => this.createWizardBossEnemy((position.x, position.y))
//        //        case EntityType.EnemyBossReaper => this.createReaperBossEnemy((position.x, position.y))
//      }
//    }
//    spawnEnemy(position)
  }

  private def createDoorWithSensors(size: (Float, Float),
                                    position: (Float, Float)): (ImmobileEntity, ImmobileEntity, ImmobileEntity) = {
    val sensorSize = (0f, size._2)
    val leftSensorPosition = (position._1 - size._1 - 10, position._2)

    val rightSensorPosition = (position._1 + size._1 + 10, position._2)

    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Door,
      DOOR_COLLISIONS, createPolygonalShape(size.PPM), position.PPM)

    val leftSensorBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Door,
      DOOR_COLLISIONS, createPolygonalShape(sensorSize.PPM), leftSensorPosition.PPM, isSensor = true)

    val rightSensorBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Door,
      DOOR_COLLISIONS, createPolygonalShape(sensorSize.PPM), rightSensorPosition.PPM, isSensor = true)

    val door: ImmobileEntity = ImmobileEntity(EntityType.Door, entityBody, size.PPM)

    val leftSensor: ImmobileEntity = ImmobileEntity(EntityType.Immobile, leftSensorBody, size.PPM)

    val rightSensor: ImmobileEntity = ImmobileEntity(EntityType.Immobile, rightSensorBody, size.PPM)

    (door, leftSensor, rightSensor)
  }

  override def createDoor(size: (Float, Float) = (10, 10),
                          position: (Float, Float) = (0, 0),
                          collisions: Short = 0): Entity = {

    val doors = createDoorWithSensors(size, position)

    doors._1.setCollisionStrategy(new DoorCollisionStrategy(this.entitiesContainer, doors._1, doors._2, doors._3))

    doors._2.setCollisionStrategy(new DoorCollisionStrategy(this.entitiesContainer, doors._1, doors._2, doors._3))

    doors._3.setCollisionStrategy(new DoorCollisionStrategy(this.entitiesContainer, doors._1, doors._2, doors._3))

    this.entitiesContainer.addEntity(doors._1)

    this.entitiesContainer.addEntity(doors._2)

    this.entitiesContainer.addEntity(doors._3)
    doors._1
  }

  override def createBossDoor(size: (Float, Float) = (10, 10),
                          position: (Float, Float) = (0, 0),
                          collisions: Short = 0): Entity = {

    val doors = createDoorWithSensors(size, position)

    doors._1.setCollisionStrategy(new BossDoorCollisionStrategy(this.entitiesContainer, doors._1, doors._2, doors._3))

    doors._2.setCollisionStrategy(new BossDoorCollisionStrategy(this.entitiesContainer, doors._1, doors._2, doors._3))

    doors._3.setCollisionStrategy(new BossDoorCollisionStrategy(this.entitiesContainer, doors._1, doors._2, doors._3))

    this.entitiesContainer.addEntity(doors._1)

    this.entitiesContainer.addEntity(doors._2)

    this.entitiesContainer.addEntity(doors._3)

    doors._1
  }

  override def createChest(size: (Float, Float), position: (Float, Float)): Entity = {
    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Door,
      CHEST_COLLISIONS, createPolygonalShape(size.PPM), position.PPM)

    val immobileEntity: ImmobileEntity = ImmobileEntity(EntityType.Chest, entityBody, size.PPM)
    immobileEntity.setCollisionStrategy(new ChestCollisionStrategy(this.entitiesContainer, immobileEntity))
    this.entitiesContainer.addEntity(immobileEntity)
    immobileEntity
  }

  override def createPortal(size: (Float, Float) = (10, 10),
                          position: (Float, Float) = (0, 0)): Entity = {

    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Portal,
      PORTAL_COLLISIONS, createPolygonalShape(size.PPM), position.PPM)

    val immobileEntity: ImmobileEntity = ImmobileEntity(EntityType.Portal, entityBody, size.PPM)
    //TODO mettere a posto
    immobileEntity.setCollisionStrategy(new PortalCollisionStrategy(immobileEntity, null))//this.level))
    immobileEntity.setState(State.Closed)
    this.entitiesContainer.addEntity(immobileEntity)
    immobileEntity
  }


  override def createWaterPool(position: (Float, Float),
                               size: (Float, Float)): Entity = {
    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Pool,
      WATER_LAVA_COLLISIONS, createPolygonalShape(size.PPM), position.PPM, isSensor = true)

    val immobileEntity: Entity = ImmobileEntity(EntityType.Water, entityBody, size.PPM)
    immobileEntity.setCollisionStrategy(new WaterCollisionStrategy)
    this.entitiesContainer.addEntity(immobileEntity)
    immobileEntity
  }

  override def createLavaPool(position: (Float, Float),
                               size: (Float, Float)): Entity = {
    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Pool,
      WATER_LAVA_COLLISIONS, createPolygonalShape(size.PPM), position.PPM, isSensor = true)

    val immobileEntity: Entity = ImmobileEntity(EntityType.Lava, entityBody, size.PPM)

    immobileEntity.setCollisionStrategy(new LavaCollisionStrategy(this.collisionMonitor))
    this.entitiesContainer.addEntity(immobileEntity)
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
      NO_COLLISIONS, createPolygonalShape(pivotSize.PPM), pivotPoint, isSensor = true)

    val rotatingBodyPosition = (pivotPoint._1 + rotatingBodyDistance._1.PPM, pivotPoint._2 + rotatingBodyDistance._2.PPM)
    val rotatingBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityCollisionBit.Sword,
      SWORD_COLLISIONS, createPolygonalShape(rotatingBodySize.PPM), rotatingBodyPosition,
      startingAngle, gravityScale = 0, 1, 0.3f, 0.5f, isSensor = true)

    val circularMobileEntity =
      new CircularMobileEntity(entityType, rotatingBody, rotatingBodySize.PPM, sourceEntity.getStatistics, pivotBody)
    circularMobileEntity.setMovementStrategy(
      new CircularMovementStrategy(circularMobileEntity, angularVelocity))
    circularMobileEntity.setCollisionStrategy(
      new ApplyDamage((e:Entity) => e.isInstanceOf[EnemyImpl], sourceEntity.getStatistics))

    this.entitiesContainer.addEntity(circularMobileEntity)
    circularMobileEntity
  }

  override def createAirAttackPattern(entityType: EntityType = EntityType.Mobile,
                                      bodySize: (Float, Float),
                                      bodyDistance: (Float, Float),
                                      sourceEntity: LivingEntity): MobileEntity = {

    val pivotPoint: (Float, Float) = sourceEntity.getPosition

    val bodyPosition = (pivotPoint._1 + bodyDistance._1.PPM, pivotPoint._2 + bodyDistance._2.PPM)

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityCollisionBit.Sword,
      SWORD_COLLISIONS, createPolygonalShape(bodySize.PPM),
      bodyPosition, gravityScale = 0, friction = 1.2f, isSensor = true)

    this.createJoint(sourceEntity.getBody, entityBody.getBody)

    val airSword: MobileEntity = new AirSwordMobileEntity(EntityType.Mobile, entityBody, bodySize.PPM)
    airSword.setCollisionStrategy(new ApplyDamage((e:Entity) => e.isInstanceOf[Enemy], sourceEntity.getStatistics))

    this.entitiesContainer.addEntity(airSword)
    airSword
  }

  override def createFireballAttack(sourceEntity: LivingEntity,
                                    targetEntity: Entity,
                                    size: (Float, Float) = (1f, 1f),
                                    offset: (Float, Float) = (0f, 0f)): MobileEntity = {

    // compute bullet spawn point
    val attackXOffset:Float = if (sourceEntity.isFacingRight) offset._1.PPM else -offset._1.PPM
    val attackYOffset:Float = offset._2.PPM
    val position = (sourceEntity.getBody.getWorldCenter.x + attackXOffset,
      sourceEntity.getBody.getWorldCenter.y + attackYOffset)

    // create a body inside the game world
    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityCollisionBit.EnemyAttack,
      FIREBALL_COLLISIONS, this.createCircleShape(size._1.PPM), position, isSensor = true)
    entityBody.getBody.setBullet(true)

    // create an entity representing the bullet
    val attack: MobileEntity = new MobileEntityImpl(EntityType.AttackFireBall, entityBody,
      size.PPM, sourceEntity.getStatistics)
    attack.setFacing(isEntityOnTheRight(sourceEntity, targetEntity))

    // set entity behaviours
    attack.setCollisionStrategy(new ApplyDamageAndDestroyEntity(attack, (e:Entity) => e.isInstanceOf[Hero],
      sourceEntity.getStatistics))
    attack.setMovementStrategy(new WeightlessProjectileTrajectory(attack, (position.x, position.y),
      (targetEntity.getBody.getWorldCenter.x, targetEntity.getBody.getWorldCenter.y), sourceEntity.getStatistics))

    this.entitiesContainer.addEntity(attack)
    attack
  }

  override def createEnergyBallAttack(sourceEntity: LivingEntity,
                                      targetEntity: Entity,
                                      size: (Float, Float) = (1f, 1f),
                                      offset: (Float, Float) = (0f, 0f)): MobileEntity = {
      // compute bullet spawn point
    val attackXOffset:Float = if (sourceEntity.isFacingRight) offset._1.PPM else -offset._1.PPM
    val attackYOffset:Float = offset._2.PPM
    val position = (sourceEntity.getBody.getWorldCenter.x+attackXOffset,
        sourceEntity.getBody.getWorldCenter.y+attackYOffset)

      // create a body inside the game world
    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityCollisionBit.EnemyAttack,
        ENERGY_BALL_COLLISIONS, this.createCircleShape(size._1.PPM), (position.x, position.y), isSensor = true)
    entityBody.getBody.setBullet(true)

      // create an entity representing the bullet
    val attack: MobileEntity = new MobileEntityImpl(EntityType.AttackEnergyBall, entityBody,
        size.PPM, sourceEntity.getStatistics)
    attack.setFacing(isEntityOnTheRight(sourceEntity, targetEntity))

      // set entity behaviours
    attack.setCollisionStrategy(new ApplyDamageAndDestroyEntity(attack, (e:Entity) => e.isInstanceOf[Hero],
        sourceEntity.getStatistics))
    attack.setMovementStrategy(new HomingProjectileTrajectory(attack, (position.x, position.y),
        (targetEntity.getBody.getWorldCenter.x, targetEntity.getBody.getWorldCenter.y), sourceEntity.getStatistics))

      this.entitiesContainer.addEntity(attack)
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
      ENEMY_MELEE_ATTACK_COLLISIONS, this.createPolygonalShape(size.PPM), position, isSensor = true)

    this.createJoint(pivotBody.getBody, entityBody.getBody)

    // create an entity representing the melee attack
    val attack: MobileEntity = new MobileEntityImpl(EntityType.Mobile, entityBody, size.PPM, sourceEntity.getStatistics)

    // set entity behaviours
    attack.setCollisionStrategy(new ApplyDamage(e => e.isInstanceOf[Hero], sourceEntity.getStatistics))

    this.entitiesContainer.addEntity(attack)
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
    val arrow: MobileEntity = this.createMobileEntity(EntityType.Arrow, size, newPosition, EntityCollisionBit.Arrow,
      EntityCollisionBit.Immobile | EntityCollisionBit.Enemy , gravityScale = 0)
    arrow.setFacing(entity.isFacingRight)
    arrow.setMovementStrategy(new ArrowMovementStrategy(arrow, entity.getStatistics(Statistic.MovementSpeed)))
    arrow.setCollisionStrategy(new ApplyDamageAndDestroyEntity(arrow, (e:Entity) => e.isInstanceOf[EnemyImpl] , entity.getStatistics))
    arrow
  }



  private def createJoint(pivotBody: Body, rotatingBody: Body): Unit = {
    val rjd: RevoluteJointDef = new RevoluteJointDef()

    rjd.initialize(pivotBody, rotatingBody, pivotBody.getWorldCenter)

    this.entitiesContainer.getWorld.get.createJoint(rjd)
  }

  override def removeEntity(entity: Entity): Unit = this.entitiesContainer.removeEntity(entity)

  override def createBody(bodyDef: BodyDef): Body = this.entitiesContainer.getWorld.get.createBody(bodyDef)

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

  override def addPendingFunction(function: () => Unit): Unit = synchronized {
    this.pendingFunctions = function :: this.pendingFunctions
  }

  override def applyPendingFunctions(): Unit = synchronized {
    for(function <- this.pendingFunctions) {
      function.apply()
    }
    this.pendingFunctions = List.empty
  }

  override def pendingJointCreation(pivotBody: Body, rotatingBody: Body): Unit =
    this.addPendingFunction(() => this.createJoint(pivotBody, rotatingBody))

  override def pendingChangeCollisions(entity: Entity, entityType: Short): Unit =
    this.addPendingFunction(() => entity.getEntityBody.setCollisions(entityType).createFixture())

  override def pendingDestroyBody(body: Body): Unit = this.addPendingFunction(() => this.destroyBody(body))

  private def destroyBody(body: Body): Unit = this.entitiesContainer.getWorld.get.destroyBody(body)
}


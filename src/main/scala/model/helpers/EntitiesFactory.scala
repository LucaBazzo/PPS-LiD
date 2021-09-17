package model.helpers

import _root_.utils.ApplicationConstants._
import _root_.utils.CollisionConstants._
import _root_.utils.EnemiesConstants._
import _root_.utils.EnvironmentConstants._
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d._
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef
import model._
import model.attack._
import model.behaviour.RichPredicates._
import model.behaviour.{EnemyBehaviours, EnemyBehavioursImpl, NotPredicate, RandomTruePredicate}
import model.collisions.ImplicitConversions._
import model.collisions.{EntityCollisionBit, _}
import model.entities.EntityType.EntityType
import model.entities.Statistic.Statistic
import model.entities.{Entity, _}
import model.helpers.EntitiesUtilities.{getEntitiesDistance, isEntityOnTheLeft, isEntityOnTheRight}
import model.movement._

trait EntitiesFactory {

  def setEntitiesContainerMonitor(entitiesContainerMonitor: EntitiesContainerMonitor): Unit

  def getEntitiesContainerMonitor: EntitiesContainerMonitor

  def getItemPool: ItemPool

  def setLevel(level: Level, pool: ItemPool): Unit

  def createEnemyEntity(position: (Float, Float),
                        size: (Float, Float),
                        stats: Map[Statistic, Float],
                        statsModifiers: Map[Statistic, Float],
                        score: Int,
                        entityId: EntityType): EnemyImpl

  def createSlimeEnemy(position: (Float, Float)): EnemyImpl

  def createSkeletonEnemy(position: (Float, Float)): EnemyImpl

  def createWormEnemy(position: (Float, Float)): EnemyImpl

  def createWizardBossEnemy(position: (Float, Float)): EnemyImpl

  def createPolygonalShape(size: (Float, Float), rounder:Boolean = false): Shape

  def createCircleShape(radius: Float): Shape

  def createImmobileEntity(entityType: EntityType = EntityType.Immobile,
                           size: (Float, Float) = (10, 10),
                           position: (Float, Float) = (0, 0),
                           entityCollisionBit: Short = EntityCollisionBit.Immobile,
                           collisions: Short = 0): Entity

  def createDoor(size: (Float, Float) = DEFAULT_DOOR_SIZE,
                 position: (Float, Float) = DEFAULT_DOOR_POSITION,
                 isBossDoor: Boolean = false,
                 collisions: Short = 0): Entity

  def createChest(size: (Float, Float) = DEFAULT_CHEST_SIZE,
                  position: (Float, Float) = DEFAULT_CHEST_POSITION): Entity

  def createPortal(size: (Float, Float) = DEFAULT_PORTAL_SIZE,
                   position: (Float, Float) = DEFAULT_PORTAL_POSITION): Entity

  def createPlatform(position: (Float, Float),
                     size: (Float, Float)): Entity

  def createLadder(position: (Float, Float),
                     size: (Float, Float)): Entity

  def createWaterPool(position: (Float, Float),
                      size: (Float, Float)): Entity

  def createLavaPool(position: (Float, Float),
                      size: (Float, Float)): Entity

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

  def pendingJointCreation(pivotBody: Body, rotatingBody: Body): Unit

  def removeEntity(entity: Entity): Unit

  def createBody(bodyDef: BodyDef): Body

  def pendingDestroyBody(body: Body): Unit

  def pendingChangeCollisions(entity: Entity, entityType: Short): Unit

  // TODO: convertire createEnemies in createSpawnZone e lasicare a levelImpl la generazione dei nemici nelle zone di spawn
  def spawnEnemy(size: (Float, Float) = (10, 10),
                 position: (Float, Float) = (0, 0)): Unit

  def spawnBoss(size: (Float, Float) = (10, 10),
                position: (Float, Float) = (0, 0)): Unit

  def addPendingFunction(function: () => Unit): Unit

  def applyPendingFunctions(): Unit

  def defineEntityBody(bodyType: BodyType,
                       entityType: Short,
                       collisions: Short,
                       shape: Shape,
                       position: (Float, Float),
                       angle: Float = 0,
                       gravityScale: Float = 1.0f,
                       density: Float = 0,
                       friction: Float = 0.2f,
                       restitution: Float = 0,
                       isSensor: Boolean = false): EntityBody

  def destroyBody(body: Body): Unit

  def createJoint(pivotBody: Body, rotatingBody: Body): Unit

  def addEntity(entity: Entity): Unit
}

object EntitiesFactoryImpl extends EntitiesFactory {
  private var level: Level = _
  private var itemPool: ItemPool = _
  private val collisionMonitor: CollisionMonitor = new CollisionMonitorImpl
  private var pendingFunctions: List[() => Unit] = List.empty
  private var entitiesContainer: EntitiesContainerMonitor = _

  override def setEntitiesContainerMonitor(entitiesContainerMonitor: EntitiesContainerMonitor): Unit =
    this.entitiesContainer = entitiesContainerMonitor

  override def getEntitiesContainerMonitor: EntitiesContainerMonitor =
    this.entitiesContainer

  override def getItemPool: ItemPool =
    this.itemPool

  override def setLevel(level: Level, pool: ItemPool): Unit = {
    this.level = level
    this.itemPool = pool
  }

  override def createPlatform(position: (Float, Float),
                              size: (Float, Float)): Entity = {

    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Platform,
      PLATFORM_COLLISIONS, createPolygonalShape(size.PPM), position.PPM)

    val immobileEntity: ImmobileEntity = ImmobileEntity(EntityType.Platform, entityBody, size.PPM)
    immobileEntity.setCollisionStrategy(DoNothingCollisionStrategy())
    this.entitiesContainer.addEntity(immobileEntity)

    createPlatformSensor(size, position, immobileEntity, sizeXOffset = PLATFORM_SENSOR_SIZE_X_OFFSET,
      sizeYOffset = -size._2, positionYOffset = size._2 + UPPER_PLATFORM_SENSOR_POSITION_Y_OFFSET, isTopSensor = true)
    createPlatformSensor(size, position, immobileEntity, sizeXOffset = PLATFORM_SENSOR_SIZE_X_OFFSET,
      sizeYOffset = -size._2, positionYOffset = - size._2 + LOWER_PLATFORM_SENSOR_POSITION_Y_OFFSET)
    createPlatformSensor(size, position, immobileEntity, sizeXOffset = -size._1,
      sizeYOffset = SIDE_PLATFORM_SENSOR_SIDE_Y_OFFSET, positionXOffset = +size._1 + SIDE_PLATFORM_SENSOR_POSITION_X_OFFSET, positionYOffset = SIDE_PLATFORM_SENSOR_POSITION_Y_OFFSET)
    createPlatformSensor(size, position, immobileEntity, sizeXOffset = -size._1,
      sizeYOffset = SIDE_PLATFORM_SENSOR_SIDE_Y_OFFSET, positionXOffset = -size._1 - SIDE_PLATFORM_SENSOR_POSITION_X_OFFSET, positionYOffset = SIDE_PLATFORM_SENSOR_POSITION_Y_OFFSET)

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

    val collisionStrategy: CollisionStrategy = if(isTopSensor)
      UpperPlatformCollisionStrategy(mainPlatform, this.collisionMonitor)
    else
      LowerPlatformCollisionStrategy(mainPlatform, this.collisionMonitor)

    sensorEntity.setCollisionStrategy(collisionStrategy)

    this.entitiesContainer.addEntity(sensorEntity)
  }

  override def createLadder(position: (Float, Float),
                            size: (Float, Float)): Entity = {
    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Ladder,
      LADDER_COLLISIONS, createPolygonalShape(size.PPM), position.PPM, isSensor = true)

    val immobileEntity: ImmobileEntity = ImmobileEntity(EntityType.Ladder, entityBody, size.PPM)
    immobileEntity.setCollisionStrategy(LadderCollisionStrategy(this.collisionMonitor))
    this.entitiesContainer.addEntity(immobileEntity)

    immobileEntity
  }

  override def createSkeletonEnemy(position: (Float, Float)): EnemyImpl = {
    val enemy:EnemyImpl = createEnemyEntity(position, SKELETON_SIZE,
      SKELETON_STATS, STATS_MODIFIER, SKELETON_SCORE, EntityType.EnemySkeleton)

    val behaviours:EnemyBehaviours = new EnemyBehavioursImpl()
    behaviours.addBehaviour((DoNothingCollisionStrategy(),
      EnemyMovementStrategy(enemy, this.entitiesContainer.getEntity(e => e.isInstanceOf[Hero])),
      new SkeletonAttack(enemy, this.entitiesContainer.getEntity(e => e.isInstanceOf[Hero]))))
    enemy.setBehaviour(behaviours)
    enemy
  }

  override def createSlimeEnemy(position: (Float, Float)): EnemyImpl = {
    // easter egg: a slime could rarely be displayed as Pacman with a 5% chance
    val enemyType =  if (RANDOM.nextInt(100) <= 5) EntityType.EnemyPacman else EntityType.EnemySlime

    val enemy:EnemyImpl = createEnemyEntity(position,
      SLIME_SIZE, SLIME_STATS, STATS_MODIFIER, SLIME_SCORE, enemyType)

    val behaviours:EnemyBehaviours = new EnemyBehavioursImpl()
    behaviours.addBehaviour((DoNothingCollisionStrategy(),
      EnemyMovementStrategy(enemy, this.entitiesContainer.getEntity(e => e.isInstanceOf[Hero])),
      new SlimeAttack(enemy, this.entitiesContainer.getEntity(e => e.isInstanceOf[Hero]))))
    enemy.setBehaviour(behaviours)
    enemy
  }

  override def createWormEnemy(position: (Float, Float)): EnemyImpl = {
    val enemy:EnemyImpl = createEnemyEntity(position, WORM_SIZE,
      WORM_STATS, STATS_MODIFIER, WORM_SCORE, EntityType.EnemyWorm)

    val behaviours:EnemyBehaviours = new EnemyBehavioursImpl()
    behaviours.addBehaviour((DoNothingCollisionStrategy(),
      EnemyMovementStrategy(enemy, this.entitiesContainer.getEntity(e => e.isInstanceOf[Hero])),
      new WormFireballAttack(enemy, this.entitiesContainer.getEntity(e => e.isInstanceOf[Hero]))))
    enemy.setBehaviour(behaviours)
    enemy
  }

  override def createWizardBossEnemy(position: (Float, Float)): EnemyImpl = {
    val enemy:EnemyImpl = createEnemyEntity(position, WIZARD_BOSS_SIZE, WIZARD_BOSS_STATS, STATS_MODIFIER,
      WIZARD_BOSS_SCORE, EntityType.EnemyBossWizard)
    val targetEntity:Entity = this.entitiesContainer.getEntity(e => e.isInstanceOf[Hero])

    val behaviours:EnemyBehaviours = new EnemyBehavioursImpl()

    // first behaviour - do nothing for some time
    val b1 = behaviours.addBehaviour((DoNothingCollisionStrategy(), DoNothingMovementStrategy(), DoNothingAttackStrategy()))

    // second behaviour - attack hero if near
    val p2AttackStrategy = new WizardFirstAttack(enemy, targetEntity)
    val b2 = behaviours.addBehaviour((DoNothingCollisionStrategy(), ChaseTarget(enemy, targetEntity), p2AttackStrategy))

    // third behaviour - attack hero if near (with another attack)
    val p3AttackStrategy = new WizardSecondAttack(enemy, targetEntity)
    val b3 = behaviours.addBehaviour((DoNothingCollisionStrategy(), ChaseTarget(enemy, targetEntity), p3AttackStrategy))

    // fourth behaviour - attack hero with ranged attacks
    val p4AttackStrategy = new WizardEnergyBallAttack(enemy, targetEntity)
    val b4 = behaviours.addBehaviour((DoNothingCollisionStrategy(), FaceTarget(enemy, targetEntity), p4AttackStrategy))

    // add conditional transitions between behaviours
    behaviours.addTransition(b1, b2, () => getEntitiesDistance(enemy, targetEntity) <= 100f.PPM)
    behaviours.addTransition(b1, b3, () => getEntitiesDistance(enemy, targetEntity) <= 100f.PPM)

    behaviours.addTransition(b2, b3, RandomTruePredicate(0.5f))
    behaviours.addTransition(b2, b4, NotPredicate(() => getEntitiesDistance(enemy, targetEntity) <= 100f.PPM))

    behaviours.addTransition(b3, b2, RandomTruePredicate(0.5f))
    behaviours.addTransition(b3, b4, NotPredicate(() => getEntitiesDistance(enemy, targetEntity) <= 100f.PPM))

    behaviours.addTransition(b4, b2, () => getEntitiesDistance(enemy, targetEntity) <= 100f.PPM)
    behaviours.addTransition(b4, b3, () => getEntitiesDistance(enemy, targetEntity) <= 100f.PPM)

    enemy.setBehaviour(behaviours)
    enemy
  }

  override def createEnemyEntity(position: (Float, Float),
                                 size: (Float, Float),
                                 stats: Map[Statistic, Float],
                                 statsModifiers: Map[Statistic, Float],
                                 score: Int,
                                 entityId: EntityType): EnemyImpl = {

    val spawnPoint = (position._1, position._2+size._2)
    val levelBasedStats =
      stats.map {case (key, value) => (key, value + this.entitiesContainer.getLevelNumber * statsModifiers.getOrElse(key, 0f))}

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityCollisionBit.Enemy,
      ENEMY_COLLISIONS, createPolygonalShape(size.PPM, rounder = true), spawnPoint.PPM)

    val heroEntity: Hero = this.entitiesContainer.getHero.get

    val enemy:EnemyImpl = new EnemyImpl(entityId, entityBody, size.PPM, levelBasedStats, score, heroEntity)
    this.entitiesContainer.addEntity(enemy)
    enemy
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
    immobileEntity.setCollisionStrategy(DoNothingCollisionStrategy())//new NewLevelOnCollision(this.level)
    this.entitiesContainer.addEntity(immobileEntity)
    immobileEntity
  }

  override def spawnEnemy(size: (Float, Float) = (10, 10),
                          position: (Float, Float) = (0, 0)): Unit =  {
    RANDOM.shuffle(ENEMY_TYPES).head match {
      case EntityType.EnemySkeleton => this.createSkeletonEnemy((position.x, position.y))
      case EntityType.EnemyWorm => this.createWormEnemy((position.x, position.y))
      case EntityType.EnemySlime => this.createSlimeEnemy((position.x, position.y))
    }
  }

  override def spawnBoss(spawnZoneSize: (Float, Float) = (10, 10),
                         spawnZonePosition: (Float, Float) = (0, 0)): Unit =  {
    this.createWizardBossEnemy(spawnZonePosition)
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

  override def createDoor(size: (Float, Float),
                          position: (Float, Float),
                          isBossDoor: Boolean,
                          collisions: Short = 0): Entity = {

    val doors = createDoorWithSensors(size, position)

    val collisionStrategy: CollisionStrategy = if(isBossDoor)
      BossDoorCollisionStrategy(this.entitiesContainer, doors._1, doors._2, doors._3)
    else
      DoorCollisionStrategy(this.entitiesContainer, doors._1, doors._2, doors._3)

    doors._1.setCollisionStrategy(collisionStrategy)

    doors._2.setCollisionStrategy(collisionStrategy)

    doors._3.setCollisionStrategy(collisionStrategy)

    this.entitiesContainer.addEntity(doors._1)

    this.entitiesContainer.addEntity(doors._2)

    this.entitiesContainer.addEntity(doors._3)
    doors._1
  }

  override def createChest(size: (Float, Float), position: (Float, Float)): Entity = {
    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Door,
      CHEST_COLLISIONS, createPolygonalShape(size.PPM), position.PPM)

    val immobileEntity: ImmobileEntity = ImmobileEntity(EntityType.Chest, entityBody, size.PPM)
    immobileEntity.setCollisionStrategy(ChestCollisionStrategy(this.entitiesContainer, immobileEntity))
    this.entitiesContainer.addEntity(immobileEntity)
    immobileEntity
  }

  override def createPortal(size: (Float, Float) = (10, 10),
                          position: (Float, Float) = (0, 0)): Entity = {

    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Portal,
      PORTAL_COLLISIONS, createPolygonalShape(size.PPM), position.PPM)

    val immobileEntity: ImmobileEntity = ImmobileEntity(EntityType.Portal, entityBody, size.PPM)
    immobileEntity.setCollisionStrategy(PortalCollisionStrategy(immobileEntity, this.level))
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

    immobileEntity.setCollisionStrategy(LavaCollisionStrategy(this.collisionMonitor))
    this.entitiesContainer.addEntity(immobileEntity)
    immobileEntity
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
    attack.setCollisionStrategy(ApplyDamageAndDestroyEntity(attack, (e:Entity) => e.isInstanceOf[Hero],
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
    attack.setCollisionStrategy(ApplyDamageAndDestroyEntity(attack, (e:Entity) => e.isInstanceOf[Hero],
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
    attack.setCollisionStrategy(ApplyDamage(e => e.isInstanceOf[Hero], sourceEntity.getStatistics))

    this.entitiesContainer.addEntity(attack)
    attack
  }

  override def createJoint(pivotBody: Body, rotatingBody: Body): Unit = {
    val rjd: RevoluteJointDef = new RevoluteJointDef()
    rjd.initialize(pivotBody, rotatingBody, pivotBody.getWorldCenter)
    this.entitiesContainer.getWorld.get.createJoint(rjd)
  }

  override def removeEntity(entity: Entity): Unit = this.entitiesContainer.removeEntity(entity)

  override def createBody(bodyDef: BodyDef): Body = this.entitiesContainer.getWorld.get.createBody(bodyDef)

  override def defineEntityBody(bodyType: BodyType,
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

  override def destroyBody(body: Body): Unit = this.entitiesContainer.getWorld.get.destroyBody(body)

  override def addEntity(entity: Entity): Unit = this.entitiesContainer.addEntity(entity)
}
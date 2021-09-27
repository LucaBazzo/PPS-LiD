package model.helpers

import _root_.utils.ApplicationConstants._
import _root_.utils.CollisionConstants._
import _root_.utils.EnemiesConstants._
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d._
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef
import model._
import ImplicitConversions._
import model.collisions.{EntityCollisionBit, _}
import model.entities.EntityType.EntityType
import model.entities.Statistic.Statistic
import model.entities.{Entity, _}

trait EntitiesFactory {

  def setEntitiesContainerMonitor(entitiesContainerMonitor: EntitiesContainerMonitor): Unit

  def getEntitiesContainerMonitor: EntitiesContainerMonitor

  def getCollisionMonitor: CollisionMonitor

  def getLevel: Level

  def getItemPool: ItemPool

  def setLevel(level: Level, pool: ItemPool): Unit

  def createPolygonalShape(size: (Float, Float), rounder: Boolean = false): Shape

  def createCircleShape(radius: Float): Shape

  def createImmobileEntity(entityType: EntityType = EntityType.Immobile,
                           size: (Float, Float) = (10, 10),
                           position: (Float, Float) = (0, 0),
                           entityCollisionBit: Short = EntityCollisionBit.Immobile,
                           collisions: Short = 0): Entity

  def pendingJointCreation(pivotBody: Body, rotatingBody: Body): Unit

  def removeEntity(entity: Entity): Unit

  def createBody(bodyDef: BodyDef): Body

  def pendingDestroyBody(body: Body): Unit

  def pendingChangeCollisions(entity: Entity, entityType: Short): Unit

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

  def createEnemyEntity(position: (Float, Float),
                        size: (Float, Float),
                        stats: Map[Statistic, Float],
                        statsModifiers: Map[Statistic, Float],
                        score: Int,
                        entityId: EntityType,
                        collisions: Short): EnemyImpl
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

  override def getLevel: Level =
    this.level

  override def getCollisionMonitor: CollisionMonitor =
    this.collisionMonitor

  override def getItemPool: ItemPool =
    this.itemPool

  override def setLevel(level: Level, pool: ItemPool): Unit = {
    this.level = level
    this.itemPool = pool
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
    immobileEntity.setCollisionStrategy(DoNothingCollisionStrategy())
    this.entitiesContainer.addEntity(immobileEntity)
    immobileEntity
  }

  override def createEnemyEntity(position: (Float, Float),
                                 size: (Float, Float),
                                 stats: Map[Statistic, Float],
                                 statsModifiers: Map[Statistic, Float],
                                 score: Int,
                                 entityId: EntityType,
                                 collisions: Short = ENEMY_COLLISIONS): EnemyImpl = {
    val spawnPoint = (position._1, position._2 + size._2)
    val levelBasedStats =
      stats.map { case (key, value) => (key, value + getEntitiesContainerMonitor.getLevelNumber * statsModifiers.getOrElse(key, 0f)) }

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityCollisionBit.Enemy,
      collisions, createPolygonalShape(size.PPM, rounder = true), spawnPoint.PPM)

    val enemy: EnemyImpl = new EnemyImpl(entityId, entityBody, size.PPM, levelBasedStats, score,
      getEntitiesContainerMonitor.getHero.getOrElse(throw new IllegalArgumentException()))
    addEntity(enemy)
    enemy
  }

  override def spawnEnemy(size: (Float, Float) = (10, 10),
                          position: (Float, Float) = (0, 0)): Unit =  {
    RANDOM.shuffle(ENEMY_TYPES).head match {
      case EntityType.EnemySkeleton => SkeletonEnemy(position)
      case EntityType.EnemyWorm => WormEnemy(position)
      case EntityType.EnemySlime => SlimeEnemy(position)
      case EntityType.EnemyBat => BatEnemy(position)
    }
  }

  override def spawnBoss(spawnZoneSize: (Float, Float) = (10, 10),
                         spawnZonePosition: (Float, Float) = (0, 0)): Unit =  {
    WizardEnemy(spawnZonePosition)
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
    this.pendingFunctions.foreach(f => f.apply())
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
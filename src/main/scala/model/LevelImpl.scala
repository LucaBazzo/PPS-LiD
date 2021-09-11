package model

import _root_.utils.ApplicationConstants._
import _root_.utils.HeroConstants
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.physics.box2d._
import controller.GameEvent.GameEvent
import model.collisions.ImplicitConversions._
import model.collisions.{CollisionManager, EntityCollisionBit}
import model.entities.ItemPools.ItemPools
import model.entities._
import model.helpers._

trait Level {

  def updateEntities(actions: List[GameEvent]): Unit

  def addEntity(entity: Entity): Unit

  def removeEntity(entity: Entity): Unit

  def getEntity(predicate: Entity => Boolean): Entity

  def getEntities(predicate: Entity => Boolean): List[Entity]

  def spawnItem(pool: ItemPools.ItemPools): Unit

  def getWorld: World

  def newLevel(): Unit

  def dispose(): Unit
}

class LevelImpl(private val model: Model, private val entitiesSetter: EntitiesSetter) extends Level {

  private val world: World = new World(GRAVITY_FORCE, true)
  WorldUtilities.setWorld(world)

  private val entitiesFactory: EntitiesFactory = EntitiesFactoryImpl
  entitiesFactory.setLevel(this, new ItemPoolImpl())

  private var entitiesList: List[Entity] = List.empty

  private val hero: Hero = entitiesFactory.createHeroEntity(this.entitiesSetter.asInstanceOf[EntitiesGetter].getHeroStatistics)

  private val item: Item = entitiesFactory.createItem(ItemPools.Level_1, (10f, 10f), (140,50), EntityCollisionBit.Hero, entitiesSetter)

  private val door: Entity = entitiesFactory.createDoor((10, 30), (390, 200))

  private var platform: Entity = entitiesFactory.createPlatform((380, 200), (60,2))
  private var ladder: Entity = entitiesFactory.createLadder((280,200),(10,100))

  EntitiesFactoryImpl.createSkeletonEnemy((HeroConstants.HERO_OFFSET._1+70, HeroConstants.HERO_OFFSET._2))

  private var water: Entity = entitiesFactory.createWaterPool((200,290), (100,15))

  private var lava: Entity = entitiesFactory.createLavaPool((400,290), (100,15))

  this.entitiesSetter.setEntities(entitiesList)
  this.entitiesSetter.setWorld(Option.apply(this.world))

  this.world.setContactListener(new CollisionManager(this.entitiesSetter.asInstanceOf[EntitiesGetter]))

  override def updateEntities(actions: List[GameEvent]): Unit = {

    for(command <- actions) this.hero.notifyCommand(command)

    this.worldStep()

    EntitiesFactoryImpl.createPendingEntities()

    this.entitiesList.foreach((entity: Entity) => entity.update())

    this.entitiesFactory.destroyBodies()

    this.entitiesFactory.applyEntityCollisionChanges()
  }

  override def addEntity(entity: Entity): Unit = {
    this.entitiesList = entity :: this.entitiesList
    this.entitiesSetter.setEntities(this.entitiesList)
  }

  override def getEntity(predicate: Entity => Boolean): Entity = entitiesList.filter(predicate).head
  override def getEntities(predicate: Entity => Boolean): List[Entity] = entitiesList.filter(predicate)

  override def removeEntity(entity: Entity): Unit = {
    this.entitiesList = this.entitiesList.filterNot((e: Entity) => e.equals(entity))
    this.entitiesSetter.setEntities(this.entitiesList)

    // update score if the removed entity's type is Enemy or Item
    if (entity.isInstanceOf[Enemy] || entity.isInstanceOf[Item]) {
      this.entitiesSetter.addScore(entity.asInstanceOf[Score].getScore)
    }

    if (entity.isInstanceOf[Enemy]) {
      // TODO: nomralizzare la creazione e rimozione di tutte le entity prima del word.step in update
      // TODO: rifattorizzare .MPP (enemy position e in PPM mentre la createItem vuole valori non scalati)
      EntitiesFactoryImpl.createItem(ItemPools.Enemy_Drops,
        position=(entity.getPosition._1, entity.getPosition._2).MPP,
        collisions = EntityCollisionBit.Hero, entitesSetter=this.entitiesSetter)
    }
  }

  override def getWorld: World = this.world

  override def spawnItem(pool: ItemPools): Unit = {
    entitiesFactory.createItem(pool, entitiesSetter = this.entitiesSetter)
  }

  private var accumulator: Float = 0f

  private def worldStep() {
    val delta: Float = Gdx.graphics.getDeltaTime

    accumulator += Math.min(delta, 0.25f)

    while (accumulator >= TIME_STEP) {
      accumulator -= TIME_STEP

      world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS)
    }
  }

  override def newLevel(): Unit = {
    this.entitiesSetter.setHeroStatistics(this.hero.getStatistics)
    println("New level", this.hero.getStatistics)
    this.model.requestNewLevel()
  }

  override def dispose(): Unit = {
    this.entitiesSetter.setWorld(Option.empty)
    this.world.dispose()
  }
}

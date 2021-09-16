package model

import _root_.utils.ApplicationConstants._
import _root_.utils.EnemiesConstants._
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.physics.box2d._
import controller.GameEvent.GameEvent
import model.collisions.CollisionManager
import model.collisions.ImplicitConversions._
import model.entities._
import model.helpers._

import java.util.concurrent.{ExecutorService, Executors}

trait Level {

  def updateEntities(actions: List[GameEvent]): Unit

  def addEntity(entity: Entity): Unit

  def removeEntity(entity: Entity): Unit

  def getEntity(predicate: Entity => Boolean): Entity

  def getEntities(predicate: Entity => Boolean): List[Entity]

  def getWorld: World

  def newLevel(): Unit

  def dispose(): Unit
}

class LevelImpl(private val model: Model, private val entitiesSetter: EntitiesSetter, private val itemPool: ItemPool) extends Level {

  private val world: World = new World(GRAVITY_FORCE, true)
  WorldUtilities.setWorld(world)

  private val entitiesFactory: EntitiesFactory = EntitiesFactoryImpl
  entitiesFactory.setLevel(this, itemPool)
  entitiesFactory.setEntitiesSetter(this.entitiesSetter)

  private var entitiesList: List[Entity] = List.empty

  private val hero: Hero = entitiesFactory.createHeroEntity(this.entitiesSetter.asInstanceOf[EntitiesGetter].getHeroStatistics)

  private var isWorldSetted: Boolean = false

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

    if (ENEMY_BOSS_TYPES.contains(entity.getType)) {
      val portal = this.getEntity(x => x.getType == EntityType.Portal)
      portal.setState(State.Opening)
      val executorService: ExecutorService = Executors.newSingleThreadExecutor()
      executorService.execute(() => {
        Thread.sleep(1900)
        portal.setState(State.Standing)
        println("Portal opened")
      })
      //executorService.shutdown()
    }
  }

  override def getWorld: World = this.world

  private var accumulator: Float = 0f

  private def worldStep(): Unit = {
    val delta: Float = Gdx.graphics.getDeltaTime

    accumulator += Math.min(delta, 0.25f)

    while (accumulator >= TIME_STEP) {
      accumulator -= TIME_STEP

      world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS)
    }
  }

  override def newLevel(): Unit = {
    this.entitiesSetter.setHeroStatistics(this.hero.getStatistics)
    this.itemPool.resetBossPool()
    this.hero.loseItem(Items.Key)
    this.model.requestLevel()
  }

  override def dispose(): Unit = {
    this.entitiesSetter.setWorld(Option.empty)
    this.world.dispose()
  }
}

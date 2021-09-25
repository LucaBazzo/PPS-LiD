package model

import _root_.utils.ApplicationConstants._
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.physics.box2d._
import controller.GameEvent.GameEvent
import model.collisions.CollisionManager
import model.collisions.ImplicitConversions._
import model.entities._
import model.helpers._

trait Level {

  def updateEntities(actions: List[GameEvent]): Unit

  def newLevel(): Unit
}

class LevelImpl(private val model: Model,
                private val entitiesContainer: EntitiesContainerMonitor,
                private val itemPool: ItemPool) extends Level {

  private val world: World = new World(GRAVITY_FORCE, true)
  WorldUtilities.setWorld(world)

  private val entitiesFactory: EntitiesFactory = EntitiesFactoryImpl
  entitiesFactory.setLevel(this, itemPool)
  entitiesFactory.addPendingFunction( () => Item(ItemPools.Keys, EntitiesFactoryImpl.getItemPool,
    EntitiesFactoryImpl.getEntitiesContainerMonitor, (10,10), (2258.0f,85.0f)))
  this.entitiesContainer.setEntities(List.empty)
  this.entitiesContainer.setWorld(Option.apply(this.world))
  this.world.setContactListener(new CollisionManager(this.entitiesContainer))

  private val hero: Hero = Hero(this.entitiesContainer.getHeroStatistics)

  override def updateEntities(actions: List[GameEvent]): Unit = {

    for(command <- actions) this.hero.notifyCommand(command)

    this.worldStep()

    EntitiesFactoryImpl.applyPendingFunctions()

    this.entitiesContainer.getEntities.foreach((entity: Entity) => entity.update())
  }

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
    this.entitiesContainer.setHeroStatistics(this.hero.getStatistics)
    this.itemPool.resetBossPool()
    this.hero.loseItem(Items.Key)
    this.model.requestLevel()
  }
}

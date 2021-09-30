package model

import _root_.utils.ApplicationConstants._
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.physics.box2d._
import controller.GameEvent.GameEvent
import controller.ModelResources
import model.entity.collision.CollisionManager
import model.helpers.ImplicitConversions._
import model.entity._
import model.helpers._

/**
 *
 */
trait Level {

  def updateEntities(actions: List[GameEvent]): Unit

  def newLevel(): Unit
}

class LevelImpl(private val model: Model,
                private val modelResources: ModelResources,
                private val itemPool: ItemPool) extends Level {

  private val world: World = new World(GRAVITY_FORCE, true)
  WorldUtilities.setWorld(world)

  private val entitiesFactory: EntitiesFactory = EntitiesFactoryImpl
  entitiesFactory.setLevel(this, itemPool)

  this.modelResources.setEntities(List.empty)
  this.modelResources.setWorld(Option.apply(this.world))
  this.world.setContactListener(new CollisionManager(this.modelResources))

  private val hero: Hero = Hero(this.modelResources.getHeroStatistics, this.modelResources.getItemsPicked)

  override def updateEntities(actions: List[GameEvent]): Unit = {

    for(command <- actions) this.hero.notifyCommand(command)

    this.worldStep()

    EntitiesFactoryImpl.applyPendingFunctions()
    this.modelResources.getAllEntities.foreach((entity: Entity) => entity.update())
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
    this.modelResources.setHeroStatistics(this.hero.getStatistics)
    //this.itemPool.resetBossPool()
    this.hero.loseItem(Items.Key)
    this.model.requestLevel()
  }
}

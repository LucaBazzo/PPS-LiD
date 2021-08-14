package model

import com.badlogic.gdx.physics.box2d.World
import controller.GameEvent.GameEvent
import model.entities.{Entity, HeroImpl}
import utils.ApplicationConstants.{GRAVITY_FORCE, POSITION_ITERATIONS, TIME_STEP, VELOCITY_ITERATIONS}
import view.screens.helpers.WorldCreator

trait Level {

  def updateEntities(actions: List[GameEvent])
  def getEntity(predicate: Entity => Boolean): Entity
}

class LevelImpl(private val entitiesSetter: EntitiesSetter) extends Level {

  private val world: World = new World(GRAVITY_FORCE, true)
  new WorldCreator(this.world)

  private val entitiesFactory: EntitiesFactory = new EntitiesFactoryImpl(world)

  private val hero: HeroImpl = entitiesFactory.createHeroEntity()

  private val entitiesList = List[Entity](hero)

  this.entitiesSetter.setEntities(entitiesList)
  this.entitiesSetter.setWorld(this.world)

  this.world.setContactListener(new CollisionManager(this))

  override def updateEntities(actions: List[GameEvent]): Unit = {

    if(actions.nonEmpty) {
      for(command <- actions) this.hero.setCommand(command)
    }

    this.hero.update()

    this.world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS)
  }

  override def getEntity(predicate: Entity => Boolean): Entity = entitiesList.filter(predicate).head
}

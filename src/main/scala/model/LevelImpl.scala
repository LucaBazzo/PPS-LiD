package model

import _root_.utils.ApplicationConstants.{GRAVITY_FORCE, POSITION_ITERATIONS, TIME_STEP, VELOCITY_ITERATIONS}
import com.badlogic.gdx.physics.box2d._
import controller.GameEvent.{Attack, GameEvent}
import model.collisions.CollisionManager
import model.entities.{Entity, Hero}
import model.helpers.{EntitiesFactory, EntitiesFactoryImpl, EntitiesSetter}
import model.world.WorldCreator

trait Level {

  def updateEntities(actions: List[GameEvent])
  def addEntity(entity: Entity)
  def getEntity(predicate: Entity => Boolean): Entity
}

class LevelImpl(private val entitiesSetter: EntitiesSetter) extends Level {

  private val world: World = new World(GRAVITY_FORCE, true)

  private val entitiesFactory: EntitiesFactory = EntitiesFactoryImpl
  entitiesFactory.setWorld(world)

  private val hero: Hero = entitiesFactory.createHeroEntity()


  private var entitiesList: List[Entity] = List(hero)

  new WorldCreator(this, this.world)

  this.entitiesSetter.setEntities(entitiesList)
  this.entitiesSetter.setWorld(this.world)

  this.world.setContactListener(new CollisionManager(this))

  override def updateEntities(actions: List[GameEvent]): Unit = {

    if(actions.nonEmpty) {
      for(command <- actions) this.hero.notifyCommand(command)
    }

    if(actions.nonEmpty) {
      for(command <- actions) if(command == Attack) {
        entitiesFactory.createAttackPattern((0.1f, 1f), this.hero.getPosition, (0, 2), -60)
      }
    }

    this.entitiesList.foreach((entity: Entity) => entity.update())

    this.world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS)
  }

  override def addEntity(entity: Entity): Unit = {
    this.entitiesList = entity :: this.entitiesList
    this.entitiesSetter.setEntities(entitiesList)
  }

  override def getEntity(predicate: Entity => Boolean): Entity = entitiesList.filter(predicate).head
}

package model

import com.badlogic.gdx.physics.box2d.World
import controller.GameEvent.GameEvent
import model.collisions.CollisionManager
import model.entities.{EnemyImpl, Entity, HeroImpl, MobileEntityImpl}
import model.helpers.{EntitiesFactory, EntitiesFactoryImpl, EntitiesSetter}
import model.world.WorldCreator
import utils.ApplicationConstants.{GRAVITY_FORCE, POSITION_ITERATIONS, TIME_STEP, VELOCITY_ITERATIONS}

trait Level {

  def updateEntities(actions: List[GameEvent])
  def addEntity(entity: Entity)
  def getEntity(predicate: Entity => Boolean): Entity
}

class LevelImpl(private val entitiesSetter: EntitiesSetter) extends Level {

  private val world: World = new World(GRAVITY_FORCE, true)

  private val entitiesFactory: EntitiesFactory = new EntitiesFactoryImpl(world, this)

  private var entitiesList: List[Entity] = List.empty

  private val hero: HeroImpl = entitiesFactory.createHeroEntity()
  entitiesList = hero :: entitiesList

  entitiesList = entitiesFactory.createEnemyEntity() :: entitiesList

  new WorldCreator(this, this.world)

  this.entitiesSetter.setEntities(entitiesList)
  this.entitiesSetter.setWorld(this.world)
  this.world.setContactListener(new CollisionManager(this))

  override def updateEntities(actions: List[GameEvent]): Unit = {
//    println(this.world.getContactList)
    if(actions.nonEmpty) {
      for(command <- actions) this.hero.setCommand(command)
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

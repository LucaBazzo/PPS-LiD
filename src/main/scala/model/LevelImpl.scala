package model

import _root_.utils.ApplicationConstants.{GRAVITY_FORCE, POSITION_ITERATIONS, TIME_STEP, VELOCITY_ITERATIONS}
import com.badlogic.gdx.physics.box2d._
import controller.GameEvent
import controller.GameEvent.GameEvent
import model.collisions.ImplicitConversions._
import model.collisions.{CollisionManager, EntityCollisionBit}
import model.entities.ItemPools.ItemPools
import model.entities._
import model.helpers.{EntitiesFactory, EntitiesFactoryImpl, EntitiesSetter, ItemPoolImpl}

trait Level {

  def updateEntities(actions: List[GameEvent]): Unit

  def addEntity(entity: Entity): Unit

  def removeEntity(entity: Entity): Unit

  def getEntity(predicate: Entity => Boolean): Entity

  def spawnItem(pool: ItemPools.ItemPools): Unit

  def getWorld: World
}

class LevelImpl(private val entitiesSetter: EntitiesSetter) extends Level {

  private var score: Int = 0

  private val world: World = new World(GRAVITY_FORCE, true)

  private val entitiesFactory: EntitiesFactory = EntitiesFactoryImpl
  entitiesFactory.setLevel(this, new ItemPoolImpl())

  private var entitiesList: List[Entity] = List.empty

  private val hero: Hero = entitiesFactory.createHeroEntity()
  private val item: Item = entitiesFactory.createItem(ItemPools.Level_1, (10f, 10f), (40,50), EntityCollisionBit.Hero)

  private val door: Entity = entitiesFactory.createDoor((5, 30), (-20f, 10f))

  private var isWorldSetted: Boolean = false

  EntitiesFactoryImpl.createSkeletonEnemy((+350, 400))
  EntitiesFactoryImpl.createWormEnemy((+370,400))
//  EntitiesFactoryImpl.createSlimeEnemy((270,300))

  this.entitiesSetter.setEntities(entitiesList)
  this.entitiesSetter.setWorld(this.world)
  this.entitiesSetter.setScore(0)

  this.world.setContactListener(new CollisionManager(this))

  override def updateEntities(actions: List[GameEvent]): Unit = {
    if(actions.nonEmpty) {
      for(command <- actions){
        if(command.equals(GameEvent.SetMap)) {
          this.isWorldSetted = true

        } else this.hero.notifyCommand(command)
      }
    }

    if(this.isWorldSetted){
      this.entitiesList.foreach((entity: Entity) => entity.update())

      this.world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS)

      this.entitiesFactory.destroyBodies()

      this.entitiesFactory.applyEntityCollisionChanges()
    }

  }

  override def addEntity(entity: Entity): Unit = {
    this.entitiesList = entity :: this.entitiesList
    this.entitiesSetter.setEntities(this.entitiesList)
  }

  override def getEntity(predicate: Entity => Boolean): Entity = entitiesList.filter(predicate).head

  override def removeEntity(entity: Entity): Unit = {
    this.entitiesList = this.entitiesList.filterNot((e: Entity) => e.equals(entity))
    this.entitiesSetter.setEntities(this.entitiesList)

    // update score if the removed entity's type is Enemy
    if (entity.isInstanceOf[Enemy]) {
      this.score += entity.asInstanceOf[Score].getScore
      this.entitiesSetter.setScore(this.score)
    }
  }

  override def getWorld: World = this.world

  override def spawnItem(pool: ItemPools): Unit = {
    entitiesFactory.createItem(pool)
  }
}

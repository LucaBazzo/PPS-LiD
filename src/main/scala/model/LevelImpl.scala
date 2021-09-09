package model

import _root_.utils.ApplicationConstants._
import _root_.utils.HeroConstants
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.physics.box2d._
import controller.GameEvent
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

}

class LevelImpl(private val entitiesSetter: EntitiesSetter) extends Level {

  private var score: Int = 0

  private val world: World = new World(GRAVITY_FORCE, true)
  WorldUtilities.setWorld(world)

  private val entitiesFactory: EntitiesFactory = EntitiesFactoryImpl
  entitiesFactory.setLevel(this, new ItemPoolImpl())
  entitiesFactory.setEntitiesSetter(entitiesSetter)

  private var entitiesList: List[Entity] = List.empty

  private val hero: Hero = entitiesFactory.createHeroEntity()
  private val item: Item = entitiesFactory.createItem(ItemPools.Level_1, (10f, 10f), (140,50), EntityCollisionBit.Hero)

  private var isWorldSetted: Boolean = false

  // TODO: to be removed
//  EntitiesFactoryImpl.createWizardBossEnemy((HeroConstants.HERO_OFFSET._1+110, HeroConstants.HERO_OFFSET._2))

  this.entitiesSetter.setEntities(entitiesList)
  this.entitiesSetter.setWorld(this.world)
  this.entitiesSetter.setScore(0)

  this.world.setContactListener(new CollisionManager(this.entitiesSetter.asInstanceOf[EntitiesGetter]))

  override def updateEntities(actions: List[GameEvent]): Unit = {
    if(actions.nonEmpty) {
      for(command <- actions){
        if(command.equals(GameEvent.SetMap)) {
          this.isWorldSetted = true
        } //else this.hero.notifyCommand(command)
      }
    }

    if(this.isWorldSetted){
      if(actions.nonEmpty) {
        for(command <- actions){
          if(!command.equals(GameEvent.SetMap)) {
            this.hero.notifyCommand(command)
          }
        }
      }

      this.worldStep()

      EntitiesFactoryImpl.createPendingEntities()

      this.entitiesList.foreach((entity: Entity) => entity.update())

      this.entitiesFactory.destroyBodies()

      this.entitiesFactory.applyEntityCollisionChanges()
    }
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
    if (entity.isInstanceOf[EnemyImpl] || entity.isInstanceOf[Item]) {
      this.score += entity.asInstanceOf[Score].getScore
      this.entitiesSetter.setScore(this.score)
    }
  }

  override def getWorld: World = this.world

  override def spawnItem(pool: ItemPools): Unit = {
    entitiesFactory.createItem(pool)
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

}

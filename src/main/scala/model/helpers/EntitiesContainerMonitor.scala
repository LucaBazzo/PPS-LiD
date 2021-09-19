package model.helpers

import com.badlogic.gdx.physics.box2d.World
import model.Score
import model.entities.Items.Items
import model.entities.Statistic.Statistic
import model.entities.{Enemy, Entity, EntityType, Hero, Item, LivingEntity, State}
import utils.EnemiesConstants.ENEMY_BOSS_TYPES
import utils.HeroConstants.HERO_STATISTICS_DEFAULT

import java.util.concurrent.{ExecutorService, Executors}

trait EntitiesGetter {

  def getEntities: List[Entity]
  def getEntities(predicate: Entity => Boolean): Option[List[Entity]]
  def getHero: Option[Hero]
  def getBoss: Option[LivingEntity]
  def getWorld: Option[World]
  def getScore: Int
  def getMessage: Option[String]
  def hasHeroPickedUpItem: Option[Items]
  def getHeroStatistics: Map[Statistic, Float]
  def getLevelNumber: Int
  def isLevelReady: Boolean

  def getEntity(predicate: Entity => Boolean): Entity
}

trait EntitiesSetter {

  def setEntities(entities: List[Entity]): Unit
  def setWorld(world: Option[World]): Unit
  def resetScore(): Unit
  def addScore(score: Int): Unit
  def addMessage(mess: String): Unit
  def heroJustPickedUpItem(item: Items): Unit
  def setHeroStatistics(statistics: Map[Statistic, Float]): Unit
  def setLevelNumber(number: Int): Unit
  def setLevelReady(ready: Boolean): Unit

  def addEntity(entity: Entity): Unit
  def removeEntity(entity: Entity): Unit

}

class EntitiesContainerMonitor extends EntitiesGetter with EntitiesSetter {

  private var world: Option[World] = Option.empty
  private var messages: List[String] = List.empty
  private var entities: List[Entity] = List.empty
  private var heroPickedUpAnItem: Option[Items] = Option.empty
  private var levelNumber = 0
  private var score: Int = 0

  private var heroStatistics: Map[Statistic, Float] = HERO_STATISTICS_DEFAULT

  private var levelReady: Boolean = false

  override def getEntities: List[Entity] = this.entities

  // TODO option di list ha poco senso , una lista vuota è un option.empty
  override def getEntities(predicate: Entity => Boolean): Option[List[Entity]] = synchronized {
    Option.apply(this.entities.filter(predicate))
  }

  override def getWorld: Option[World] = synchronized {
    this.world
  }

  override def getScore: Int = synchronized {
    this.score
  }

  override def setEntities(entities: List[Entity]): Unit = synchronized {
    this.entities = entities
  }

  override def setWorld(world: Option[World]): Unit = synchronized {
    this.world = world
  }

  override def resetScore(): Unit = synchronized {
    this.score = 0
  }

  override def addScore(score: Int): Unit = synchronized {
    this.score += score
  }

  override def getMessage: Option[String] = {
    var res: Option[String] = Option.empty
    if (this.messages.nonEmpty) {
      res = Option.apply(this.messages.head)
      this.messages = List.empty
    }
    res
  }

  override def addMessage(mess: String): Unit = this.messages = mess :: this.messages

  override def getHero: Option[Hero] = {
    if(this.entities.exists(e => e.isInstanceOf[Hero]))
      return Option.apply(this.entities.filter(e => e.isInstanceOf[Hero]).head.asInstanceOf[Hero])
    Option.empty
  }

  override def getBoss: Option[LivingEntity] = {
    if(this.entities.exists(e => ENEMY_BOSS_TYPES.contains(e.getType)))
      return Option.apply(this.entities.filter(e => ENEMY_BOSS_TYPES.contains(e.getType)).head.asInstanceOf[LivingEntity])
    Option.empty
  }

  override def getHeroStatistics: Map[Statistic, Float] = this.heroStatistics

  override def setHeroStatistics(statistics: Map[Statistic, Float]): Unit = this.heroStatistics = statistics

  override def setLevelNumber(levelNumber: Int): Unit = this.levelNumber = levelNumber

  override def getLevelNumber: Int = this.levelNumber

  override def hasHeroPickedUpItem: Option[Items] = {
    val res = this.heroPickedUpAnItem
    this.heroPickedUpAnItem = Option.empty
    res
  }

  override def heroJustPickedUpItem(item: Items): Unit = this.heroPickedUpAnItem = Option.apply(item)

  override def isLevelReady: Boolean = this.levelReady

  override def setLevelReady(ready: Boolean): Unit = this.levelReady = ready

  override def addEntity(entity: Entity): Unit = this.entities = entity :: this.entities

  override def removeEntity(entity: Entity): Unit = {
    this.entities = this.entities.filterNot((e: Entity) => e.equals(entity))

    // update score if the removed entity's type is Enemy or Item
    if (entity.isInstanceOf[Enemy] || entity.isInstanceOf[Item]) {
      this.addScore(entity.asInstanceOf[Score].getScore)
    }

    if (ENEMY_BOSS_TYPES.contains(entity.getType)) {
      val portal: Entity = this.getEntity(x => x.getType == EntityType.Portal)
      portal.setState(State.Opening)
      val executorService: ExecutorService = Executors.newSingleThreadExecutor()
      executorService.execute(() => {
        Thread.sleep(1000)
        portal.setState(State.Standing)
        println("Portal opened")
      })
      executorService.shutdown()
    }
  }

  override def getEntity(predicate: Entity => Boolean): Entity = this.entities.filter(predicate).head
}

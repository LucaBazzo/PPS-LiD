package model.helpers

import com.badlogic.gdx.physics.box2d.World
import model.entities.Statistic.Statistic
import model.entities.{Entity, Hero}

trait EntitiesGetter {

  def getEntities(predicate: Entity => Boolean): Option[List[Entity]]
  def getHero: Option[Hero]
  def getWorld: Option[World]
  def getScore: Int
  def getMessage: Option[String]
  def getHeroStatistics: Option[Map[Statistic, Float]]
  def getLevelNumber: Int
}

trait EntitiesSetter {

  def setEntities(entities: List[Entity]): Unit
  def setWorld(world: Option[World]): Unit
  def resetScore(): Unit
  def addScore(score: Int): Unit
  def addMessage(mess: String): Unit
  def setHeroStatistics(statistics: Map[Statistic, Float])
  def setLevelNumber(number: Int): Unit
}

class EntitiesContainerMonitor extends EntitiesGetter with EntitiesSetter {

  private var world: Option[World] = Option.empty
  private var messages: List[String] = List.empty
  private var entities: List[Entity] = List.empty

  private var levelNumber = 0
  private var score: Int = 0

  private var heroStatistics: Option[Map[Statistic, Float]] = Option.empty

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

  override def getHeroStatistics: Option[Map[Statistic, Float]] = this.heroStatistics

  override def setHeroStatistics(statistics: Map[Statistic, Float]): Unit = this.heroStatistics = Option.apply(statistics)

  override def setLevelNumber(levelNumber: Int): Unit = this.levelNumber = levelNumber

  override def getLevelNumber: Int = this.levelNumber
}

package model.helpers

import com.badlogic.gdx.physics.box2d.World
import model.entities.Entity

trait EntitiesGetter {

  def getEntities(predicate: Entity => Boolean): Option[List[Entity]]
  def getWorld: World
  def getScore: Int
  def getMessage: Option[String]
}

trait EntitiesSetter {

  def setEntities(entities: List[Entity]): Unit
  def setWorld(world: World): Unit
  def setScore(score: Int): Unit
  def addMessage(mess: String): Unit
}

class EntitiesContainerMonitor extends EntitiesGetter with EntitiesSetter {

  private var world: World = _
  private var messages: List[String] = List.empty
  private var entities: List[Entity] = List.empty
  private var score: Int = 0

  override def getEntities(predicate: Entity => Boolean): Option[List[Entity]] = synchronized {
    Option.apply(this.entities.filter(predicate))
  }

  override def getWorld: World = synchronized {
    this.world
  }

  override def getScore: Int = synchronized {
    this.score
  }

  override def setEntities(entities: List[Entity]): Unit = synchronized {
    this.entities = entities
  }

  override def setWorld(world: World): Unit = synchronized {
    this.world = world
  }

  override def setScore(score: Int): Unit = synchronized {
    this.score = score
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
}

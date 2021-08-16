package model.helpers

import com.badlogic.gdx.physics.box2d.World
import model.entities.Entity

trait EntitiesGetter {

  def getEntities(predicate: Entity => Boolean): Option[List[Entity]]
  def getWorld: World
  def getScore: Int
}

trait EntitiesSetter {

  def setEntities(entities: List[Entity])
  def setWorld(world: World)
  def setScore(score: Int)
}

class EntitiesContainerMonitor extends EntitiesGetter with EntitiesSetter {

  private var world: World = _

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
}

package model

import com.badlogic.gdx.physics.box2d.World
import controller.GameEvent.GameEvent
import model.helpers.EntitiesSetter

trait Model {

  def update(actions: List[GameEvent])
  def getCurrentLevelNumber: Int
}

class ModelImpl(private val entitiesSetter: EntitiesSetter, private val level: Level) extends Model {

  private var levelNumber: Int = 1

  override def update(actions: List[GameEvent]): Unit = {
//    println("MODEL update - " + actions.toString())

    this.level.updateEntities(actions)
  }

  override def getCurrentLevelNumber: Int = this.levelNumber
}

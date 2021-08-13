package model

import controller.GameEvent.GameEvent

trait Model {

  def update(actions: List[GameEvent])
  def getCurrentLevelNumber: Int
}

class ModelImpl(private val entitiesSetter: EntitiesSetter) extends Model {

  private var levelNumber: Int = 1

  private var level: Level = new LevelImpl(entitiesSetter)

  override def update(actions: List[GameEvent]): Unit = {
    //println("MODEL update - " + actions.toString())

    this.level.updateEntities(actions)
  }

  override def getCurrentLevelNumber: Int = this.levelNumber
}

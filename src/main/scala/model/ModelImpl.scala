package model

import controller.GameEvent.GameEvent
import model.entities.{Hero, LivingEntity}
import model.helpers.EntitiesSetter

trait Model {

  def update(actions: List[GameEvent])

  def getCurrentLevelNumber: Int

  def isGameOver: Boolean
}

class ModelImpl(private val entitiesSetter: EntitiesSetter) extends Model {

  private var levelNumber: Int = 1

  private var level: Level = new LevelImpl(entitiesSetter)

  override def update(actions: List[GameEvent]): Unit = {
    this.level.updateEntities(actions)
  }

  override def isGameOver: Boolean = this.level.getEntity(e => e.isInstanceOf[Hero]).asInstanceOf[LivingEntity].getLife == 0

  override def getCurrentLevelNumber: Int = this.levelNumber
}

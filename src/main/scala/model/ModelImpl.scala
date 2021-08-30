package model

import com.badlogic.gdx.physics.box2d.World
import controller.GameEvent.GameEvent
import model.entities.{Hero, LivingEntity}
import model.helpers.EntitiesSetter

trait Model {

  def update(actions: List[GameEvent])

  def getCurrentLevelNumber: Int

  def isGameOver: Boolean
}

class ModelImpl(private val entitiesSetter: EntitiesSetter, private val level: Level) extends Model {

  private var levelNumber: Int = 1

  override def update(actions: List[GameEvent]): Unit = {
    this.level.updateEntities(actions)
  }

  override def isGameOver: Boolean = this.level.getEntity(e => e.isInstanceOf[Hero]).asInstanceOf[LivingEntity].getLife == 0

  override def getCurrentLevelNumber: Int = this.levelNumber
}

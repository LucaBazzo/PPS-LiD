package model

import com.badlogic.gdx.Gdx
import controller.GameEvent
import controller.GameEvent.GameEvent
import model.entities.{Hero, LivingEntity}
import model.helpers.{EntitiesFactoryImpl, EntitiesSetter}
import view.screens.helpers.TileMapHelper

trait Model {

  def update(actions: List[GameEvent])

  def getCurrentLevelNumber: Int

  def isGameOver: Boolean
}

class ModelImpl(private val entitiesSetter: EntitiesSetter,
                private  val rooms: Array[(String, (Integer, Integer))]) extends Model {

  private val level: Level = new LevelImpl(entitiesSetter)
  EntitiesFactoryImpl.setModel(this)
  private var levelNumber: Int = 100

  override def update(actions: List[GameEvent]): Unit = {
    for (action <- actions) {
      if(action.equals(GameEvent.SetMap)) {
        Gdx.app.postRunnable(
          () => TileMapHelper.setWorld(this.rooms)
        )
      }
    }

    this.level.updateEntities(actions)
  }

  override def isGameOver: Boolean = this.level.getEntity(e => e.isInstanceOf[Hero]).asInstanceOf[LivingEntity].getLife == 0

  override def getCurrentLevelNumber: Int = this.levelNumber
}

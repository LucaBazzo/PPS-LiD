package model

import com.badlogic.gdx.Gdx
import controller.GameEvent
import controller.GameEvent.GameEvent
import model.collisions.EntityCollisionBit
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

  private var levelNumber: Int = 1

  override def update(actions: List[GameEvent]): Unit = {
    for (action <- actions) {
      if(action.equals(GameEvent.SetMap)) {

//        EntitiesFactoryImpl.createImmobileEntity(
//          size=(200, 10),
//          position=(0, 10000),
//          collisions = (EntityCollisionBit.Hero | EntityCollisionBit.Enemy).toShort
//        )


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

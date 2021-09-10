package model

import com.badlogic.gdx.Gdx
import controller.GameEvent
import controller.GameEvent.GameEvent
import model.helpers.{EntitiesGetter, EntitiesSetter}
import view.screens.helpers.TileMapHelper

trait Model {

  def update(actions: List[GameEvent])

  def getCurrentLevelNumber: Int

  def isGameOver: Boolean

  def requestNewLevel(): Unit
}

class ModelImpl(private val entitiesSetter: EntitiesSetter,
                private  val rooms: Array[String]) extends Model {

  private var level: Level = new LevelImpl(this, entitiesSetter)

  private var levelNumber: Int = 1
  private var isLevelActive: Boolean = false
  private var requestedNewLevel: Boolean = false

  this.entitiesSetter.setLevelNumber(this.levelNumber)

  override def update(actions: List[GameEvent]): Unit = {
    if(this.requestedNewLevel)
      this.newLevel()

    if(actions.exists(g => g equals GameEvent.SetMap))
      this.setWorld()

    //TODO scegliere un altro metodo invece della filter
    if(this.isLevelActive)
      this.level.updateEntities(actions.filterNot(g => g equals GameEvent.SetMap))
  }

  override def isGameOver: Boolean = {
    if(this.entitiesSetter.asInstanceOf[EntitiesGetter].getHero.nonEmpty)
      return this.entitiesSetter.asInstanceOf[EntitiesGetter].getHero.get.isDead
    false
  }


  override def getCurrentLevelNumber: Int = this.levelNumber

  override def requestNewLevel(): Unit = this.requestedNewLevel = true

  private def newLevel(): Unit = {
    this.isLevelActive = false
    this.requestedNewLevel = false

    this.entitiesSetter.setEntities(List.empty)
    this.level.dispose()

    this.levelNumber += 1
    this.entitiesSetter.setLevelNumber(this.levelNumber)
    this.level = new LevelImpl(this, entitiesSetter)

    this.setWorld()
  }

  private def setWorld(): Unit = {
    Gdx.app.postRunnable(() => TileMapHelper.setWorld(this.level, this.rooms))
    this.isLevelActive = true
  }
}

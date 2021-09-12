package model

import com.badlogic.gdx.Gdx
import controller.GameEvent
import controller.GameEvent.GameEvent
import model.helpers.{EntitiesFactoryImpl, EntitiesGetter, EntitiesSetter, ItemPool, ItemPoolImpl}
import utils.HeroConstants.HERO_STATISTICS_DEFAULT
import view.screens.helpers.TileMapHelper

trait Model {

  def update(actions: List[GameEvent]): Unit

  def getCurrentLevelNumber: Int

  def isGameOver: Boolean

  def requestStartGame(): Unit

  def requestNewLevel(): Unit
}

class ModelImpl(private val entitiesSetter: EntitiesSetter,
                private val tileMapHelper: TileMapHelper) extends Model {

  EntitiesFactoryImpl.setModel(this)

  private var level: Option[Level] = Option.empty
  private val itemPool: ItemPool = new ItemPoolImpl()
  private var levelNumber: Int = 0
  private var isLevelActive: Boolean = false
  private var requestedNewLevel: Boolean = false

  override def update(actions: List[GameEvent]): Unit = {
    if(this.requestedNewLevel)
      this.newLevel()

    if(level.nonEmpty) {
      if(actions.exists(g => g equals GameEvent.SetMap))
        this.setWorld()

      //TODO scegliere un altro metodo invece della filter
      if(this.isLevelActive)
        this.level.get.updateEntities(actions.filterNot(g => g equals GameEvent.SetMap))
    }
  }

  override def isGameOver: Boolean = {
    if(this.entitiesSetter.asInstanceOf[EntitiesGetter].getHero.nonEmpty)
      return this.entitiesSetter.asInstanceOf[EntitiesGetter].getHero.get.isDead
    false
  }


  override def getCurrentLevelNumber: Int = this.levelNumber

  override def requestStartGame(): Unit = {
    this.levelNumber = 0
    this.entitiesSetter.resetScore()
    this.entitiesSetter.setHeroStatistics(HERO_STATISTICS_DEFAULT)
    this.requestNewLevel()
  }

  override def requestNewLevel(): Unit = this.requestedNewLevel = true

  private def newLevel(): Unit = {
    this.isLevelActive = false
    this.requestedNewLevel = false

    this.entitiesSetter.setEntities(List.empty)

    if(level.nonEmpty)
      this.level.get.dispose()

    this.levelNumber += 1
    this.entitiesSetter.setLevelNumber(this.levelNumber)
    this.level = Option.apply(new LevelImpl(this, entitiesSetter, this.itemPool))

    if(this.levelNumber > 1)
      this.setWorld()
  }

  private def setWorld(): Unit = {
    Gdx.app.postRunnable(() => {
      tileMapHelper.loadTiledMaps()
      tileMapHelper.setWorld()
    })
    this.isLevelActive = true
  }
}

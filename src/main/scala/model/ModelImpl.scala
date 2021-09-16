package model

import controller.GameEvent.GameEvent
import controller.{GameEvent, Observer, SoundManager}
import model.helpers._
import utils.HeroConstants.HERO_STATISTICS_DEFAULT
import view.screens.helpers.TileMapManager

trait Model {

  def update(actions: List[GameEvent]): Unit

  def getCurrentLevelNumber: Int

  def isGameOver: Boolean

  def requestStartGame(): Unit

  def requestNewLevel(): Unit

  def loadWorld(): Unit

  def requestLevel(): Unit

  def disposeLevel(): Unit
}

class ModelImpl(private val controller: Observer,
                private val entitiesContainer: EntitiesContainerMonitor,
                private val tileMapHelper: TileMapManager) extends Model {

  private val soundManager: SoundManager = new SoundManager

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
        this.loadWorld()

      //TODO scegliere un altro metodo invece della filter
      if(this.isLevelActive)
        this.level.get.updateEntities(actions.filterNot(g => g equals GameEvent.SetMap))
    }
  }

  override def isGameOver: Boolean = {
    if(this.entitiesContainer.getHero.nonEmpty)
      return this.entitiesContainer.getHero.get.isDead
    false
  }

  override def getCurrentLevelNumber: Int = this.levelNumber

  override def requestStartGame(): Unit = {
    this.levelNumber = 0
    this.entitiesContainer.resetScore()
    this.entitiesContainer.setHeroStatistics(HERO_STATISTICS_DEFAULT)
    this.requestNewLevel()
  }

  override def requestNewLevel(): Unit = this.requestedNewLevel = true

  private def newLevel(): Unit = {
    this.isLevelActive = false
    this.requestedNewLevel = false

    this.entitiesContainer.setEntities(List.empty)

    this.disposeLevel()

    this.levelNumber += 1
    this.entitiesContainer.setLevelNumber(this.levelNumber)
    this.level = Option.apply(new LevelImpl(this, entitiesContainer, this.itemPool))

    if(this.levelNumber > 1)
      this.loadWorld()
  }

  override def loadWorld(): Unit = {
    tileMapHelper.setWorld()
    this.isLevelActive = true
    this.entitiesContainer.setLevelReady(true)

    soundManager.startMusic()
  }

  override def requestLevel(): Unit = {
    this.controller.handleEvent(GameEvent.StartGame)
  }

  override def disposeLevel(): Unit = {
    if(level.nonEmpty) {
      this.entitiesContainer.setLevelReady(false)
      this.entitiesContainer.setWorld(Option.empty)
      this.level = Option.empty
    }
  }
}

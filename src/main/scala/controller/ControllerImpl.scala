package controller

import _root_.utils.ApplicationConstants.{GAME_LOOP_STEP, RANDOM_SEED}
import com.badlogic.gdx.Gdx
import controller.GameEvent.GameEvent
import model._
import model.helpers.EntitiesFactoryImpl
import model.world.TileMapManager
import view._

import java.util.concurrent.{ExecutorService, Executors, ScheduledExecutorService, TimeUnit}

/** Handles almost every aspect that allows the game to run such as starting and
 * stopping the game loop, initializing view and model, saving existing game run
 * and load it, handling inputs and closing the application.
 */
trait Controller {
  /** Called the hero is dead and the application should set the Game Over Screen.
   */
  def gameOver(): Unit
}

/** This class represent the Controller of the all game.
 */
class ControllerImpl extends Controller with Observer {

  private val entitiesContainer: ModelResources = new ModelResources()
  private val observerManager: ObserverManager = new ObserverManagerImpl()
  this.observerManager.addObserver(this)

  private val tileMapManager: TileMapManager = new TileMapManager

  private val view: View = new ViewImpl(entitiesContainer, observerManager, tileMapManager)
  private val model: Model = new ModelImpl(this, entitiesContainer, tileMapManager)

  private var executorService: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
  private var gameLoop: GameLoop = new GameLoopImpl(model, this)

  EntitiesFactoryImpl.setEntitiesContainerMonitor(entitiesContainer)

  override def handleEvent(event: GameEvent): Unit = event match {
    case GameEvent.StartGame => this.startGame()
    case GameEvent.CloseApplication => this.terminateApplication()
    case GameEvent.ReturnToMenu => this.view.returnToMenu()
    case GameEvent.MapLoaded => this.newLevel()
    case _ => this.gameLoop.addAction(event)
  }

  override def gameOver(): Unit = {
    val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    val task: Runnable = () => {
      Thread.sleep(1500)
      this.stopGameLoop()
      this.view.endGame()
      Thread.sleep(300)
      this.model.disposeLevel()
      this.entitiesContainer.setLevelNumber(0)
    }
    executorService.submit(task)
  }

  private def stopGameLoop(): Unit = this.executorService.shutdownNow()

  private def startGame(): Unit = {
    this.view.startGame()
    Gdx.app.postRunnable(() => {
      tileMapManager.updateTiledMapList(RANDOM_SEED)
      this.handleEvent(GameEvent.MapLoaded)
    })
  }

  private def newLevel(): Unit = {
    if(this.entitiesContainer.getLevelNumber == 0) {
      this.gameLoop = new GameLoopImpl(model, this)
      this.executorService = Executors.newSingleThreadScheduledExecutor()
      this.executorService.scheduleAtFixedRate(gameLoop, 0, GAME_LOOP_STEP, TimeUnit.NANOSECONDS)
      this.model.requestStartGame()
      this.handleEvent(GameEvent.SetMap)
    }
    else {
      this.model.requestNewLevel()
    }
  }

  private def terminateApplication(): Unit = {
    // terminate game loop executor
    this.executorService.shutdownNow()

    // let view terminate itself
    this.view.terminate()
  }
}

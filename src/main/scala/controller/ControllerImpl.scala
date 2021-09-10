package controller

import _root_.utils.ApplicationConstants.{GAME_LOOP_STEP, ROOM_MAP_NAMES}
import controller.GameEvent.GameEvent
import model._
import model.helpers.EntitiesContainerMonitor
import view._

import java.util.concurrent.{ExecutorService, Executors, ScheduledExecutorService, TimeUnit}
import scala.util.Random

/** Handles almost every aspect that allows the game to run such as starting and
 * stopping the game loop, initializing view and model, saving existing game run
 * and load it, handling inputs and closing the application.
 */
trait Controller {
  def stopExecutorService()
  def gameOver()
}

/** This class represent the Controller of the all game.
 */
class ControllerImpl extends Controller with Observer {

  private val entitiesContainer: EntitiesContainerMonitor = new EntitiesContainerMonitor()
  private val observerManager: ObserverManager = new ObserverManagerImpl()
  this.observerManager.addObserver(this)

  private var rooms: Array[String] = Array("hero-room", "room1-final")
  for(n <- 1 to 2) rooms = rooms :+ ROOM_MAP_NAMES(Random.between(0,ROOM_MAP_NAMES.size))
//  rooms = rooms :+ "boss-room"

  private val view: View = new ViewImpl(entitiesContainer, observerManager, rooms)
  private val model: Model = new ModelImpl(entitiesContainer, rooms)

  private val executorService: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
  private val gameLoop: GameLoop = new GameLoopImpl(model, this)

  this.executorService.scheduleAtFixedRate(gameLoop, 0, GAME_LOOP_STEP, TimeUnit.NANOSECONDS)

  override def handleEvent(event: GameEvent): Unit = event match {
    case GameEvent.StartGame => this.view.startGame()
    case GameEvent.CloseApplication => this.terminateApplication()
    case GameEvent.ReturnToMenu => this.view.returnToMenu()
    case _ => this.gameLoop.addAction(event)
  }

  override def gameOver(): Unit = {
    val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    val task: Runnable = () => {
      Thread.sleep(2000)
      this.stopExecutorService()
      this.view.endGame()
    }
    executorService.submit(task)
  }

  override def stopExecutorService(): Unit = this.executorService.shutdownNow()

  private def terminateApplication(): Unit = {
    // terminate game loop executor
    this.executorService.shutdownNow()

    // let view terminate itself
    this.view.terminate()
  }
}

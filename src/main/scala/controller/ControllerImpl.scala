package controller

import controller.GameEvent.GameEvent
import model._
import model.helpers.EntitiesContainerMonitor
import _root_.utils.ApplicationConstants.{GAME_LOOP_STEP, ROOM_MAP_NAMES}
import view._

import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
import scala.util.Random

/** Handles almost every aspect that allows the game to run such as starting and
 * stopping the game loop, initializing view and model, saving existing game run
 * and load it, handling inputs and closing the application.
 */
trait Controller {

  def stopExecutorService()
  def gameOver()
  def newLevel()
}


/** This class represent the Controller of the all game.
 */
class ControllerImpl extends Controller with Observer {

  private val entitiesContainer: EntitiesContainerMonitor = new EntitiesContainerMonitor()
  private val observerManager: ObserverManager = new ObserverManagerImpl()
  this.observerManager.addObserver(this)

//  private var rooms: Array[String] = Array("hero-room", "room2")
  private var rooms: Array[String] = Array()
  rooms = rooms :+ "hero-room"
  for(n <- 1 to 3) rooms = rooms :+ ROOM_MAP_NAMES(Random.between(0,ROOM_MAP_NAMES.size))
//  rooms = rooms :+ "boss-room"

  private val view: View = new ViewImpl(entitiesContainer, observerManager, rooms)
  private val model: Model = new ModelImpl(entitiesContainer, rooms)

  private val executorService: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
  private val gameLoop: GameLoop = new GameLoopImpl(model, this)

  this.executorService.scheduleAtFixedRate(gameLoop, 0, GAME_LOOP_STEP, TimeUnit.NANOSECONDS)

  //TODO only when the main menu is ready
  //this.view.startGame()

  override def handleEvent(event: GameEvent): Unit = event match {
    case GameEvent.CloseApplication => this.terminateApplication()
    case _ => this.gameLoop.addAction(event)
  }

  override def gameOver(): Unit = {
    this.terminateApplication()
  }

  override def newLevel(): Unit = ???

  override def stopExecutorService(): Unit = this.executorService.shutdown()

  private def terminateApplication(): Unit = {
    // terminate game loop executor
    this.executorService.shutdown()

    // let view terminate itself
    this.view.terminate()
  }
}

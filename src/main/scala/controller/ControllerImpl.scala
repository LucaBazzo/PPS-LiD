package controller

import controller.GameEvent.GameEvent
import model._
import utils.ApplicationConstants.GAME_LOOP_STEP
import view._

import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

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

  private val view: View = new ViewImpl(entitiesContainer, observerManager)
  private val model: Model = new ModelImpl(entitiesContainer)

  private val executorService: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
  private val gameLoop: GameLoop = new GameLoopImpl(model)

  this.executorService.scheduleAtFixedRate(gameLoop, 0, GAME_LOOP_STEP, TimeUnit.NANOSECONDS)

  //TODO only when the main menu is ready
  //this.view.startGame()

  override def handleEvent(event: GameEvent): Unit = this.gameLoop.addAction(event)

  override def gameOver(): Unit = ???

  override def newLevel(): Unit = ???

  override def stopExecutorService(): Unit = this.executorService.shutdown()
}

package controller

import model._
import utils.ApplicationConstants.GAME_LOOP_STEP
import view._

import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

trait Controller {

  def stopExecutorService()
  def gameOver()
  def newLevel()
}

trait Observer {

  def handleEvent()
}

class ControllerImpl extends Controller with Observer {

  private val view: View = new ViewImpl()
  private val model: Model = new ModelImpl()
  private val executorService: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
  private val gameLoop: Runnable = new GameLoopImpl(model)

  private val observerManager: ObserverManager = new ObserverManagerImpl()

  this.view.setObserverManager(observerManager)

  this.executorService.scheduleAtFixedRate(gameLoop, 0, GAME_LOOP_STEP, TimeUnit.NANOSECONDS)

  this.view.startGame()

  override def handleEvent(): Unit = ???

  override def gameOver(): Unit = ???

  override def newLevel(): Unit = ???

  override def stopExecutorService(): Unit = this.executorService.shutdown()
}

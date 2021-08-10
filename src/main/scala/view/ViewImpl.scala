package view

import com.badlogic.gdx.backends.lwjgl3.{Lwjgl3Application, Lwjgl3ApplicationConfiguration}
import controller.ObserverManager
import main.LostInDungeons
import utils.ApplicationConstants.TITLE
import view.screens.game.GameScreen

import java.util.concurrent.{ExecutorService, Executors}

trait View {

  def setObserverManager(observerManager: ObserverManager)
  def startGame()
  def endGame()
  def initialize()
}

class ViewImpl extends View {

  //private val screenSetter: LostInDungeons = new LostInDungeons()
  private var application: Lwjgl3Application = _

  private var observerManager: ObserverManager = _

  val config = new Lwjgl3ApplicationConfiguration
  config.setTitle(TITLE)
  //config.addIcon(ICON_PATH, FileType.Internal)

  val executorService: ExecutorService = Executors.newSingleThreadExecutor()

  executorService.submit(() => {
    new Lwjgl3Application(LostInDungeons, config)
  })

  override def setObserverManager(observerManager: ObserverManager): Unit = this.observerManager = observerManager

  override def startGame(): Unit = LostInDungeons.setScreen(new GameScreen())

  override def endGame(): Unit = ???

  override def initialize(): Unit = ???


}

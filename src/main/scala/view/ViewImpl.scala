package view

import com.badlogic.gdx.backends.lwjgl3.{Lwjgl3Application, Lwjgl3ApplicationConfiguration}
import controller.ObserverManager
import main.LostInDungeons
import utils.ApplicationConstants.TITLE
import view.screens.game.GameScreen

trait View {

  def setObserverManager(observerManager: ObserverManager)
  def startGame()
  def endGame()
  def initialize()
}

class ViewImpl extends View {

  private val screenSetter: LostInDungeons = new LostInDungeons()

  private var observerManager: ObserverManager = _

  val config = new Lwjgl3ApplicationConfiguration
  config.setTitle(TITLE)
  //config.addIcon(ICON_PATH, FileType.Internal)
  new Lwjgl3Application(screenSetter, config)

  override def setObserverManager(observerManager: ObserverManager): Unit = this.observerManager = observerManager

  override def startGame(): Unit = this.screenSetter.setScreen(new GameScreen())

  override def endGame(): Unit = ???

  override def initialize(): Unit = ???


}

package view

import com.badlogic.gdx.Files.FileType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.{Lwjgl3Application, Lwjgl3ApplicationConfiguration}
import controller.ObserverManager
import main.LostInDungeons
import model.helpers.EntitiesGetter
import utils.ApplicationConstants.{ICON_PATH, TITLE}
import view.screens.helpers.TileMapHelper
import view.screens.menu.{GUIFactory, GameScreen}

import java.util.concurrent.{ExecutorService, Executors}

trait View {

  def startGame()

  def endGame()

  def returnToMenu()

  def terminate()
}

class ViewImpl(private val entitiesGetter: EntitiesGetter,
               private val observerManager: ObserverManager,
               private val tileMapHelper: TileMapHelper) extends View {

  private val screenSetter: LostInDungeons = new LostInDungeons(this.observerManager)

  val config = new Lwjgl3ApplicationConfiguration
  config.setTitle(TITLE)
  config.setWindowIcon(FileType.Internal, ICON_PATH)

  val executorService: ExecutorService = Executors.newSingleThreadExecutor()
  executorService.submit(() => {
    new Lwjgl3Application(screenSetter, config)
  })

  override def startGame(): Unit = {
    Gdx.app.postRunnable(() => this.screenSetter.setScreen(new GameScreen(this.entitiesGetter, this.observerManager, this.tileMapHelper)))
  }

  override def endGame(): Unit = Gdx.app.postRunnable(() => this.screenSetter.setScreen(GUIFactory.createGameOverScreen(this.observerManager)))

  override def returnToMenu(): Unit = Gdx.app.postRunnable(() => this.screenSetter.setScreen(GUIFactory.createMainMenuScreen(this.observerManager)))

  override def terminate(): Unit = {
    this.executorService.shutdownNow()
    Gdx.app.exit()
  }
}

package view

import com.badlogic.gdx.Files.FileType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.{Lwjgl3Application, Lwjgl3ApplicationConfiguration}
import controller.{EntitiesGetter, ObserverManager}
import main.LostInDungeons
import model.world.TileMapManager
import utils.ApplicationConstants.{ICON_PATH, TITLE}
import view.screen.{GUIFactory, GameScreen}

import java.util.concurrent.{ExecutorService, Executors}

/** Manages the current screen of the application
 */
trait View {

  /** Change the current screen and start the game.
   *
   */
  def startGame(): Unit

  /** Set the Game Over Screen, is called when the hero is dead.
   *
   */
  def endGame(): Unit

  /** Return to the Main Menu Screen.
   *
   */
  def returnToMenu(): Unit

  /** Close the application.
   *
   */
  def terminate(): Unit
}

/** Handles the graphics part of the game
 *
 *  @param entitiesGetter monitor that contains the entities of the Model, used to place sprites, score, level number
 *  @param observerManager observer for the messages from View to Controller
 *  @param tileMapManager class for map rendering
 */
class ViewImpl(private val entitiesGetter: EntitiesGetter,
               private val observerManager: ObserverManager,
               private  val tileMapManager: TileMapManager) extends View {

  private val screenSetter: LostInDungeons = new LostInDungeons(this.observerManager)

  //configuration for the libgdx application
  val config = new Lwjgl3ApplicationConfiguration
  config.setTitle(TITLE)
  config.setWindowIcon(FileType.Internal, ICON_PATH)

  val executorService: ExecutorService = Executors.newSingleThreadExecutor()
  executorService.submit(() => new Lwjgl3Application(screenSetter, config))

  override def startGame(): Unit = {
    Gdx.app.postRunnable(() => this.screenSetter.setScreen(new GameScreen(this.entitiesGetter, this.observerManager, this.tileMapManager)))
  }

  override def endGame(): Unit = Gdx.app.postRunnable(() => this.screenSetter.setScreen(GUIFactory.createGameOverScreen(this.observerManager)))

  override def returnToMenu(): Unit = Gdx.app.postRunnable(() => this.screenSetter.setScreen(GUIFactory.createMainMenuScreen(this.observerManager)))

  override def terminate(): Unit = {
    this.executorService.shutdownNow()
    Gdx.app.exit()
  }
}

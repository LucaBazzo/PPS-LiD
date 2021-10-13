package main

import com.badlogic.gdx.{Game, Screen}
import controller.{GameEvent, ObserverManager}
import view.screen.{GUIFactory, GameScreen}

/** The class that represents the whole game, as viewed from the LibGDX
 * framework. It starts the application by showing the initial menu screen.
 *
 * Here are also caught application window closing and resize events.
 *
 *  @param observerManager observer for the messages from View to Controller
 */
class LostInDungeons(private val observerManager: ObserverManager) extends Game {

  /** Create the game. This method is called automatically when this class is
   * instantiated.
   */
  override def create(): Unit = {
    this.setScreen(GUIFactory.createMainMenuScreen(this.observerManager))
  }

  /** Dispose of the view thread. This method is called automatically when the
   * application window is closed. This action is executed differently from
   * the closure of the application started by pressing a specific keyboard
   * key. In fact, key press and release events are already caught by the
   * different screens.
   */
  override def dispose(): Unit = {
    this.getScreen.dispose()
    super.dispose()
    observerManager.notifyEvent(GameEvent.CloseApplication)
  }

  override def setScreen(screen: Screen): Unit = {
    this.getScreen match {
      case game: GameScreen => game.stopMusic()
      case _ =>
    }
    super.setScreen(screen)
  }
}

package main

import com.badlogic.gdx.Game
import controller.{GameEvent, ObserverManager}
import view.screen.GUIFactory

/** The class that represents the whole game, start the application from the menu.
 *
 *  @param observerManager observer for the messages from View to Controller
 */
class LostInDungeons(private val observerManager: ObserverManager) extends Game {

  /** Create the game. This method is called automatically when this class is instantiated.
   *
   */
  override def create(): Unit = {
    this.setScreen(GUIFactory.createMainMenuScreen(this.observerManager))
  }

  override def dispose(): Unit = {
    this.getScreen.dispose()
    super.dispose()
    observerManager.notifyEvent(GameEvent.CloseApplication)
  }
}

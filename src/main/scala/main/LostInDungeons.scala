package main

import com.badlogic.gdx.Game
import controller.Controller
import view.screens.menu._

object LostInDungeons extends Game {

  private var view: ScreenObserver = _

  /** Create the game starting from the ScreenObserver (and Controller), which will set the active screen to the main menu.
   * This method is called automatically when this class is instantiated.
   */
  override def create(): Unit = {
    // VIEW of the all game
    this.view = new ScreenObserverImpl()

    // CONTROLLER of the all game: set the screen manager as the current "screen" to observe
    new Controller().setCurrentObservable(this.view.asInstanceOf[ScreensMessenger])

    //TODO change to MainScreen when its ready
    view.setGameScreen()
  }

  override def dispose(): Unit = {
    this.view.dispose()
    super.dispose()
  }
}

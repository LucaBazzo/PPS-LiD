package controller

import model.{Model, PublicModel}
import view.screens.menu.ScreensMessenger

/** This class represent the Controller of the all game.
 */
class Controller extends ControllerObserver {

  private var view: Option[ScreensMessenger] = Option.empty
  private val model: PublicModel = new Model()

  override def setCurrentObservable(view: ScreensMessenger): Unit = {
    if(view == null) {
      throw new IllegalArgumentException("The view passed as input is null!")
    }
    this.view = Option.apply(view)
    this.view.get.startControllerObserving(this)
  }

  /** It allows to reset and start a new game.
   */
  override def requestNewGame(): Unit = this.model.restartGame()
}

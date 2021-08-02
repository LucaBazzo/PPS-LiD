package controller

import view.screens.menu.ScreensMessenger

/** Observer for controller; these methods will be notified from the view.
 */
trait ControllerObserver {

  def setCurrentObservable(view: ScreensMessenger)

  /** It allows to reset and start a new game.
   */
  def requestNewGame()
}

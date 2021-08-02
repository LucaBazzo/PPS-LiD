package view.screens.menu

import controller.ControllerObserver

/** This interface is used by the controller to
 * communicate with it by calling public methods.
 */
trait ScreensMessenger {

  /** To attach a controller as an observer.
   *
   * @param controller [[ControllerObserver]] of the game
   */
  def startControllerObserving(controller: ControllerObserver)
}



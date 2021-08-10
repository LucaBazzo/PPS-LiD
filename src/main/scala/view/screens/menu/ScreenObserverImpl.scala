package view.screens.menu

import controller.ControllerObserver

/** Implementation of ScreenObserver and ScreensMessenger interfaces for screen management.
 */
class ScreenObserverImpl extends ScreenObserver with ScreensMessenger {

  private var controller: Option[ControllerObserver] = Option.empty

  private var gameScreen: ObservableScreen = _

  override def setMainMenuScreen(): Unit = ???

  override def setGameScreen(): Unit = {
    //this.gameScreen = new GameScreen()
    this.gameScreen.setObserver(this)
    startScreen(this.gameScreen)
  }

  override def setGameOverScreen(score: Int): Unit = ???

  override def closeGame(): Unit = ???

  override def dispose(): Unit = {
    if(this.gameScreen != null) {
      this.gameScreen.dispose()
    }
  }

  override def startControllerObserving(controller: ControllerObserver): Unit = {
    if(controller == null) {
      throw new IllegalArgumentException("The controller passed as input in startControllerObserving is null!")
    }
    this.controller = Option.apply(controller)
  }


  private def startScreen(observableScreen: ObservableScreen): Unit = {
    if(observableScreen != null) {
      //LostInDungeons.setScreen(observableScreen)
    }
  }
}

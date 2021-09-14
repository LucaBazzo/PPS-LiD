package main

import com.badlogic.gdx.Game
import controller.{GameEvent, ObserverManager}
import view.screens.menu.GUIFactory

class LostInDungeons(private val observerManager: ObserverManager) extends Game {

  override def create(): Unit = {
    this.setScreen(GUIFactory.createMainMenuScreen(this.observerManager))
  }

  override def dispose(): Unit = {
    this.getScreen.dispose()
    super.dispose()
    observerManager.notifyEvent(GameEvent.CloseApplication)
  }
}

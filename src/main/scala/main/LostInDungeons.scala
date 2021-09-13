package main

import com.badlogic.gdx.Game
import controller.{GameEvent, ObserverManager}
import view.screens.menu.MainMenuScreen

class LostInDungeons(private val observerManager: ObserverManager) extends Game {

  override def create(): Unit = {
    this.setScreen(new MainMenuScreen(this.observerManager))
  }

  override def dispose(): Unit = {
    this.getScreen.dispose()
    super.dispose()
    observerManager.notifyEvent(GameEvent.CloseApplication)
  }
}

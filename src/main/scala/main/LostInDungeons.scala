package main

import com.badlogic.gdx.Game
import controller.ObserverManager
import model.EntitiesGetter
import view.screens.game.GameScreen

class LostInDungeons(private val entitiesGetter: EntitiesGetter,
                     private val observerManager: ObserverManager) extends Game {

  override def create(): Unit = {
    this.setScreen(new GameScreen(entitiesGetter, observerManager))
  }

  override def dispose(): Unit = {
    this.getScreen.dispose()
    super.dispose()
  }
}

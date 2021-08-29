package main

import com.badlogic.gdx.Game
import com.badlogic.gdx.physics.box2d.World
import controller.{GameEvent, ObserverManager}
import model.Level
import model.helpers.EntitiesGetter
import view.screens.game.GameScreen

class LostInDungeons(private val entitiesGetter: EntitiesGetter,
                     private val observerManager: ObserverManager,
                     private val level: Level) extends Game {

  override def create(): Unit = {
    this.setScreen(new GameScreen(entitiesGetter, observerManager, level))
  }

  override def dispose(): Unit = {
    this.getScreen.dispose()
    super.dispose()
    observerManager.notifyEvent(GameEvent.CloseApplication)
  }
}

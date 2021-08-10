package main

import com.badlogic.gdx.Game
import view.screens.game.GameScreen

object LostInDungeons extends Game {

  override def create(): Unit = {
    this.setScreen(new GameScreen())
  }

  override def dispose(): Unit = {
    this.getScreen.dispose()
    super.dispose()
  }
}

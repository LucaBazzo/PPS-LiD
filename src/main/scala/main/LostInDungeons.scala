package main

import com.badlogic.gdx.Game
import view.screens.game.{GameScreen, InitializationScreen}

object LostInDungeons extends Game {

  override def create(): Unit = {
//    this.setScreen(new InitializationScreen())
  }

  override def dispose(): Unit = {
    this.getScreen.dispose()
    super.dispose()
  }
}

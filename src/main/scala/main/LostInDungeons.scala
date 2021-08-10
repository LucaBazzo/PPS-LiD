package main

import com.badlogic.gdx.{Game, Screen}

class LostInDungeons extends Game {

  override def create(): Unit = {
    println("ciao")

  }

  override def setScreen(screen: Screen): Unit = {
    super.setScreen(screen)
    println(this.getScreen())
  }

  override def dispose(): Unit = {
    this.getScreen.dispose()
    super.dispose()
  }
}

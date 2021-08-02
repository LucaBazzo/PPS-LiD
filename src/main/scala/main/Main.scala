package main

import com.badlogic.gdx.backends.lwjgl3.{Lwjgl3Application, Lwjgl3ApplicationConfiguration}

object Main {

  private val ICON_PATH = "Sprites/" + "icon_32x32.png"
  private val TITLE = "Lost in Dungeons"

  def main(args:Array[String]): Unit = {
    val config = new Lwjgl3ApplicationConfiguration

    config.setTitle(TITLE)
    //config.addIcon(ICON_PATH, FileType.Internal)
    new Lwjgl3Application(LostInDungeons, config)
  }

}

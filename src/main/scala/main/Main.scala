package main

import com.badlogic.gdx.Gdx
import controller.ControllerImpl

object Main {

  private val ICON_PATH = "Sprites/" + "icon_32x32.png"
  private val TITLE = "Lost in Dungeons"

  private var l: List[Int] = List(1,2,3,4,5,6,7,8,9)

  def getEntities(predicate: Int => Boolean): List[Int] = this.l.filter(predicate)

  def main(args:Array[String]): Unit = {

    new ControllerImpl()

    /*println(getEntities((x:Int) => x > 5))

    */
  }

}

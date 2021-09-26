package main

import controller.ControllerImpl

/** The Main class of Lost in Dungeons.
 */
object Main {

  /** The starting point of the application.
   *
   * @param args unused.
   */
  def main(args:Array[String]): Unit = {
    new ControllerImpl()
  }

}

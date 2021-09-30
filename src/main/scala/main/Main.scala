package main

import controller.ControllerImpl

/** Entry point class of the application Lost in Dungeons.
 */
object Main {

  /** The starting point of the application. Initialization of both the
   * application window and the game world are demanded to the Controller
   * itself.
   *
   * @param args application input arguments. Unused.
   */
  def main(args:Array[String]): Unit = {
    new ControllerImpl()
  }

}

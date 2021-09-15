package main

import controller.ControllerImpl

/** The Main class of Lost in Dungeons.
 */
object Main {

  // TODO: trasformare tutte le chiamate a metodi in EntitiesFactori in createPendingEntity(() => ...)

  /** The starting point of the application.
   *
   * @param args unused.
   */
  def main(args:Array[String]): Unit = {
    new ControllerImpl()
  }

}

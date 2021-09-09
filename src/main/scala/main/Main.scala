package main

import controller.ControllerImpl

object Main {

  // TODO: trasformare tutte le chiamate a metodi in EntitiesFactori in createPendingEntity(() => ...)

  def main(args:Array[String]): Unit = {
    new ControllerImpl()
  }

}

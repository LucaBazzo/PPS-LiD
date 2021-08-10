package controller

import model.Model

class GameLoopImpl(val model: Model) extends Runnable {

  override def run(): Unit = {
    this.model.update()
    this.checkEndGame()
  }

  private def checkEndGame() = {

  }
}

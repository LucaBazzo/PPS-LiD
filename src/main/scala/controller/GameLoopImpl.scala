package controller

import model.Model

class GameLoopImpl(val model: Model) extends Runnable {

  override def run(): Unit = {
    this.model.update()
    this.checkEndGame()
    ScreenQueue.setScreen(0)
  }

  private def checkEndGame() = {

  }
}

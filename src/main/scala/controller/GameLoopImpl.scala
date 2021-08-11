package controller

import model.Model

trait GameLoop extends Runnable {

  def addAction(action: Int)
}

class GameLoopImpl(private val model: Model) extends GameLoop {

  private var actions: List[Int] = List.empty

  override def run(): Unit = {
    val currentActions = extractAndEmptyTheActions()
    this.model.update(currentActions)
    this.checkEndGame()
  }

  override def addAction(action: Int): Unit = synchronized {
    this.actions = action :: this.actions
  }

  private def extractAndEmptyTheActions(): List[Int] = synchronized {
    val currentActions = actions map identity
    this.actions = List.empty
    currentActions
  }

  private def checkEndGame() = {

  }
}

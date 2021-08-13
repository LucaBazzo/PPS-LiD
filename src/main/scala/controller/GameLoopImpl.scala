package controller

import controller.GameEvent.GameEvent
import model.Model

trait GameLoop extends Runnable {

  def addAction(action: GameEvent)
}

class GameLoopImpl(private val model: Model) extends GameLoop {

  private var actions: List[GameEvent] = List.empty

  override def run(): Unit = {
    val currentActions = extractAndEmptyTheActions()
    this.model.update(currentActions)
    this.checkEndGame()
  }

  override def addAction(action: GameEvent): Unit = synchronized {
    this.actions = action :: this.actions
  }

  private def extractAndEmptyTheActions(): List[GameEvent] = synchronized {
    val currentActions = actions map identity
    this.actions = List.empty
    currentActions
  }

  private def checkEndGame() = {

  }
}

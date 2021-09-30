package controller

import controller.GameEvent.GameEvent
import model.Model

/** Stop the game loop and the messages from the View to the Model.
 */
trait GameLoop extends Runnable {

  /** Add game events from the View, each action will be notified to the Model in the next step.
   *
   *  @param action the view event
   */
  def addAction(action: GameEvent): Unit
}

/** Game loop that runs periodically and carries events from the View to the Model.
 *
 *  @param model where the view's messages will be sent
 *  @param controller the controller of the game, is notified when the game is over
 */
class GameLoopImpl(private val model: Model, private val controller: Controller) extends GameLoop {

  private var actions: List[GameEvent] = List.empty
  private var gameOverNotified: Boolean = false

  override def run(): Unit = {
    val currentActions = extractAndEmptyTheActions()
    this.model.update(currentActions)

    if (this.model.isGameOver && !gameOverNotified) this.gameOver()
  }

  override def addAction(action: GameEvent): Unit = synchronized {
    this.actions = action :: this.actions
  }

  private def extractAndEmptyTheActions(): List[GameEvent] = synchronized {
    val currentActions = actions map identity
    this.actions = List.empty
    currentActions
  }

  private def gameOver(): Unit = {
    this.controller.gameOver()
    this.gameOverNotified = true
  }
}

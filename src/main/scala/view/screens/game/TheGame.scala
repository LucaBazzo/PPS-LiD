package view.screens.game

/** Interface for the game view; is used by the controller to communicate with it.
 */
trait TheGame {

  /** Send the current score to the view.
   *
   * @param currentScore The current score of the player
   */
  def sendScore(currentScore: Int)
}

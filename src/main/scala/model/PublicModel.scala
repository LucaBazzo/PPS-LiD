package model

/** Interface for the MODEL in the MVC pattern. Represents the public part for
 * the controller.
 */
trait PublicModel {

  /** Getter for the actual game score.
   *
   * @return the actual score
   */
  def getCurrentScore: Int

  /** Reset the current score.
   */
  def resetCurrentScore()

  /** Restart the game.
   */
  def restartGame()
}

package view.screens.menu

/** Interface for screen setting management.
 */
trait ScreenObserver {

  /** Set the main menu screen.
   */
  def setMainMenuScreen()

  /** Set the game view screen.
   */
  def setGameScreen()

  /** Set the game over screen.
   *
   * @param score The score at the end of the game
   */
  def setGameOverScreen(score: Int)

  /** Close the game.
   */
  def closeGame()

  /** Release all resources.
   */
  def dispose()
}

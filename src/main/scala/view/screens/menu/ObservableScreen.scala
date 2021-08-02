package view.screens.menu

import com.badlogic.gdx.Screen
import com.badlogic.gdx.scenes.scene2d.Stage

/** Notify the observer when a certain event occurs.
 */
trait ObservableScreen extends Screen{

  /** Get the stage where all the objects of the view are contained.
   *
   * @return The stage
   */
  def getStage: Stage

  /** Set the screen observer.
   *
   * @param screenObserver The Observer
   */
  def setObserver(screenObserver: ScreenObserver)
}

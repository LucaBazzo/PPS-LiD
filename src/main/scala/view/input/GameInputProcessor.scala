package view.input

import com.badlogic.gdx.Input.{Buttons, Keys}
import com.badlogic.gdx.InputProcessor
import controller.{GameEvent, ObserverManager}

/** Class that implements the inputProcessor interface for the management of
 *  the buttons from the keyboard.
 *
 *  @param observerManager The manager that send messages from View to Controller
 */
class GameInputProcessor(private val observerManager: ObserverManager) extends InputProcessor {

  override def keyDown(i: Int): Boolean = i match {
      case Keys.ESCAPE =>
        this.observerManager.notifyEvent(GameEvent.CloseApplication)
        true
      case Keys.W | Keys.UP =>
        this.observerManager.notifyEvent(GameEvent.Up)
        true
      case Keys.E => this.observerManager.notifyEvent(GameEvent.Slide)
        true
      case Keys.S | Keys.DOWN =>
        this.observerManager.notifyEvent(GameEvent.Down)
        true
      case Keys.SPACE | Keys.ENTER =>
        this.observerManager.notifyEvent(GameEvent.Interaction)
        true
      case _ => false
    }


  override def keyUp(i: Int): Boolean = i match {
      case Keys.S | Keys.DOWN =>
        this.observerManager.notifyEvent(GameEvent.DownReleased)
        true
      case Keys.W | Keys.UP =>
        this.observerManager.notifyEvent(GameEvent.UpReleased)
        true
      case _ => false
  }

  override def keyTyped(c: Char): Boolean = {false}

  override def touchDown(x: Int, y: Int, pointer: Int, button: Int): Boolean = button match {
      case Buttons.LEFT =>
        this.observerManager.notifyEvent(GameEvent.Attack)
        true
      case Buttons.RIGHT =>
        this.observerManager.notifyEvent(GameEvent.BowAttack)
        true
      case _ =>
        false
  }

  // This and the methods below are not implemented because they are not useful as input for the game
  override def touchUp(i: Int, i1: Int, i2: Int, i3: Int): Boolean = false

  override def touchDragged(i: Int, i1: Int, i2: Int): Boolean = false

  override def mouseMoved(i: Int, i1: Int): Boolean = false

  override def scrolled(v: Float, v1: Float): Boolean = false
}

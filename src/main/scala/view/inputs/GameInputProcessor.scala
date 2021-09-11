package view.inputs

import com.badlogic.gdx.Input.{Buttons, Keys}
import com.badlogic.gdx.InputProcessor
import controller.{GameEvent, ObserverManager}

class GameInputProcessor(private val observerManager: ObserverManager) extends InputProcessor{

  override def keyDown(i: Int): Boolean = {
    i match {
      case Keys.ESCAPE => {
        this.observerManager.notifyEvent(GameEvent.CloseApplication)
      }
      case Keys.W | Keys.UP => this.observerManager.notifyEvent(GameEvent.Up)
      case Keys.E => this.observerManager.notifyEvent(GameEvent.Slide)
      case Keys.S | Keys.DOWN => this.observerManager.notifyEvent(GameEvent.Down)
      case Keys.SPACE | Keys.ENTER => this.observerManager.notifyEvent(GameEvent.Interaction)
      case _ => return false
    }
    true
  }

  override def keyUp(i: Int): Boolean = {
    i match {
      case Keys.S | Keys.DOWN => this.observerManager.notifyEvent(GameEvent.DownReleased)
      case Keys.W | Keys.UP => this.observerManager.notifyEvent(GameEvent.UpReleased)
      case _ => return false
    }
    true
  }

  override def keyTyped(c: Char): Boolean = {false}

  override def touchDown(x: Int, y: Int, pointer: Int, button: Int): Boolean = {
    button match {
      case Buttons.LEFT => this.observerManager.notifyEvent(GameEvent.Attack)
      case Buttons.RIGHT => this.observerManager.notifyEvent(GameEvent.BowAttack)
      case _ => return false
    }
    true
  }

  override def touchUp(i: Int, i1: Int, i2: Int, i3: Int): Boolean = {false}

  override def touchDragged(i: Int, i1: Int, i2: Int): Boolean = {false}

  override def mouseMoved(i: Int, i1: Int): Boolean = {false}

  override def scrolled(v: Float, v1: Float): Boolean = {false}
}

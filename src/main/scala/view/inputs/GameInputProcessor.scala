package view.inputs

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.{Gdx, InputProcessor}
import controller.{GameEvent, ObserverManager}

class GameInputProcessor(private val observerManager: ObserverManager) extends InputProcessor{

  override def keyDown(i: Int): Boolean = {
    i match {
      case Keys.ESCAPE => Gdx.app.exit()
      case Keys.W | Keys.SPACE | Keys.UP => this.observerManager.notifyEvent(GameEvent.Jump)
      case Keys.E => this.observerManager.notifyEvent(GameEvent.Slide)
      case Keys.S | Keys.DOWN => this.observerManager.notifyEvent(GameEvent.Crouch)
      case _ => return false
    }
    true
  }

  override def keyUp(i: Int): Boolean = {
    i match {
      case Keys.S | Keys.DOWN => this.observerManager.notifyEvent(GameEvent.StopCrouch)
      case _ => return false
    }
    true
  }

  override def keyTyped(c: Char): Boolean = {false}

  override def touchDown(i: Int, i1: Int, i2: Int, i3: Int): Boolean = {false}

  override def touchUp(i: Int, i1: Int, i2: Int, i3: Int): Boolean = {false}

  override def touchDragged(i: Int, i1: Int, i2: Int): Boolean = {false}

  override def mouseMoved(i: Int, i1: Int): Boolean = {false}

  override def scrolled(v: Float, v1: Float): Boolean = {false}
}

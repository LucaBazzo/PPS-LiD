package model.movement

import controller.GameEvent.GameEvent

trait MovementStrategy {

  def apply()
  def apply(command: GameEvent):Unit = ???
  def stopMovement():Unit = ???

  def alterSpeed(alteration: Float): Unit = ???
}


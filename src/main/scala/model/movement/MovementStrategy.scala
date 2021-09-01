package model.movement

import controller.GameEvent.GameEvent

trait MovementStrategy {

  def apply()
  def apply(command: GameEvent):Unit = ???
  def stopMovement():Unit = ???

  def alterSpeed(alteration: Float): Unit = ???
}

case class DoNothingMovementStrategy() extends MovementStrategy {

  override def apply(): Unit = {}

  override def apply(command: GameEvent): Unit = {}

  override def stopMovement(): Unit = {}

  override def alterSpeed(alteration: Float): Unit = {}
}

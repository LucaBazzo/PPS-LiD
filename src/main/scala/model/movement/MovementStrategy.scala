package model.movement

trait MovementStrategy {
  def move(): Unit

  def isMoving(): Boolean
}

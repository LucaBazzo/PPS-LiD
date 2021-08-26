package model.movement

import controller.GameEvent.GameEvent
import model.collisions.ImplicitConversions.RichFloat
import model.entities.{Hero, State}
import utils.ApplicationConstants.HERO_SIZE_SMALL

trait MovementStrategy {

  def apply()

  def apply(command: GameEvent): Unit = ???

  def stopMovement(): Unit = ???
}

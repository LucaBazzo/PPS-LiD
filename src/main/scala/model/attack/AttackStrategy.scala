package model.attack

import controller.GameEvent.GameEvent

trait AttackStrategy {

  def apply(): Unit = ???
  def apply(command: GameEvent):Unit = ???

  def stopAttack():Unit = ???
  def isAttackFinished: Boolean = ???

  def decrementAttackTimer():Unit = ???
  def checkTimeEvent():Unit = ???

  def alterStrength(alteration: Float): Unit = ???
}

case class DoNothingAttackStrategy() extends AttackStrategy {

  override def apply(command: GameEvent): Unit = {}

  override def stopAttack(): Unit = {}

  override def isAttackFinished: Boolean = true

  override def checkTimeEvent(): Unit = {}

  override def alterStrength(alteration: Float): Unit = {}
}
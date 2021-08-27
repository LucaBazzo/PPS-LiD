package model.attack

import controller.GameEvent.GameEvent

trait AttackStrategy {

  def apply(): Unit = ???
  def apply(command: GameEvent):Unit = ???

  def stopAttack():Unit = ???
  def isAttackFinished: Boolean = ???

  def decrementAttackTimer():Unit = ???
  def checkTimeEvent():Unit = ???

}

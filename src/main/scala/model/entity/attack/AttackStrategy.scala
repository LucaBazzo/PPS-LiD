package model.entity.attack

import controller.GameEvent.GameEvent

/** The strategy that a Living Entity use to attack another entity.
 *
 */
trait AttackStrategy {

  /** Apply the attack strategy.
   *
   */
  def apply(): Unit = { }

  /** Apply the attack strategy given a specific command.
   *
   *  @param command the attack command
   */
  def apply(command: GameEvent): Unit = { }

  /** Stop the current attack.
   *
   */
  def stopAttack(): Unit = { }

  /** Check if the attack is finished.
   *
   *  @return true if the attack was stopped or has ended correctly
   */
  def isAttackFinished: Boolean = true

  /** Decrement the timer that each attack has.
   *
   */
  def decrementAttackTimer():Unit = { }

  /** Check if there is some event to be apply based by the attack timer.
   *
   */
  def checkTimeEvent(): Unit = { }

  /** Changes the strength of the attack.
   *
   * @param alteration the value to be added
   *
   */
  def alterStrength(alteration: Float): Unit = { }
}

/** Attack strategy that does nothing.
 *
 */
case class DoNothingAttackStrategy() extends AttackStrategy { }
package model.movement

import controller.GameEvent.GameEvent

/** The strategy that a Mobile Entity use to move inside the world.
 *
 */
trait MovementStrategy {

  def apply()

  /** Apply the movement strategy given a specific command.
   *
   *  @param command the movement command
   */
  def apply(command: GameEvent): Unit = ???

  /** Stop the movement
   *
   */
  def stopMovement():Unit = ???

  /** Changes the speed that will be applied to the movement.
   *
   * @param alteration the value to be added
   *
   */
  def alterSpeed(alteration: Float): Unit = ???


  def onBegin(): Unit = ???

  def onEnd(): Unit = ???
}

/** Movement strategy that does nothing.
 *
 */
case class DoNothingMovementStrategy() extends MovementStrategy {

  override def apply(): Unit = {}

  override def apply(command: GameEvent): Unit = {}

  override def stopMovement(): Unit = {}

  override def alterSpeed(alteration: Float): Unit = {}

  override def onBegin(): Unit = {}

  override def onEnd(): Unit = {}
}

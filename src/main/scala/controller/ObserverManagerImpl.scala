package controller

import controller.GameEvent.GameEvent

/*
sealed trait GameEvent
case class StartGame() extends GameEvent
case class StopGame() extends GameEvent
case class CloseApplication() extends GameEvent
case class MoveUp() extends GameEvent
case class MoveDown() extends GameEvent
case class MoveLeft() extends GameEvent
case class MoveRight() extends GameEvent
case class Attack() extends GameEvent
*/

object GameEvent extends Enumeration {
  type GameEvent = Value
  val StartGame, StopGame, CloseApplication, SetMap,
      Up, UpReleased, MoveRight, MoveLeft, Slide, Down, DownReleased,
      Interaction,
      Attack, BowAttack = Value
}

/** Observer for controller; these methods will be notified from the view.
 */
trait Observer {

  /** Notifies the observer with the event given.
   *
   * @param event the event generated from observable
   */
  def handleEvent(event: GameEvent)

}

trait ObserverManager {

  def addObserver(observer: Observer)
  def removeObserver(observer: Observer)
  def notifyEvent(event: GameEvent)
}

class ObserverManagerImpl extends ObserverManager {

  private var observers: List[Observer] = List.empty

  override def addObserver(observer: Observer): Unit = this.observers = observer :: this.observers

  override def removeObserver(observer: Observer): Unit = {
    this.observers = observers.filterNot((obs: Observer) => obs.equals(observer))
  }

  override def notifyEvent(event: GameEvent): Unit = for(obs <- observers) obs.handleEvent(event)
}

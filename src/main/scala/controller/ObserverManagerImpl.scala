package controller

import controller.GameEvent.GameEvent

object GameEvent extends Enumeration {
  type GameEvent = Value
  val StartGame, ReturnToMenu, CloseApplication, SetMap, MapLoaded,
      Up, UpReleased, MoveRight, MoveLeft, Slide, Down, DownReleased,
      Interaction, Attack, BowAttack = Value
}

/** Observer for controller; these methods will be notified from the view.
 */
trait Observer {

  /** Notifies the observer with the event given.
   *
   * @param event the event generated from observable
   */
  def handleEvent(event: GameEvent): Unit

}

trait ObserverManager {

  def addObserver(observer: Observer): Unit
  def removeObserver(observer: Observer): Unit
  def notifyEvent(event: GameEvent): Unit
}

class ObserverManagerImpl extends ObserverManager {

  private var observers: List[Observer] = List.empty

  override def addObserver(observer: Observer): Unit = this.observers = observer :: this.observers

  override def removeObserver(observer: Observer): Unit = {
    this.observers = observers.filterNot((obs: Observer) => obs.equals(observer))
  }

  override def notifyEvent(event: GameEvent): Unit = for(obs <- observers) obs.handleEvent(event)
}

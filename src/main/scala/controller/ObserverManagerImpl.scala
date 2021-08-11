package controller

trait ObserverManager {

  def addObserver(observer: Observer)
  def removeObserver(observer: Observer)

  def notifyEvent(event: Int)
}

class ObserverManagerImpl extends ObserverManager {

  private var observers: List[Observer] = List.empty

  override def addObserver(observer: Observer): Unit = this.observers = observer :: this.observers

  override def removeObserver(observer: Observer): Unit = {
    this.observers = observers.filterNot((obs: Observer) => obs.equals(observer))
  }

  override def notifyEvent(event: Int): Unit = for(obs <- observers) obs.handleEvent(event)
}

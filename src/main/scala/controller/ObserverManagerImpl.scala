package controller

trait ObserverManager {

  def addObserver()
  def removeObserver()

  def notifyEvent()
}

class ObserverManagerImpl extends ObserverManager {

  override def addObserver(): Unit = ???

  override def removeObserver(): Unit = ???

  override def notifyEvent(): Unit = ???
}

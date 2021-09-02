package model.collisions

trait CollisionMonitor {
  def isPlayerInsideLava: Boolean

  def playerOutOfLava(): Unit

  def playerInLava(): Unit
}

class CollisionMonitorImpl extends CollisionMonitor {
  private var insideLava: Boolean = false

  override def isPlayerInsideLava: Boolean = synchronized {
    this.insideLava
  }

  override def playerOutOfLava(): Unit = this.insideLava = false

  override def playerInLava(): Unit = this.insideLava = true
}

package model.collisions

trait CollisionMonitor {
  def isPlayerInsideLava: Boolean

  def playerOutOfLava(): Unit

  def playerInLava(): Unit

  def isPlayerOnLadder: Boolean

  def playerQuitLadder(): Unit

  def playerOnLadder(): Unit
}

class CollisionMonitorImpl extends CollisionMonitor {
  private var insideLava: Boolean = false
  private var onLadder: Boolean = false

  override def isPlayerInsideLava: Boolean = synchronized {
    this.insideLava
  }

  override def playerOutOfLava(): Unit = synchronized {
    this.insideLava = false
  }

  override def playerInLava(): Unit = synchronized {
    this.insideLava = true
  }

  override def isPlayerOnLadder: Boolean = synchronized {
    this.onLadder
  }

  override def playerQuitLadder(): Unit = synchronized {
    this.onLadder = false
  }

  override def playerOnLadder(): Unit = synchronized {
    this.onLadder = true
  }
}

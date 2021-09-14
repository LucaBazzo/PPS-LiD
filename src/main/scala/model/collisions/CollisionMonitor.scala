package model.collisions

trait CollisionMonitor {
  def isPlayerInsideLava: Boolean

  def playerOutOfLava(): Unit

  def playerInLava(): Unit

  def isPlayerOnLadder: Boolean

  def playerQuitLadder(): Unit

  def playerOnLadder(): Unit

  def isPlayerTouchingPlatformEdges: Boolean

  def playerTouchesPlatformEdge(): Unit

  def playerQuitPlatform(): Unit
}

class CollisionMonitorImpl extends CollisionMonitor {
  private var insideLava: Boolean = false
  private var onLadder: Boolean = false
  private var touchingPlatformEdges: Boolean = false

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

  override def isPlayerTouchingPlatformEdges: Boolean = synchronized {
    this.touchingPlatformEdges
  }

  override def playerTouchesPlatformEdge(): Unit = synchronized {
    this.touchingPlatformEdges = true
  }

  override def playerQuitPlatform(): Unit = synchronized {
    this.touchingPlatformEdges = false
  }
}

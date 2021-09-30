package model.entity.collision

/** A Monitor that tracks the actual state of Hero's interactions with world props
 *
 */
trait CollisionMonitor {

  /** Check if the Hero is touching a ladder
   *
   * @return true if the hero is touching a ladder
   */
  def isPlayerOnLadder: Boolean

  /** The Hero is not touching a ladder anymore
   *
   */
  def playerQuitLadder(): Unit

  /** The Hero is touching a ladder
   *
   */
  def playerOnLadder(): Unit

  /** Check if the Hero is touching a platform
   *
   * @return true if the hero is touching a platform
   */
  def isPlayerTouchingPlatformEdges: Boolean

  /** The Hero is touching a platform
   *
   */
  def playerTouchesPlatformEdge(): Unit

  /** The Hero is not touching a platform anymore
   *
   */
  def playerQuitPlatform(): Unit
}

/** Implementation of the Collision Monitor trait
 *
 */
class CollisionMonitorImpl extends CollisionMonitor {
  private var onLadder: Boolean = false
  private var touchingPlatformEdges: Boolean = false

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

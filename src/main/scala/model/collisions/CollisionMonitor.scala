package model.collisions

/** A Monitor that tracks the actual state of Hero's interactions with world props
 *
 */
trait CollisionMonitor {

  /** Check if the Hero is inside of lava
   *
   * @return true if the hero is in lava
   */
  def isPlayerInsideLava: Boolean

  /** The hero just escaped from lava
   *
   */
  def playerOutOfLava(): Unit

  /** The Hero fell into lava
   *
   */
  def playerInLava(): Unit

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

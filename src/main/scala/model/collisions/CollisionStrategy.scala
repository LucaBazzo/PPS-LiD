package model.collisions

import model.entities.Entity

/** Represent a generic collision with an entity
 *
 */
trait CollisionStrategy {

  /** A new collision is detected
   *
   * @param entity the entity that entered in collision with this
   */
  def apply(entity: Entity): Unit

  /** A collision ends
   *
   * @param entity the entity that ended the collision
   */
  def release(entity: Entity): Unit
}

/** Represent the strategy given mostly to walls and other objects that doesn't have
 * any particular behaviour when colliding with something
 *
 */
case class DoNothingOnCollision() extends CollisionStrategy {
  override def apply(entity: Entity): Unit = {}

  override def release(entity: Entity): Unit = {}
}

package model.entity.collision

import model.entity.Entity

/** Represent a generic collision with an entity
 *
 */
trait CollisionStrategy {

  def apply(): Unit = { }

  /** A new collision is detected
   *
   * @param entity the entity that entered in collision with this
   */
  def contact(entity: Entity): Unit = { }

  /** A collision ends
   *
   * @param entity the entity that ended the collision
   */
  def release(entity: Entity): Unit = { }
}

/** Represent the strategy given mostly to walls and other objects that doesn't have
 * any particular behaviour when colliding with something
 *
 */
case class DoNothingCollisionStrategy() extends CollisionStrategy { }

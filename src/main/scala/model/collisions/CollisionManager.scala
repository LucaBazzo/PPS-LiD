package model.collisions

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import model.Level
import model.entities.Entity

class CollisionManager(private val level: Level) extends ContactListener {

  override def beginContact(contact: Contact): Unit = {
    val bodyA: Body = contact.getFixtureA.getBody
    val bodyB: Body = contact.getFixtureB.getBody

    val entityA: Entity = level.getEntity((x: Entity) => x.getBody equals bodyA)
    val entityB: Entity = level.getEntity((x: Entity) => x.getBody equals bodyB)

    entityA.collisionDetected(entityB)
    entityB.collisionDetected(entityA)
  }

  override def endContact(contact: Contact): Unit = {
  }

  override def preSolve(contact: Contact, manifold: Manifold): Unit = {
  }

  override def postSolve(contact: Contact, contactImpulse: ContactImpulse): Unit = {
  }

  def vectorScalar(vector: Vector2, scalar: Float = Gdx.graphics.getDeltaTime) = new Vector2(vector.x * scalar, vector.y * scalar)
}

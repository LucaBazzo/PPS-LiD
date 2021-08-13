package model

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._

class CollisionManager(private val level: Level) extends ContactListener {

  override def beginContact(contact: Contact): Unit = {
    val bodyA: Body = contact.getFixtureA.getBody
    val bodyB: Body = contact.getFixtureB.getBody

    println(bodyA)
    println(bodyB)

    println(contact.getFixtureA.getFilterData.categoryBits)
    println(contact.getFixtureB.getFilterData.categoryBits)

    /*val entityA: Entity = level.getEntity((x: Entity) => x.getBody equals bodyA)
    val entityB: Entity = level.getEntity((x: Entity) => x.getBody equals bodyB)

    println(entityA)
    println(entityB)*/
  }

  override def endContact(contact: Contact): Unit = ???

  override def preSolve(contact: Contact, manifold: Manifold): Unit = ???

  override def postSolve(contact: Contact, contactImpulse: ContactImpulse): Unit = ???

  def vectorScalar(vector: Vector2, scalar: Float = Gdx.graphics.getDeltaTime) = new Vector2(vector.x * scalar, vector.y * scalar)
}

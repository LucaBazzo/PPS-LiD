package model.collisions

import com.badlogic.gdx.physics.box2d._
import model.Level
import model.entities.Entity
import ImplicitConversions._
import com.badlogic.gdx.math.Vector2
import _root_.utils.ApplicationConstants.PIXELS_PER_METER

object EntityType extends Enumeration {

  private var currentBitValue: Short = 1
  private val bitMulti: Int = 2

  /** Collision bit used in fixture filters for recognizing the player.
   */
  val Hero: Short = currentBitValue
  val Enemy: Short = getNextBitValue
  val Mobile: Short = getNextBitValue
  val Immobile: Short = getNextBitValue
  val Item: Short = getNextBitValue
  val Sword: Short = getNextBitValue

  private def getNextBitValue: Short = {
    this.currentBitValue = this.currentBitValue * bitMulti
    this.currentBitValue
  }

}

object ImplicitConversions {

  implicit def intToShort(value: Int): Short = {
    value.toShort
  }

  implicit def tupleToVector2(tuple: (Float, Float)): Vector2 = {
    new Vector2(tuple._1, tuple._2)
  }

  implicit class RichFloat(base: Float) {
    def PPM: Float = base / PIXELS_PER_METER
  }

  implicit class RichInt(base: Int) {
    def PPM: Float = base / PIXELS_PER_METER
  }

  implicit class RichTuple2(base: (Float, Float)) {
    def PPM: (Float, Float) = base / PIXELS_PER_METER

    def /(div: Float): (Float, Float) = (base._1 / div, base._2 / div)

    def *(mul: Float): (Float, Float) = (base._1 * mul, base._2 * mul)
  }
}

// TODO: come gestire collissioni continue?

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
}

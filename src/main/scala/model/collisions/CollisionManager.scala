package model.collisions

import _root_.utils.ApplicationConstants.PIXELS_PER_METER
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import model.collisions.ImplicitConversions._
import model.entities.Entity
import model.helpers.EntitiesGetter

object EntityCollisionBit {

  private var currentBitValue: Short = 1
  private val bitMulti: Int = 2

  /** Collision bit used in fixture filters for recognizing the player.
   */
  val Hero: Short = currentBitValue
  val HeroFoot: Short = currentBitValue
  val Wall: Short = getNextBitValue
  val Enemy: Short = getNextBitValue
  val Mobile: Short = getNextBitValue
  val Immobile: Short = getNextBitValue
  val Item: Short = getNextBitValue
  val Sword: Short = getNextBitValue
  val Door: Short = getNextBitValue
  val OpenedDoor: Short = getNextBitValue
  val DestroyedDoor: Short = getNextBitValue
  val Arrow: Short = getNextBitValue
  val EnemyAttack: Short = getNextBitValue
  val Platform: Short = getNextBitValue
  val Ladder: Short = getNextBitValue
  val Pool: Short = getNextBitValue

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

    def INV: (Float, Float) = (-base._1, -base._2)

    def /(div: Float): (Float, Float) = (base._1 / div, base._2 / div)

    def *(mul: Float): (Float, Float) = (base._1 * mul, base._2 * mul)

    def *(tuple: (Float, Float)): (Float, Float) = (base._1 * tuple._1, base._2 * tuple._2)

    def +(tuple: (Float, Float)): (Float, Float) = (base._1 + tuple._1, base._2 + tuple._2)

    def -(tuple: (Float, Float)): (Float, Float) = (base._1 - tuple._1, base._2 - tuple._2)

  }
}

case class CollidingEntities(firstEntity: Option[Entity], secondEntity: Option[Entity])

// TODO: come gestire collissioni continue?

class CollisionManager(private val entitiesGetter: EntitiesGetter) extends ContactListener {

  override def beginContact(contact: Contact): Unit = {
    val collidingEntities: CollidingEntities = getCollidingEntities(contact)

    if(collidingEntities.firstEntity.nonEmpty)
      collidingEntities.firstEntity.get.collisionDetected(collidingEntities.secondEntity)
    if(collidingEntities.secondEntity.nonEmpty)
      collidingEntities.secondEntity.get.collisionDetected(collidingEntities.firstEntity)
  }

  override def endContact(contact: Contact): Unit = {
    val collidingEntities: CollidingEntities = getCollidingEntities(contact)

    if(collidingEntities.firstEntity.nonEmpty)
      collidingEntities.firstEntity.get.collisionReleased(collidingEntities.secondEntity)
    if(collidingEntities.secondEntity.nonEmpty)
      collidingEntities.secondEntity.get.collisionReleased(collidingEntities.firstEntity)
  }

  override def preSolve(contact: Contact, manifold: Manifold): Unit = {
  }

  override def postSolve(contact: Contact, contactImpulse: ContactImpulse): Unit = {
  }

  private def getCollidingEntities(contact: Contact): CollidingEntities = {
    val firstBody: Body = contact.getFixtureA.getBody
    val secondBody: Body = contact.getFixtureB.getBody

    val firstEntities: List[Entity] = entitiesGetter.getEntities((x: Entity) => x.getBody equals firstBody).get
    val secondEntities: List[Entity] = entitiesGetter.getEntities((x: Entity) => x.getBody equals secondBody).get

    (firstEntities, secondEntities) match {
      case (List(), List()) => CollidingEntities(Option.empty, Option.empty)
      case (List(), _) => CollidingEntities(Option.empty, Option.apply(secondEntities.head))
      case (_, List()) => CollidingEntities(Option.apply(firstEntities.head), Option.empty)
      case (_, _) => CollidingEntities(Option.apply(firstEntities.head), Option.apply(secondEntities.head))
    }
  }

}

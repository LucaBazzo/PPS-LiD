package model.collisions

import _root_.utils.ApplicationConstants.PIXELS_PER_METER
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import model.helpers.ImplicitConversions._
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
  val Arrow: Short = getNextBitValue
  val EnemyAttack: Short = getNextBitValue
  val Platform: Short = getNextBitValue
  val PlatformSensor: Short = getNextBitValue
  val Ladder: Short = getNextBitValue
  val Portal: Short = getNextBitValue
  val Pool: Short = getNextBitValue

  private def getNextBitValue: Short = {
    this.currentBitValue = this.currentBitValue * bitMulti
    this.currentBitValue
  }
}

// TODO muovere in un file a parte



class CollisionManager(private val entitiesGetter: EntitiesGetter) extends ContactListener {

  override def beginContact(contact: Contact): Unit = {
    val collidingEntities: (Option[Entity], Option[Entity]) = getCollidingEntities(contact)

    if(collidingEntities._1.nonEmpty && collidingEntities._2.nonEmpty) {
      collidingEntities._1.get.collisionDetected(collidingEntities._2.get)
      collidingEntities._2.get.collisionDetected(collidingEntities._1.get)
    }
  }

  override def endContact(contact: Contact): Unit = {
    val collidingEntities: (Option[Entity], Option[Entity]) = getCollidingEntities(contact)

    if(collidingEntities._1.nonEmpty && collidingEntities._2.nonEmpty) {
      collidingEntities._1.get.collisionReleased(collidingEntities._2.get)
      collidingEntities._2.get.collisionReleased(collidingEntities._1.get)
    }
  }

  override def preSolve(contact: Contact, manifold: Manifold): Unit = { }

  override def postSolve(contact: Contact, contactImpulse: ContactImpulse): Unit = { }

  private def getCollidingEntities(contact: Contact): (Option[Entity], Option[Entity]) = {
    val firstBody: Body = contact.getFixtureA.getBody
    val secondBody: Body = contact.getFixtureB.getBody

    val firstEntities: List[Entity] = entitiesGetter.getEntities((x: Entity) => x.getBody equals firstBody).get
    val secondEntities: List[Entity] = entitiesGetter.getEntities((x: Entity) => x.getBody equals secondBody).get

    (firstEntities, secondEntities) match {
      case (List(), List()) => (Option.empty, Option.empty)
      case (List(), _) => (Option.empty, Option.apply(secondEntities.head))
      case (_, List()) => (Option.apply(firstEntities.head), Option.empty)
      case (_, _) => (Option.apply(firstEntities.head), Option.apply(secondEntities.head))
    }
  }
}

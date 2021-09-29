package model.entity.collision

import com.badlogic.gdx.physics.box2d._
import controller.EntitiesGetter
import model.entity.Entity

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

    val firstEntities: List[Entity] = entitiesGetter.getEntities((x: Entity) => x.getBody equals firstBody)
    val secondEntities: List[Entity] = entitiesGetter.getEntities((x: Entity) => x.getBody equals secondBody)

    (firstEntities, secondEntities) match {
      case (List(), List()) => (Option.empty, Option.empty)
      case (List(), _) => (Option.empty, Option.apply(secondEntities.head))
      case (_, List()) => (Option.apply(firstEntities.head), Option.empty)
      case (_, _) => (Option.apply(firstEntities.head), Option.apply(secondEntities.head))
    }
  }
}

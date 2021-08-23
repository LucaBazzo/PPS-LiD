package model.movement

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import controller.GameEvent.GameEvent
import model.collisions.EntityType
import model.entities.Entity
import model.helpers.EntitiesBits
import model.helpers.WorldUtilities.{checkBodyIsVisible, checkPointCollision, getBodiesDistance, isTargetOnTheRight}

class DoNotMove() extends MovementStrategy {
  override def apply(): Unit = { }

  override def apply(command: GameEvent): Unit = ???

  override def stopMovement(): Unit = ???
}

abstract class PatrolPlatform(val entity: Entity, val world: World) extends MovementStrategy {
  // TODO: move statistics to enemy class
  protected val maxMovementSpeed: Float = 3 // maximum horizontal velocity
  protected val acceleration: Float = 1 // strength of the force applied
  // TODO: link to implementation
  protected var lastMovementTime: Long = 0 // last time a force was applied to the body
  protected val movementImpulseFrequency: Float = 5000 // movement impulse frequency

  protected var isMovingLeft: Boolean = true

  override def apply(): Unit = {
    val canMoveToTheLeft: Boolean = checkMoveToTheLeft
    val canMoveToTheRight: Boolean = checkMoveToTheRight

    // change direction
    if (!canMoveToTheLeft && isMovingLeft || !canMoveToTheRight && !isMovingLeft)
      entity.getBody.setLinearVelocity(0, entity.getBody.getLinearVelocity.y)

    if (!canMoveToTheLeft && isMovingLeft) isMovingLeft = false
    if (!canMoveToTheRight && !isMovingLeft) isMovingLeft = true

    // apply movement to entity's body
    if (canMoveToTheLeft || canMoveToTheRight ) {
      println("ASD")
      if (isMovingLeft) {
        entity.getBody.applyLinearImpulse(new Vector2(-acceleration, 0), entity.getBody.getWorldCenter, true)
        if (entity.getBody.getLinearVelocity.x <= -maxMovementSpeed)
          entity.getBody.setLinearVelocity(-maxMovementSpeed, entity.getBody.getLinearVelocity.y)
      } else if (!isMovingLeft) {
        entity.getBody.applyLinearImpulse(new Vector2(+acceleration, 0), entity.getBody.getWorldCenter, true)
        if (entity.getBody.getLinearVelocity.x >= maxMovementSpeed)
          entity.getBody.setLinearVelocity(maxMovementSpeed, entity.getBody.getLinearVelocity.y)
      }
    }
  }

  override def apply(command: GameEvent): Unit = ???

  override def stopMovement(): Unit = ???

  protected def checkMoveToTheLeft: Boolean
  protected def checkMoveToTheRight: Boolean
}

//class PlatformPatrolWithSensors(override val entity: Entity, override  val world: World)
//  extends PatrolPlatform(entity, world) {
//  // populate the body with movement specific sensors
//  val lowerLeftSensor: Fixture = createLowerLeftSensor(entity.getBody)
//  val lowerRightSensor: Fixture = createLowerRightSensor(entity.getBody)
//
//  override protected def checkMoveToTheLeft: Boolean =
//    sensorIsIntersectingWith(lowerLeftSensor, EntitiesBits.WORLD_CATEGORY_BIT, world)
//
//  override protected def checkMoveToTheRight: Boolean =
//    sensorIsIntersectingWith(lowerRightSensor, EntitiesBits.WORLD_CATEGORY_BIT, world)
//
//}

class PlatformPatrolWithAABBTests(override val entity: Entity, override  val world: World)
  extends PatrolPlatform(entity, world) {

  override protected def checkMoveToTheLeft: Boolean =
    !checkPointCollision(world, entity.getPosition._1 - entity.getSize._1 - 0.5f,
      entity.getPosition._2, EntityType.Immobile) &&
      checkPointCollision(world, entity.getPosition._1 - entity.getSize._1 - 0.5f,
        entity.getPosition._2 - entity.getSize._2 - 0.5f, EntityType.Immobile)

  override protected def checkMoveToTheRight: Boolean =
    !checkPointCollision(world, entity.getPosition._1 + entity.getSize._1 + 0.5f,
      entity.getPosition._2, EntityType.Immobile) &&
      checkPointCollision(world, entity.getPosition._1 + entity.getSize._1 + 0.5f,
        entity.getPosition._2 - entity.getSize._2 - 0.5f, EntityType.Immobile)
}

class PatrolAndStopIfNearHero(override val entity:Entity, override val world: World, val targetEntity:Entity) extends PlatformPatrolWithAABBTests(entity, world) {
  // TODO: move statistics to enemy class
  protected val maxDistance: Float = 4
  protected val visibilityMaxHorizontalAngle: Float = 30 // defines a vision "cone" originating from the entity

  override def apply(): Unit = {
    if (!isHeroNear) {
      super.apply()
    } else {
      entity.getBody.setLinearVelocity(0, entity.getBody.getLinearVelocity.y)
    }
  }

  protected def isHeroNear: Boolean = {
    checkBodyIsVisible(world, entity.getBody, targetEntity.getBody, visibilityMaxHorizontalAngle) &&
      getBodiesDistance(entity.getBody, targetEntity.getBody) <= maxDistance
  }
}

class PatrolAndStopIfFacingHero(override val entity:Entity, override val world: World, val targetEntity:Entity) extends PlatformPatrolWithAABBTests(entity, world) {
  // TODO: move statistics to enemy class
  protected val maxDistance: Float = 4
  protected val visibilityMaxHorizontalAngle: Float = 30 // defines a vision "cone" originating from the entity

  override def apply(): Unit = {
    if (isHeroNear && (isTargetOnTheRight(entity.getBody, targetEntity.getBody) && !isMovingLeft
      || !isTargetOnTheRight(entity.getBody, targetEntity.getBody) && isMovingLeft)) {
      entity.getBody.setLinearVelocity(0, entity.getBody.getLinearVelocity.y)
    } else {
      super.apply()
    }
  }

  protected def isHeroNear: Boolean = {
    checkBodyIsVisible(world, entity.getBody, targetEntity.getBody, visibilityMaxHorizontalAngle) &&
      getBodiesDistance(entity.getBody, targetEntity.getBody) <= maxDistance
  }
}


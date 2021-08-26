package model.movement

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import model.collisions.EntityType
import model.entities.{Entity, MobileEntity}
import model.collisions.ImplicitConversions.RichInt
import model.helpers.WorldUtilities.{checkBodyIsVisible, checkPointCollision, getBodiesDistance, isTargetOnTheRight}

class DoNotMove() extends MovementStrategy {
  override def apply(): Unit = { }
}

class PatrolPlatform(val entity: MobileEntity, val world: World) extends MovementStrategy {
  // TODO: move statistics to enemy class
  protected val maxMovementSpeed: Float = 30.PPM // maximum horizontal velocity
  protected val acceleration: Float = 10.PPM // strength of the force applied

  protected var isMovingLeft: Boolean = true

  entity.setFacing(right = !isMovingLeft)

  override def apply(): Unit = {
    val canMoveToTheLeft: Boolean = checkMoveToTheLeft
    val canMoveToTheRight: Boolean = checkMoveToTheRight

    // change direction
    if (!canMoveToTheLeft && isMovingLeft || !canMoveToTheRight && !isMovingLeft)
      entity.getBody.setLinearVelocity(0, entity.getBody.getLinearVelocity.y)

      if (!canMoveToTheLeft && isMovingLeft) isMovingLeft = false
      if (!canMoveToTheRight && !isMovingLeft) isMovingLeft = true

      // update mobile entity direction
      entity.setFacing(right = !isMovingLeft)

    // apply movement to entity's body
    if (canMoveToTheLeft || canMoveToTheRight ) {
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

  protected def checkMoveToTheLeft: Boolean =
    !checkPointCollision(world, entity.getPosition._1 - entity.getSize._1 - 5.PPM,
      entity.getPosition._2, EntityType.Immobile) &&
      checkPointCollision(world, entity.getPosition._1 - entity.getSize._1 - 5.PPM,
        entity.getPosition._2 - entity.getSize._2 - 5.PPM, EntityType.Immobile)

  protected def checkMoveToTheRight: Boolean =
    !checkPointCollision(world, entity.getPosition._1 + entity.getSize._1 + 5.PPM,
      entity.getPosition._2, EntityType.Immobile) &&
      checkPointCollision(world, entity.getPosition._1 + entity.getSize._1 + 5.PPM,
        entity.getPosition._2 - entity.getSize._2 - 5.PPM, EntityType.Immobile)
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

class PatrolAndStopIfNearHero(override val entity:MobileEntity, override val world: World, val targetEntity:Entity) extends PatrolPlatform(entity, world) {
  // TODO: move statistics to enemy class
  protected val maxDistance: Float = 40.PPM
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

class PatrolAndStopIfFacingHero(override val entity:MobileEntity, override val world: World, val targetEntity:Entity) extends PatrolPlatform(entity, world) {
  // TODO: move statistics to enemy class
  protected val maxDistance: Float = 40.PPM
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


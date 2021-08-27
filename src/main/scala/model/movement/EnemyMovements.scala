package model.movement

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import model.Level
import model.collisions.EntityType
import model.collisions.ImplicitConversions.RichInt
import model.entities.Statistic.Statistic
import model.entities.{Entity, MobileEntity}
import model.helpers.WorldUtilities.{checkBodyIsVisible, checkPointCollision, getBodiesDistance, isTargetOnTheRight}

import scala.collection.mutable

class DoNotMove() extends MovementStrategy {
  override def apply(): Unit = { }
}

class PatrolPlatform(val owner: MobileEntity, val level: Level, val stats:mutable.Map[Statistic, Float]) extends MovementStrategy {
  protected val world:World = level.getWorld

  // TODO: move statistics to enemy class
  protected val maxMovementSpeed: Float = 30.PPM // maximum horizontal velocity
  protected val acceleration: Float = 10.PPM // strength of the force applied

  protected var isMovingLeft: Boolean = true

  owner.setFacing(right = !isMovingLeft)

  override def apply(): Unit = {
    val canMoveToTheLeft: Boolean = checkMoveToTheLeft
    val canMoveToTheRight: Boolean = checkMoveToTheRight

    // change direction
    if (!canMoveToTheLeft && isMovingLeft || !canMoveToTheRight && !isMovingLeft)
      owner.getBody.setLinearVelocity(0, owner.getBody.getLinearVelocity.y)

      if (!canMoveToTheLeft && isMovingLeft) isMovingLeft = false
      if (!canMoveToTheRight && !isMovingLeft) isMovingLeft = true

      // update mobile entity direction
      owner.setFacing(right = !isMovingLeft)

    // apply movement to entity's body
    if (canMoveToTheLeft || canMoveToTheRight ) {
      if (isMovingLeft) {
        owner.getBody.applyLinearImpulse(new Vector2(-acceleration, 0), owner.getBody.getWorldCenter, true)
        if (owner.getBody.getLinearVelocity.x <= -maxMovementSpeed)
          owner.getBody.setLinearVelocity(-maxMovementSpeed, owner.getBody.getLinearVelocity.y)
      } else if (!isMovingLeft) {
        owner.getBody.applyLinearImpulse(new Vector2(+acceleration, 0), owner.getBody.getWorldCenter, true)
        if (owner.getBody.getLinearVelocity.x >= maxMovementSpeed)
          owner.getBody.setLinearVelocity(maxMovementSpeed, owner.getBody.getLinearVelocity.y)
      }
    }
  }

  protected def checkMoveToTheLeft: Boolean =
    !checkPointCollision(world, owner.getPosition._1 - owner.getSize._1 - 5.PPM,
      owner.getPosition._2, EntityType.Immobile) &&
      checkPointCollision(world, owner.getPosition._1 - owner.getSize._1 - 5.PPM,
        owner.getPosition._2 - owner.getSize._2 - 5.PPM, EntityType.Immobile)

  protected def checkMoveToTheRight: Boolean =
    !checkPointCollision(world, owner.getPosition._1 + owner.getSize._1 + 5.PPM,
      owner.getPosition._2, EntityType.Immobile) &&
      checkPointCollision(world, owner.getPosition._1 + owner.getSize._1 + 5.PPM,
        owner.getPosition._2 - owner.getSize._2 - 5.PPM, EntityType.Immobile)
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

class PatrolAndStop(override val owner:MobileEntity, override val level: Level, override val stats:mutable.Map[Statistic, Float],
                    val target:Entity => Boolean) extends PatrolPlatform(owner, level, stats) {

  protected val targetEntity: Entity = level.getEntity(target)

  // TODO: move statistics to enemy class
  protected val maxDistance: Float = 40.PPM
  protected val visibilityMaxHorizontalAngle: Float = 30 // defines a vision "cone" originating from the entity

  override def apply(): Unit = {
    if (isHeroNear && (isTargetOnTheRight(owner.getBody, targetEntity.getBody) && !isMovingLeft
      || !isTargetOnTheRight(owner.getBody, targetEntity.getBody) && isMovingLeft)) {
      owner.getBody.setLinearVelocity(0, owner.getBody.getLinearVelocity.y)
    } else {
      super.apply()
    }
  }

  protected def isHeroNear: Boolean = {
    checkBodyIsVisible(world, owner.getBody, targetEntity.getBody, visibilityMaxHorizontalAngle) &&
      getBodiesDistance(owner.getBody, targetEntity.getBody) <= maxDistance
  }
}


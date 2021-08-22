package model.movement

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import model.entities.Entity
import model.helpers.EntitiesBits
import model.helpers.SensorsUtility.{createLowerLeftSensor, createLowerRightSensor, sensorIsIntersectingWith}
import model.helpers.WorldUtilities.{checkBodyIsVisible, checkPointCollision, getBodiesDistance, isTargetOnTheRight}


class DoNotMove() extends MovementStrategy {
  override def move(): Unit = { }

  override def isMoving(): Boolean = false
}

abstract class PatrolPlatform(val entity: Entity, val world: World) extends MovementStrategy {
  // TODO: link to implementation
  // TODO: move statistics to enemy class
  protected val maxMovementSpeed: Float = 3 // maximum horizontal velocity
  protected val acceleration: Float = 1 // strength of the force applied
  protected var lastMovementTime: Long = 0l // last time a force was applied to the body
  protected val movementImpulseFrequency: Float = 5000 // movement impulse frequency

  protected var isMovingLeft: Boolean = true

  override def move(): Unit = {
    val canMoveToTheLeft: Boolean = checkMoveToTheLeft
    val canMoveToTheRight: Boolean = checkMoveToTheRight

    // change direction
    if (!canMoveToTheLeft && isMovingLeft || !canMoveToTheRight && !isMovingLeft)
      entity.getBody.setLinearVelocity(0, entity.getBody.getLinearVelocity.y)

    if (!canMoveToTheLeft && isMovingLeft) isMovingLeft = false
    if (!canMoveToTheRight && !isMovingLeft) isMovingLeft = true

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

  override def isMoving(): Boolean = entity.getBody.getLinearVelocity.x != 0

  protected def checkMoveToTheLeft: Boolean
  protected def checkMoveToTheRight: Boolean
}

class PlatformPatrolWithSensors(override val entity: Entity, override  val world: World)
  extends PatrolPlatform(entity, world) {
  // populate the body with movement specific sensors
  val lowerLeftSensor: Fixture = createLowerLeftSensor(entity.getBody)
  val lowerRightSensor: Fixture = createLowerRightSensor(entity.getBody)

  override protected def checkMoveToTheLeft: Boolean =
    sensorIsIntersectingWith(lowerLeftSensor, EntitiesBits.WORLD_CATEGORY_BIT, world)

  override protected def checkMoveToTheRight: Boolean =
    sensorIsIntersectingWith(lowerRightSensor, EntitiesBits.WORLD_CATEGORY_BIT, world)
}

class PlatformPatrolWithAABBTests(override val entity: Entity, override  val world: World)
  extends PatrolPlatform(entity, world) {

  override protected def checkMoveToTheLeft: Boolean =
    !checkPointCollision(world, entity.getPosition._1 - entity.getSize._1 - 0.5f,
      entity.getPosition._2, EntitiesBits.WORLD_CATEGORY_BIT) &&
      checkPointCollision(world, entity.getPosition._1 - entity.getSize._1 - 0.5f,
        entity.getPosition._2 - entity.getSize._2 - 0.5f, EntitiesBits.WORLD_CATEGORY_BIT)

  override protected def checkMoveToTheRight: Boolean =
    !checkPointCollision(world, entity.getPosition._1 + entity.getSize._1 + 0.5f,
      entity.getPosition._2, EntitiesBits.WORLD_CATEGORY_BIT) &&
      checkPointCollision(world, entity.getPosition._1 + entity.getSize._1 + 0.5f,
        entity.getPosition._2 - entity.getSize._2 - 0.5f, EntitiesBits.WORLD_CATEGORY_BIT)
}

class PatrolAndStopIfNearHero(override val entity:Entity, override val world: World, val targetEntity:Entity) extends PlatformPatrolWithAABBTests(entity, world) {
  // TODO: move statistics to enemy class
  protected val maxDistance: Float = 4
  protected val visibilityMaxHorizontalAngle: Float = 30 // defines a vision "cone" originating from the entity

  override def move(): Unit = {
    if (!isHeroNear) {
      super.move()
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

  override def move(): Unit = {
    if (isHeroNear && (isTargetOnTheRight(entity.getBody, targetEntity.getBody) && !isMovingLeft
      || !isTargetOnTheRight(entity.getBody, targetEntity.getBody) && isMovingLeft)) {
      entity.getBody.setLinearVelocity(0, entity.getBody.getLinearVelocity.y)
    } else {
      super.move()
    }
  }

  protected def isHeroNear: Boolean = {
    checkBodyIsVisible(world, entity.getBody, targetEntity.getBody, visibilityMaxHorizontalAngle) &&
      getBodiesDistance(entity.getBody, targetEntity.getBody) <= maxDistance
  }
}

//class PlatformPatrolRandomly(override val entity: Entity, override val world: World) extends PlatformPatrolWithAABBTests(entity, world)  {
//  val randomGen = new Random(0)
//  var lastRandomChange = System.currentTimeMillis()
//
//  override def move(): Unit = {
//    // randomly change direction every 5 seconds
//    if (System.currentTimeMillis() - lastRandomChange > 5000) {
//      isMovingLeft = randomGen.nextBoolean()
//      lastRandomChange = System.currentTimeMillis()
//    }
//    super.move()
//  }
//}
//
//class PatrolAndStopNearHero(override val entity:Entity, override val world: World, val heroEntity:Entity, val level:Level) extends PlatformPatrolWithCollisionsCheck(entity, world) {
//  override def move(): Unit = super.move()
//
//  override def canMove(): Boolean = {
//    !checkEntityIsVisible(heroEntity, entity, world, level) && super.canMove()
//  }
//}
//
//class ChaseHero(val entity: Entity, val world: World, val heroEntity:Entity) extends MovementStrategy {
//  val linearFieldOfView: Float = 5f
//
//  override def move(): Unit = {
//    if (canMove()) {
//      if (isHeroOnTheFarLeft && !isHeroOnTheNearLeft) {
//        entity.getBody.applyLinearImpulse(scaleForceVector(new Vector2(-5f, 0)), entity.getBody.getWorldCenter, true)
//      } else if (isHeroOnTheFarRight && !isHeroOnTheNearRight) {
//        entity.getBody.applyLinearImpulse(scaleForceVector(new Vector2(5f, 0)), entity.getBody.getWorldCenter, true)
//      }
//    }
//  }
//
//  protected def isHeroOnTheFarLeft: Boolean = checkAABBCollision(world,
//    entity.getPosition._1 - entity.getSize._1 - linearFieldOfView, entity.getPosition._2,
//    entity.getPosition._1 - entity.getSize._1 , entity.getPosition._2,
//    heroEntity)
//
//  protected def isHeroOnTheFarRight: Boolean = checkAABBCollision(world,
//    entity.getPosition._1 + entity.getSize._1, entity.getPosition._2,
//    entity.getPosition._1 + entity.getSize._1 + linearFieldOfView, entity.getPosition._2,
//    heroEntity)
//
//  protected def isHeroOnTheNearLeft: Boolean = checkAABBCollision(world,
//    entity.getPosition._1 - entity.getSize._1 - 2, entity.getPosition._2,
//    entity.getPosition._1 - entity.getSize._1 , entity.getPosition._2,
//    heroEntity)
//
//  protected def isHeroOnTheNearRight: Boolean = checkAABBCollision(world,
//    entity.getPosition._1 + entity.getSize._1, entity.getPosition._2,
//    entity.getPosition._1 + entity.getSize._1 + 2, entity.getPosition._2,
//    heroEntity)
//
//  override def canMove(): Boolean = (isHeroOnTheFarLeft && !isHeroOnTheNearLeft) || (isHeroOnTheFarRight && !isHeroOnTheNearRight)
//}
//
//class AndStopIfFacingHeroPatrolWithCollisionsCheck(override val entity: Entity, override val world: World, val heroEntity: Entity) extends PlatformPatrolWithCollisionsCheck(entity, world)  {
//
//  override def move(): Unit = {
//    if ((!isHeroOnTheNearLeft && isMovingLeft) || (!isHeroOnTheNearRight && !isMovingLeft))
//      super.move()
//  }
//
//  protected def isHeroOnTheNearLeft: Boolean = checkAABBCollision(world,
//    entity.getPosition._1 - entity.getSize._1 - 2, entity.getPosition._2,
//    entity.getPosition._1 - entity.getSize._1 , entity.getPosition._2,
//    heroEntity)
//
//  protected def isHeroOnTheNearRight: Boolean = checkAABBCollision(world,
//    entity.getPosition._1 + entity.getSize._1, entity.getPosition._2,
//    entity.getPosition._1 + entity.getSize._1 + 2, entity.getPosition._2,
//    heroEntity)
//
//  override def canMove(): Boolean = !isHeroOnTheNearLeft && !isHeroOnTheNearRight && super.canMove()
//}


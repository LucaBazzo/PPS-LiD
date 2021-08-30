package model.movement

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import model.Level
import model.collisions.EntityType
import model.collisions.ImplicitConversions.RichInt
import model.entities.Statistic.Statistic
import model.entities.{Entity, MobileEntity, State, Statistic}
import model.helpers.WorldUtilities.{checkBodyIsVisible, checkPointCollision, getBodiesDistance, isTargetOnTheRight}

class DoNotMove() extends MovementStrategy {
  override def apply(): Unit = { }
}

class StopMoving(val owner: MobileEntity) extends MovementStrategy {
  this.owner.getBody.setLinearVelocity(0, 0)
  this.owner.getBody.setGravityScale(0)

  override def apply(): Unit = { }
}

class PatrolPlatform(val owner: MobileEntity,
                     val level: Level,
                     val stats: Map[Statistic, Float]) extends MovementStrategy {

  protected val world:World = level.getWorld
  protected val maxMovementSpeed: Float = stats(Statistic.MaxMovementSpeed)
  protected val acceleration: Float = stats(Statistic.Acceleration)

  protected val patrolSensorsOffset: Float = 5.PPM
  protected var isMovingLeft: Boolean = true

  owner.setFacing(right = !isMovingLeft)

  override def apply(): Unit = {
    val canMoveToTheLeft: Boolean = this.checkMoveToTheLeft
    val canMoveToTheRight: Boolean = this.checkMoveToTheRight
    
    if (!canMoveToTheLeft && isMovingLeft || !canMoveToTheRight && !isMovingLeft) {
      this.stopMoving
      this.changeDirection(canMoveToTheLeft, canMoveToTheRight)
    }

     if (canMoveToTheLeft || canMoveToTheRight ) {
       this.move
       this.owner.setState(State.Running)
     } else
       this.owner.setState(State.Standing)
  }

  protected def checkMoveToTheLeft: Boolean =
    !checkPointCollision(world,
      this.owner.getPosition._1 - this.owner.getSize._1 - this.patrolSensorsOffset,
      this.owner.getPosition._2, EntityType.Immobile) &&
      checkPointCollision(world,
        this.owner.getPosition._1 - this.owner.getSize._1 - this.patrolSensorsOffset,
        this.owner.getPosition._2 - this.owner.getSize._2 - this.patrolSensorsOffset,
        EntityType.Immobile)

  protected def checkMoveToTheRight: Boolean =
    !checkPointCollision(world,
      this.owner.getPosition._1 + this.owner.getSize._1 + this.patrolSensorsOffset,
      this.owner.getPosition._2, EntityType.Immobile) &&
      checkPointCollision(world,
        this.owner.getPosition._1 + this.owner.getSize._1 + this.patrolSensorsOffset,
        this.owner.getPosition._2 - this.owner.getSize._2 - this.patrolSensorsOffset,
        EntityType.Immobile)

  protected def stopMoving: Unit = {
    this.owner.getBody.setLinearVelocity(0, this.owner.getBody.getLinearVelocity.y)
    this.owner.setState(State.Standing)
  }

  protected def changeDirection(canMoveToTheLeft:Boolean, canMoveToTheRight:Boolean) = {
    this.isMovingLeft = !this.isMovingLeft
    this.owner.setFacing(right = !this.isMovingLeft)
  }

  protected def move = {
    if (this.isMovingLeft) {
      this.owner.getBody.applyLinearImpulse(
      new Vector2(-this.acceleration, 0), this.owner.getBody.getWorldCenter, true)

      if (this.owner.getBody.getLinearVelocity.x <= -this.maxMovementSpeed)
        this.owner.getBody.setLinearVelocity(-this.maxMovementSpeed, this.owner.getBody.getLinearVelocity.y)
    } else if (!this.isMovingLeft) {
      this.owner.getBody.applyLinearImpulse(
      new Vector2(+this.acceleration, 0), this.owner.getBody.getWorldCenter, true)

      if (this.owner.getBody.getLinearVelocity.x >= this.maxMovementSpeed)
        this.owner.getBody.setLinearVelocity(this.maxMovementSpeed, this.owner.getBody.getLinearVelocity.y)
   }
  }
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

class PatrolAndStop(override val owner:MobileEntity,
                    override val level: Level,
                    override val stats: Map[Statistic, Float],
                    val target:Entity => Boolean) extends PatrolPlatform(owner, level, stats) {

  protected val targetEntity: Entity = level.getEntity(target)

  protected val maxDistance: Float = stats(Statistic.HorizontalVisionDistance)
  protected val visibilityMaxHorizontalAngle: Float = stats(Statistic.HorizontalVisionAngle)

  protected var targetIsAhead: Boolean = false

  override def apply(): Unit = {
    if (this.isTargetNear && (isTargetOnTheRight(owner.getBody, this.targetEntity.getBody) && !this.isMovingLeft ||
      !isTargetOnTheRight(owner.getBody, this.targetEntity.getBody) && this.isMovingLeft)) {
      this.stopMoving
    } else {
      super.apply()
    }
  }

  protected def isTargetNear: Boolean = {
    checkBodyIsVisible(this.world, owner.getBody, this.targetEntity.getBody, this.visibilityMaxHorizontalAngle) &&
      getBodiesDistance(owner.getBody, this.targetEntity.getBody) <= this.maxDistance
  }
}


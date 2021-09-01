package model.movement

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import model.Level
import model.collisions.EntityCollisionBit
import model.collisions.ImplicitConversions.RichFloat
import model.entities.Statistic.Statistic
import model.entities.{Entity, MobileEntity, State, Statistic}

import model.helpers.WorldUtilities._

class DoNotMove() extends MovementStrategy {
  override def apply(): Unit = { }
}

class StopMoving(val sourceEntity: MobileEntity) extends MovementStrategy {
  this.sourceEntity.getBody.setLinearVelocity(0, 0)
  this.sourceEntity.getBody.setGravityScale(0)

  override def apply(): Unit = { }
}

class PatrolPlatform(val sourceEntity: MobileEntity,
                     val level: Level,
                     val stats: Map[Statistic, Float]) extends MovementStrategy {

  protected val world:World = level.getWorld
  protected val maxMovementSpeed: Float = stats(Statistic.MaxMovementSpeed)
  protected val acceleration: Float = stats(Statistic.Acceleration)

  protected val patrolSensorsOffset: Float = 5.PPM
  protected var isMovingLeft: Boolean = true

  sourceEntity.setFacing(right = !isMovingLeft)

  override def apply(): Unit = {
    val canMoveToTheLeft: Boolean = this.checkMoveToTheLeft
    val canMoveToTheRight: Boolean = this.checkMoveToTheRight

    if (!canMoveToTheLeft && isMovingLeft || !canMoveToTheRight && !isMovingLeft) {
      this.stopMoving
      this.changeDirection(canMoveToTheLeft, canMoveToTheRight)
    }

     if (canMoveToTheLeft || canMoveToTheRight ) {
       this.move
       this.sourceEntity.setState(State.Running)
     } else
       this.sourceEntity.setState(State.Standing)
  }

  protected def checkMoveToTheLeft: Boolean =
    !checkCollision(
      this.sourceEntity.getPosition._1 - this.sourceEntity.getSize._1 / 2 - this.patrolSensorsOffset,
      this.sourceEntity.getPosition._2, EntityCollisionBit.Immobile) &&
      checkCollision(
        this.sourceEntity.getPosition._1 - this.sourceEntity.getSize._1 / 2  - this.patrolSensorsOffset,
        this.sourceEntity.getPosition._2 - this.sourceEntity.getSize._2 / 2  - this.patrolSensorsOffset,
        EntityCollisionBit.Immobile)

  protected def checkMoveToTheRight: Boolean =
    !checkCollision(
      this.sourceEntity.getPosition._1 + this.sourceEntity.getSize._1 / 2  + this.patrolSensorsOffset,
      this.sourceEntity.getPosition._2, EntityCollisionBit.Immobile) &&
      checkCollision(
        this.sourceEntity.getPosition._1 + this.sourceEntity.getSize._1 / 2  + this.patrolSensorsOffset,
        this.sourceEntity.getPosition._2 - this.sourceEntity.getSize._2 / 2  - this.patrolSensorsOffset,
        EntityCollisionBit.Immobile)

  protected def stopMoving: Unit = {
    this.sourceEntity.getBody.setLinearVelocity(0, this.sourceEntity.getBody.getLinearVelocity.y)
    this.sourceEntity.setState(State.Standing)
  }

  protected def changeDirection(canMoveToTheLeft:Boolean, canMoveToTheRight:Boolean) = {
    this.isMovingLeft = !this.isMovingLeft
    this.sourceEntity.setFacing(right = !this.isMovingLeft)
  }

  protected def move = {
    if (this.isMovingLeft) {
      this.sourceEntity.getBody.applyLinearImpulse(
      new Vector2(-this.acceleration, 0), this.sourceEntity.getBody.getWorldCenter, true)

      if (this.sourceEntity.getBody.getLinearVelocity.x <= -this.maxMovementSpeed)
        this.sourceEntity.getBody.setLinearVelocity(-this.maxMovementSpeed, this.sourceEntity.getBody.getLinearVelocity.y)
    } else if (!this.isMovingLeft) {
      this.sourceEntity.getBody.applyLinearImpulse(new Vector2(+this.acceleration, 0), this.sourceEntity.getBody.getWorldCenter, true)

      if (this.sourceEntity.getBody.getLinearVelocity.x >= this.maxMovementSpeed)
        this.sourceEntity.getBody.setLinearVelocity(this.maxMovementSpeed, this.sourceEntity.getBody.getLinearVelocity.y)
   }
  }
}

class PatrolAndStop(override val sourceEntity:MobileEntity,
                    override val level: Level,
                    override val stats: Map[Statistic, Float],
                    val target:Entity => Boolean) extends PatrolPlatform(sourceEntity, level, stats) {

  protected val targetEntity: Entity = level.getEntity(target)

  protected val maxDistance: Float = stats(Statistic.HorizontalVisionDistance)
  protected val visibilityMaxHorizontalAngle: Float = stats(Statistic.HorizontalVisionAngle)

  protected var targetIsAhead: Boolean = false

  override def apply(): Unit = {
    if (this.isTargetNear &&
      (isBodyOnTheRight(sourceEntity.getBody, this.targetEntity.getBody) && !this.isMovingLeft ||
      !isBodyOnTheRight(sourceEntity.getBody, this.targetEntity.getBody) && this.isMovingLeft)) {
      this.stopMoving
    } else {
      super.apply()
    }
  }

  protected def isTargetNear: Boolean = {
    isBodyVisible(sourceEntity.getBody, this.targetEntity.getBody, this.visibilityMaxHorizontalAngle) &&
      getBodiesDistance(sourceEntity.getBody, this.targetEntity.getBody) <= this.maxDistance
  }
}


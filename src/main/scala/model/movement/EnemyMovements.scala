package model.movement

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import model.Level
import model.entities.Statistic.Statistic
import model.entities.{Entity, MobileEntity, State, Statistic}
import model.helpers.EntitiesUtilities.{getEntitiesDistance, isEntityOnTheLeft, isEntityOnTheRight, isEntityVisible, isFloorPresentOnTheLeft, isFloorPresentOnTheRight, isPathClearOnTheLeft, isPathClearOnTheRight}

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

  protected val patrolSensorsOffset: Float = 5
  protected var isMovingLeft: Boolean = false

  sourceEntity.setFacing(right = !isMovingLeft)

  override def apply(): Unit = {

    val canMoveToTheLeft: Boolean = isPathClearOnTheLeft(this.sourceEntity) && isFloorPresentOnTheLeft(this.sourceEntity)
    val canMoveToTheRight: Boolean = isPathClearOnTheRight(this.sourceEntity) && isFloorPresentOnTheRight(this.sourceEntity)
    println(this,
      isPathClearOnTheLeft(this.sourceEntity), isFloorPresentOnTheLeft(this.sourceEntity))
    if (!canMoveToTheLeft && isMovingLeft || !canMoveToTheRight && !isMovingLeft) {
      this.stopMoving()
      this.changeDirection()
    }

     if (canMoveToTheLeft || canMoveToTheRight ) {
       this.move()
       this.sourceEntity.setState(State.Running)
     } else
       this.sourceEntity.setState(State.Standing)
  }

  protected def stopMoving(): Unit = {
    this.sourceEntity.getBody.setLinearVelocity(0, this.sourceEntity.getBody.getLinearVelocity.y)
    this.sourceEntity.setState(State.Standing)
  }

  protected def changeDirection(): Unit = {
    this.isMovingLeft = !this.isMovingLeft
    this.sourceEntity.setFacing(right = !this.isMovingLeft)
  }

  protected def move(): Unit = {
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
    if (this.isTargetNearby &&
      isEntityOnTheRight(sourceEntity, this.targetEntity) && !this.isMovingLeft ||
      isEntityOnTheLeft(sourceEntity, this.targetEntity) && this.isMovingLeft) {
      this.stopMoving()
    } else {
      super.apply()
    }
  }

  protected def isTargetNearby: Boolean = {
    isEntityVisible(sourceEntity, this.targetEntity, this.visibilityMaxHorizontalAngle) &&
      getEntitiesDistance(sourceEntity, this.targetEntity) <= this.maxDistance
  }
}


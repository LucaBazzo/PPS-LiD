package model.movement

import com.badlogic.gdx.math.Vector2
import model.entities.{Entity, MobileEntity, State, Statistic}
import model.helpers.EntitiesUtilities._

class DoNotMove() extends MovementStrategy {
  override def apply(): Unit = { }
}

class PatrolPlatform(val sourceEntity: MobileEntity,
                     val targetEntity: Entity) extends MovementStrategy {

  protected val maxMovementSpeed: Float = sourceEntity.getStatistic(Statistic.MaxMovementSpeed).get
  protected val acceleration: Float = sourceEntity.getStatistic(Statistic.Acceleration).get

  protected var isMovingLeft: Boolean = false

  sourceEntity.setFacing(right = !isMovingLeft)

  override def apply(): Unit = {

    val canMoveToTheLeft: Boolean = !isPathObstructedOnTheLeft(this.sourceEntity) && isFloorPresentOnTheLeft(this.sourceEntity)
    val canMoveToTheRight: Boolean = !isPathObstructedOnTheRight(this.sourceEntity) && isFloorPresentOnTheRight(this.sourceEntity)

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
  }

  protected def changeDirection(): Unit = {
    this.isMovingLeft = !this.isMovingLeft
    this.sourceEntity.setFacing(right = !this.isMovingLeft)
  }

  protected def move(): Unit = {
    // apply movement force
    val movementForce = if (this.isMovingLeft) -this.acceleration else this.acceleration
    this.sourceEntity.getBody.applyLinearImpulse(
      new Vector2(movementForce, 0), this.sourceEntity.getBody.getWorldCenter, true)

    // limit horizontal speed
    if (Math.abs(this.sourceEntity.getBody.getLinearVelocity.x) > this.maxMovementSpeed) {
      val maxMovementForce: Vector2 = new Vector2(0f, this.sourceEntity.getBody.getLinearVelocity.y)
      if (this.sourceEntity.getBody.getLinearVelocity.x > this.maxMovementSpeed)
        maxMovementForce.x = this.maxMovementSpeed
      if (this.sourceEntity.getBody.getLinearVelocity.x < -this.maxMovementSpeed)
        maxMovementForce.x = -this.maxMovementSpeed
      this.sourceEntity.getBody.setLinearVelocity(maxMovementForce)
    }
  }
}

class PatrolAndStop(override val sourceEntity:MobileEntity,
                    override val targetEntity:Entity)
  extends PatrolPlatform(sourceEntity, targetEntity) {

  protected val maxDistance: Float = this.sourceEntity.getStatistic(Statistic.VisionDistance).get
  protected val visibilityMaxHorizontalAngle: Float = this.sourceEntity.getStatistic(Statistic.VisionAngle).get

  protected var lastIsTargetNear: Boolean = this.isTargetNearby

  override def apply(): Unit = {
    // prevents enemy movement if he is already attacking
    if (!Array(State.Attack01, State.Attack02, State.Attack03).contains(this.sourceEntity.getState)) {
      val isTargetNearbyCheck = this.isTargetNearby

      // face the target entity if near
      if (isTargetNearbyCheck &&
        (this.isMovingLeft && isEntityOnTheRight(this.sourceEntity, this.targetEntity) ||
          !this.isMovingLeft && isEntityOnTheLeft(this.sourceEntity, this.targetEntity))) {
        this.changeDirection()
      }

      // stop moving when the target entity is near
      if (isTargetNearbyCheck && !this.lastIsTargetNear) {
        this.stopMoving()
        this.sourceEntity.setState(State.Standing)
      } else if (!isTargetNearbyCheck) {
        super.apply()
      }
      this.lastIsTargetNear = isTargetNearbyCheck
    }
  }

  protected def isTargetNearby: Boolean = {
    isEntityVisible(this.sourceEntity, this.targetEntity, this.visibilityMaxHorizontalAngle) &&
      getEntitiesDistance(this.sourceEntity, this.targetEntity) <= this.maxDistance
  }
}


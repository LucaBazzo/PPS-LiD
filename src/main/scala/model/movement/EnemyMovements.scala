package model.movement

import com.badlogic.gdx.math.Vector2
import model.entities.{Entity, MobileEntity, State, Statistic}
import model.helpers.EntitiesUtilities._

// TODO: replace with MovementStratey.DoNothingMovementStrategy
class DoNotMove() extends MovementStrategy {
  override def apply(): Unit = { }
}

class FaceTarget(val sourceEntity: MobileEntity, val targetEntity: Entity) extends MovementStrategy {

  private var isFacingRight: Boolean = isEntityOnTheRight(sourceEntity, targetEntity)

  override def apply(): Unit = {
    val facingRightCheck = isEntityOnTheRight(sourceEntity, targetEntity)

    // prevents continuous calls to sourceEntity.setFacing
    if (facingRightCheck != isFacingRight) {
      this.isFacingRight = facingRightCheck
      this.sourceEntity.setFacing(facingRightCheck)
    }
  }
}

class PatrolPlatform(val sourceEntity: MobileEntity,
                     val targetEntity: Entity) extends MovementStrategy {

  protected val maxMovementSpeed: Float = sourceEntity.getStatistic(Statistic.MaxMovementSpeed).get
  protected val acceleration: Float = sourceEntity.getStatistic(Statistic.Acceleration).get

  override def apply(): Unit = {

    val canMoveToTheLeft: Boolean = !isPathObstructedOnTheLeft(this.sourceEntity) && isFloorPresentOnTheLeft(this.sourceEntity)
    val canMoveToTheRight: Boolean = !isPathObstructedOnTheRight(this.sourceEntity) && isFloorPresentOnTheRight(this.sourceEntity)

    if (!canMoveToTheLeft && !this.sourceEntity.isFacingRight || !canMoveToTheRight && this.sourceEntity.isFacingRight) {
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
    this.sourceEntity.setFacing(right = !this.sourceEntity.isFacingRight)
  }

  protected def move(): Unit = {
    // apply movement force
    val movementForce = if (!this.sourceEntity.isFacingRight) -this.acceleration else this.acceleration
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

  protected val visionDistance: Float = this.sourceEntity.getStatistic(Statistic.VisionDistance).get
  protected val visionAngle: Float = this.sourceEntity.getStatistic(Statistic.VisionAngle).get

  protected var lastIsTargetNear: Boolean = this.isTargetNearby

  override def apply(): Unit = {
    // prevents enemy movement if he is already attacking
    if (!Array(State.Attack01, State.Attack02, State.Attack03).contains(this.sourceEntity.getState)) {
      val isTargetNearbyCheck = this.isTargetNearby

      // face the target entity if near
      if (isTargetNearbyCheck &&
        (!this.sourceEntity.isFacingRight && isEntityOnTheRight(this.sourceEntity, this.targetEntity) ||
          this.sourceEntity.isFacingRight && isEntityOnTheLeft(this.sourceEntity, this.targetEntity))) {
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
    isEntityVisible(this.sourceEntity, this.targetEntity, this.visionAngle) &&
      getEntitiesDistance(this.sourceEntity, this.targetEntity) <= this.visionDistance
  }
}

class ChaseTarget(val sourceEntity:MobileEntity,
                  val targetEntity:Entity) extends MovementStrategy {

  protected val visionDistance: Float = this.sourceEntity.getStatistic(Statistic.VisionDistance).get
  protected val visionAngle: Float = this.sourceEntity.getStatistic(Statistic.VisionAngle).get
  protected val maxMovementSpeed: Float = sourceEntity.getStatistic(Statistic.MaxMovementSpeed).get
  protected val acceleration: Float = sourceEntity.getStatistic(Statistic.Acceleration).get

  protected var isTargetLeft: Boolean = false
  private var lastMovementCheck: Boolean = false

  override def apply(): Unit = {

    val canMoveToTheLeft: Boolean = !isPathObstructedOnTheLeft(this.sourceEntity) && isFloorPresentOnTheLeft(this.sourceEntity)
    val canMoveToTheRight: Boolean = !isPathObstructedOnTheRight(this.sourceEntity) && isFloorPresentOnTheRight(this.sourceEntity)

    this.isTargetLeft = isEntityOnTheLeft(this.sourceEntity, this.targetEntity)

    val isTargetVisible: Boolean = isEntityVisible(this.sourceEntity, this.targetEntity)

    if (!Array(State.Attack01, State.Attack02, State.Attack03).contains(this.sourceEntity.getState)) {
      if (!canMoveToTheLeft && isTargetLeft ||
        !canMoveToTheRight && !isTargetLeft ||
        !isTargetVisible ||
        getEntitiesDistance(this.sourceEntity, this.targetEntity) < this.visionDistance) {
        if (this.lastMovementCheck)
          this.sourceEntity.setState(State.Standing)
        this.lastMovementCheck = false
      } else {
        this.move()
        if (!this.lastMovementCheck)
          this.sourceEntity.setState(State.Running)
        this.lastMovementCheck = true
      }
      this.sourceEntity.setFacing(right = !isTargetLeft)
    }
  }

  protected def move(): Unit = {
    // apply movement force
    val movementForce = if (this.isTargetLeft) -this.acceleration else this.acceleration
    this.sourceEntity.setVelocityX(this.sourceEntity.getVelocity._1 + movementForce)

    // limit horizontal speed
    if (Math.abs(this.sourceEntity.getVelocity._1) > this.maxMovementSpeed) {
      var maxMovementForce: Float = 0
      if (this.sourceEntity.getBody.getLinearVelocity.x > this.maxMovementSpeed)
        maxMovementForce = this.maxMovementSpeed
      if (this.sourceEntity.getBody.getLinearVelocity.x < -this.maxMovementSpeed)
        maxMovementForce = -this.maxMovementSpeed
      this.sourceEntity.setVelocityX(maxMovementForce)
    }
  }
}

package model.behaviour

import _root_.utils.ApplicationConstants._
import model.attack.AttackStrategy
import model.collisions.ImplicitConversions.entityToBody
import model.entities._
import model.helpers.EntitiesUtilities._
import model.helpers.GeometricUtilities.{getBodiesDistance, isBodyOnTheLeft, isBodyOnTheRight}
import model.helpers.WorldUtilities.isBodyVisible

trait Transition {
  def apply(): Boolean
  def reset(): Unit = { }
}

object RichTransitions {
  implicit def functionToTransition(f:() => Boolean): Transition = () => f.apply()

  implicit class LogicalTransition(p:Transition){
    def ||(x: Transition): Transition = () => p.apply || x.apply
    def &&(x: Transition): Transition = () => p.apply && x.apply
  }
}

case class Not(predicate: Transition) extends Transition {
  override def apply(): Boolean = {
    !predicate.apply()
  }

  override def reset(): Unit = predicate.reset()
}

case class TimePredicate(time:Long) extends Transition {
  var startTime: Long = System.currentTimeMillis()

  override def apply(): Boolean = System.currentTimeMillis() - startTime >= time

  override def reset(): Unit = this.startTime = System.currentTimeMillis()
}

case class HealthThreshold(entity:LivingEntity, percentage:Float) extends Transition {
  val healthThreshold: Float = entity.getStatistic(Statistic.Health).get * this.percentage / 100

  override def apply(): Boolean = this.entity.getLife <= this.healthThreshold
}

case class CompletedAttacks(attackStrategy: AttackStrategy,
                            numAttacks: Int = 1) extends Transition {

  private var finishedAttacksCount: Int = 0
  private var lastAttackFinishedCheck: Boolean = this.attackStrategy.isAttackFinished

  override def apply(): Boolean = {
    if (this.attackStrategy.isAttackFinished && !lastAttackFinishedCheck) {
      finishedAttacksCount += 1
    }

    lastAttackFinishedCheck = this.attackStrategy.isAttackFinished
    finishedAttacksCount >= this.numAttacks
  }

  override def reset(): Unit = {
    this.finishedAttacksCount = 0
    this.lastAttackFinishedCheck = true
  }
}

case class RandomlyTrue(percentage: Float) extends Transition {
  private var lastCheckTime:Long = 0
  private val checkPeriod: Long = 2000
  private var response: Boolean = false

  override def apply(): Boolean = {
    if (this.lastCheckTime == 0) this.lastCheckTime = System.currentTimeMillis()

    val now:Long = System.currentTimeMillis()
    if (now - this.lastCheckTime > this.checkPeriod) {
      this.lastCheckTime = now
      RANDOM.nextFloat() match {
        case x if x < this.percentage => this.response = true
        case _ => this.response = false
      }
    }
    this.response
  }

  override def reset(): Unit = {
    this.response = false
    this.lastCheckTime = 0
  }
}

case class IsEntityAttacking(entity: Entity) extends Transition {
  override def apply(): Boolean = List(State.Attack01, State.Attack02, State.Attack03) contains this.entity.getState
}

case class CanMoveToTheLeft(sourceEntity:Entity) extends Transition {
  override def apply(): Boolean =
    !isPathObstructedOnTheLeft(sourceEntity, vOffset = 0) &&
      isFloorPresentOnTheLeft(sourceEntity, vOffset = 0)
}

case class CanMoveToTheRight(sourceEntity:Entity) extends Transition {
  override def apply(): Boolean =
    !isPathObstructedOnTheRight(sourceEntity, vOffset = 0) &&
      isFloorPresentOnTheRight(sourceEntity, vOffset = 0)
}

case class IsTargetNearby(sourceEntity:Entity,
                          targetEntity:Entity,
                          distance:Float) extends Transition {
  override def apply(): Boolean =
    getBodiesDistance(this.sourceEntity, this.targetEntity) <= distance
}
case class IsTargetVisible(sourceEntity:MobileEntity,
                           targetEntity:Entity) extends Transition {

  override def apply(): Boolean =
    isBodyVisible(this.sourceEntity, this.targetEntity)
}

case class IsPathWalkable(sourceEntity:MobileEntity,
                          targetEntity:Entity) extends Transition {
  override def apply(): Boolean =
    (!isPathObstructedOnTheLeft(this.sourceEntity, vOffset = 0) &&
    isFloorPresentOnTheLeft(this.sourceEntity, vOffset = 0) &&
    isBodyOnTheLeft(this.sourceEntity, this.targetEntity)) ||
    (!isPathObstructedOnTheRight(this.sourceEntity, vOffset = 0) &&
      isFloorPresentOnTheRight(this.sourceEntity, vOffset = 0) &&
      isBodyOnTheRight(this.sourceEntity, this.targetEntity))
}

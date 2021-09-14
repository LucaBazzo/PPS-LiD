package model.behaviour

import model.attack.AttackStrategy
import model.entities.{Entity, LivingEntity, Statistic}
import model.helpers.EntitiesUtilities
import utils.ApplicationConstants.RANDOM

trait Predicate {
  def apply(): Boolean
  def reset(): Unit
}

class TimePredicate(time:Long) extends Predicate {
  var startTime: Long = System.currentTimeMillis()

  override def apply(): Boolean = System.currentTimeMillis() - startTime >= time

  override def reset(): Unit = this.startTime = System.currentTimeMillis()
}

class HealthThresholdPredicate(entity:LivingEntity, percentage:Float) extends Predicate {
  val healthThreshold: Float = entity.getStatistic(Statistic.Health).get * this.percentage / 100

  override def apply(): Boolean = this.entity.getLife <= this.healthThreshold

  override def reset(): Unit = { }
}

class CompletedAttackPredicate(attackStrategy: AttackStrategy,
                               numAttacks: Int = 1) extends Predicate {

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

abstract class DistancePredicate() extends Predicate {

  private var lastCheckResult: Boolean = false
  private var lastCheckTime: Long = 0
  private val TIMER:Long = 1000

  override def apply(): Boolean = {
    if (System.currentTimeMillis() - this.lastCheckTime > TIMER) {
      this.lastCheckTime = System.currentTimeMillis()
      this.lastCheckResult = this.distanceCheck()
    }
    this.lastCheckResult
  }

  override def reset(): Unit = {
    this.lastCheckTime = 0
  }

  def distanceCheck(): Boolean
}

class TargetIsNearPredicate(sourceEntity: Entity,
                            targetEntity:Entity,
                            distance: Float) extends DistancePredicate() {

  override def distanceCheck(): Boolean =
    EntitiesUtilities.getEntitiesDistance(this.sourceEntity, this.targetEntity) <= this.distance
}


class TargetIsFarPredicate(sourceEntity: Entity,
                           targetEntity:Entity,
                           distance: Float) extends DistancePredicate() {

  override def distanceCheck(): Boolean =
    EntitiesUtilities.getEntitiesDistance(this.sourceEntity, this.targetEntity) > this.distance
}

class RandomTruePredicate(val percentage: Float) extends Predicate {
  private var lastCheckTime:Long = 0
  private val checkPeriod: Long = 3000
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

class NotPredicate(val predicate: Predicate) extends Predicate {
  override def apply(): Boolean = {
    !predicate.apply()
  }

  override def reset(): Unit = predicate.reset()
}

abstract class CompositePredicates(val predicates: Seq[Predicate]) extends Predicate {
  override def reset(): Unit = predicates.foreach(p => p.reset())
}



class AnyPredicate(override val predicates: Seq[Predicate]) extends CompositePredicates(predicates) {
  override def apply(): Boolean = {
    predicates.map(p => p.apply()).count(e => e) != 0
  }
}

class AllPredicate(override val predicates: Seq[Predicate]) extends CompositePredicates(predicates) {
  override def apply(): Boolean = {
    predicates.map(p => p.apply()).count(e => e) == predicates.size
  }
}

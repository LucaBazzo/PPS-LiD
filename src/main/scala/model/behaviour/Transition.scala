package model.behaviour

import _root_.utils.ApplicationConstants._
import model.attack.AttackStrategy
import model.entities.{Entity, LivingEntity, Statistic}
import model.helpers.EntitiesUtilities.getEntitiesDistance



/**
 *
 */
trait Transition {
  def apply(): Boolean
  def reset(): Unit = { }
}

/**
 *
 */
object RichTransitions {
  implicit def funcToPredicate(f:() => Boolean): Transition = {
    new Transition {
      override def apply(): Boolean = f.apply()
    }
  }

  /**
   *
   * @param p
   */
  implicit class LogicalTransition(p:Transition){
    def ||(x: Transition): Transition = new Transition {
      override def apply(): Boolean = p.apply || x.apply
    }
    def &&(x: Transition): Transition = new Transition {
      override def apply(): Boolean = p.apply && x.apply
    }
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

case class TargetIsNear(sourceEntity:Entity, targetEntity:Entity, distance:Float) extends Transition {
  override def apply(): Boolean = getEntitiesDistance(this.sourceEntity, this.targetEntity) <= distance
}

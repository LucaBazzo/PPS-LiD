package model

import model.attack.AttackStrategy
import model.collisions.CollisionStrategy
import model.entities.{Entity, LivingEntity, Statistic}
import model.helpers.EntitiesUtilities
import model.movement.MovementStrategy
import utils.ApplicationConstants.RANDOM

trait BehavioursManager {

  type Name
  type Behaviour

  def addBehaviour(state:Name, behaviour:Behaviour): Unit

  def addTransition(state:Name, nextState:Name, predicate:Predicate): Unit

  def apply(): Unit
}

abstract class BehavioursManagerImpl()
  extends BehavioursManager {

  protected val behaviours:Map[Name, Behaviour] = Map.empty

  protected var transitions:Map[(Name, Name), Predicate] = Map.empty

  protected var currentBehaviour:Option[Name] = None

  override def addBehaviour(state:Name, behaviour: Behaviour): Unit = {
    if (this.behaviours.getOrElse(state)) {
      // TODO: aggiungere il messaggio all'eccezione
      throw new IllegalArgumentException()
    } else
      this.behaviours(state) = behaviour

    // set first behaviour automatically
    if (this.behaviours.size == 1) {
      this.currentBehaviour = Option(state, behaviour)
    }
  }

  override def addTransition(state: Name, nextState: Name, predicate: Predicate): Unit =
    if (!this.behaviours.map(b => b._1).exists(n => n.equals(state)) ||
      !this.behaviours.map(b => b._1).exists(n => n.equals(nextBehaviour))) {
      throw new IllegalArgumentException()
    } else {
      transitions = (behaviour, nextBehaviour, predicate) :: transitions
    }
  }

  override def apply(): Unit = ???
}

trait EnemyBehavioursManager extends BehavioursManagerImpl {
  type Behaviour = (CollisionStrategy, MovementStrategy, AttackStrategy)
  type Name = String

  def getAttackStrategy: AttackStrategy
  def getMovementStrategy: MovementStrategy
  def getCollisionStrategy: CollisionStrategy
}

trait MovementBehavioursManager extends BehavioursManager {
  type Behaviour = MovementStrategy
  type Name = String

  def getMovementStrategy: MovementStrategy
}

class EnemyBehaviourManager()
  extends EnemyBehavioursManager {

  protected var behaviours:List[(Name, Behaviour)] = List()
  protected var transitions:List[(Name, Name, Predicate)] = List.empty

  protected var currentBehaviour:Option[(Name, Behaviour)] = None

  override def addBehaviour(state:Name, behaviour: (CollisionStrategy, MovementStrategy, AttackStrategy)): Unit = {
    if (this.behaviours.map(b => b._1).exists(n => n equals name))
      throw new IllegalArgumentException()
    else
      this.behaviours = (name, behaviour) :: this.behaviours

    if (this.behaviours.size == 1) {
      this.currentBehaviour = Option(name, behaviour)
    }
  }

  override def addTransition(behaviour:String, nextBehaviour:String, predicate:Predicate): Unit = {
    if (!this.behaviours.map(b => b._1).exists(n => n.equals(behaviour)) ||
      !this.behaviours.map(b => b._1).exists(n => n.equals(nextBehaviour))) {
      throw new IllegalArgumentException()
    } else {
      transitions = (behaviour, nextBehaviour, predicate) :: transitions
    }
  }

  override def apply(): Unit = {
    if (currentBehaviour.isDefined) {
      val activeTransitions: List[(String, String, Predicate)] =
        this.transitions.filter(t => t._1.equals(this.currentBehaviour.get._1) && t._3.apply())

      if (activeTransitions.nonEmpty) {
        val pickedTransition = activeTransitions(RANDOM.nextInt(activeTransitions.length))

        this.resetBehaviour()
        this.currentBehaviour = Option(this.behaviours.find(b => b._1.equals(pickedTransition._2)).get)
      }
    }
  }

  protected def resetBehaviour(): Unit = {
    this.transitions.filter(t => t._1.equals(this.currentBehaviour.get._1)).foreach(t => t._3.reset())
    this.currentBehaviour.get._3.stopMovement()
    this.currentBehaviour.get._4.stopAttack()
  }

  override def getAttackStrategy: AttackStrategy = this.currentBehaviour.get._4

  override def getMovementStrategy: MovementStrategy = this.currentBehaviour.get._3

  override def getCollisionStrategy: CollisionStrategy = this.currentBehaviour.get._2
}

// TODO: convertire in funzioni higher order?

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

abstract class AggregatedPredicate(val predicates: Seq[Predicate]) extends Predicate {
  override def reset(): Unit = predicates.foreach(p => p.reset())
}

class AnyPredicate(override val predicates: Seq[Predicate]) extends AggregatedPredicate(predicates) {
  override def apply(): Boolean = {
    predicates.map(p => p.apply()).count(e => e) != 0
  }
}

class AllPredicate(override val predicates: Seq[Predicate]) extends AggregatedPredicate(predicates) {
  override def apply(): Boolean = {
    predicates.map(p => p.apply()).count(e => e) == predicates.size
  }
}

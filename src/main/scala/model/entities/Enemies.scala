package model.entities

import model.attack.AttackStrategy
import model.collisions.CollisionStrategy
import model.entities.EntityType.EntityType
import model.entities.Statistic.Statistic
import model.movement.MovementStrategy
import model.{EntityBody, Score}

import scala.util.Random

trait EnemyInterface {
  // TODO: rifattorizzare a livello di living entity?
  def setBehaviour(enemyBehaviour: EnemyBehaviour): Unit
}

class Enemy(private val entityType: EntityType,
            private var entityBody: EntityBody,
            private val size: (Float, Float),
            private val stats: Map[Statistic, Float],
            private val score: Int = 100) extends LivingEntityImpl(entityType, entityBody, size, stats)
          with LivingEntity with Score with EnemyInterface {

  var enemyBehaviour:EnemyBehaviour = null

  override def getScore: Int = this.score

  override def update(): Unit = {
    super.update()
    if (state != State.Dying) {
      enemyBehaviour.apply()
    }
  }

  override def setBehaviour(enemyBehaviour: EnemyBehaviour): Unit = this.enemyBehaviour = enemyBehaviour

  override def sufferDamage(damage: Float): Unit = {
    super.sufferDamage(damage)
    if (this.getStatistic(Statistic.CurrentHealth) <= 0) {
      this.state = State.Dying
    }

    // TODO: spostare qui la creazione di items
  }
}

// TODO: usare movement e attack strategy factory per agevolare la creazione degli strategy

trait EnemyBehaviour {

  def addBehaviour(name:String, collisionStrategy: CollisionStrategy, movementStrategy: MovementStrategy, attackStrategy: AttackStrategy): Unit

  def addTransition(behaviour:String, nextBehaviour:String, predicate:Predicate, forced:Boolean = false): Unit

  def apply(): Unit

}

class EnemyBehaviourImpl(protected val sourceEntity:LivingEntity,
                         protected val name:String,
                         protected val collisionStrategy: CollisionStrategy,
                         protected val movementStrategy: MovementStrategy,
                         protected val attackStrategy: AttackStrategy)
  extends EnemyBehaviour {

  protected var behaviours:List[(String, CollisionStrategy, MovementStrategy, AttackStrategy)] = List((name, collisionStrategy, movementStrategy, attackStrategy))
  protected var transitions:List[(String, String, Predicate, Boolean)] = List.empty

  protected var currentBehaviour:(String, CollisionStrategy, MovementStrategy, AttackStrategy) = behaviours.head

  protected val randomGenerator = new Random()

  override def addBehaviour(name:String, collisionStrategy: CollisionStrategy, movementStrategy: MovementStrategy, attackStrategy: AttackStrategy): Unit = {
    if (this.behaviours.map(b => b._1).exists(n => n equals name))
      throw new IllegalArgumentException()
    else
      this.behaviours = (name, collisionStrategy, movementStrategy, attackStrategy) :: this.behaviours
  }

  override def addTransition(behaviour:String, nextBehaviour:String, predicate:Predicate, forced:Boolean = false): Unit = {
    if (!this.behaviours.map(b => b._1).exists(n => n.equals(behaviour)) ||
      !this.behaviours.map(b => b._1).exists(n => n.equals(nextBehaviour))) {
      throw new IllegalArgumentException()
    } else {
      transitions = (behaviour, nextBehaviour, predicate, forced) :: transitions
    }
  }

  override def apply(): Unit = {

    val activeTransitions:List[(String, String, Predicate, Boolean)] =
      this.transitions.filter(t => t._1 == this.currentBehaviour._1 && t._3.apply())

    if (activeTransitions.nonEmpty) {
      // reset current state allowing reuse of cyclic behaviours
//      this.currentBehaviour._1.reset()
//      this.currentBehaviour._2.reset()
//      this.currentBehaviour._3.reset()
//      this.currentBehaviour._4.reset()


      val forcedTransitions = activeTransitions.filter(t => t._4)
      if (forcedTransitions.size > 1) {
        throw new IllegalArgumentException()
      } else if (forcedTransitions.size == 1) {
        this.currentBehaviour = this.behaviours.find(b => b._1.equals(forcedTransitions.head._2)).get
      } else {
        val unforcedTransitions = activeTransitions.filterNot(t => t._4)
        val temp = unforcedTransitions(randomGenerator.nextInt(unforcedTransitions.length))
        this.currentBehaviour = this.behaviours.find(b => b._1.equals(temp._2)).get
      }
    }

    this.currentBehaviour._3.apply()
    this.currentBehaviour._4.apply()
  }
}

// TODO: convertire in funzioni higher order

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
  val healthThreshold = entity.getStatistic(Statistic.Health) * this.percentage / 100

  override def apply(): Boolean = this.entity.getLife <= this.healthThreshold

  override def reset(): Unit = { }
}

class CompletedAttackPredicate (attackStrategy: AttackStrategy,
                                numAttacks: Int = 1) extends Predicate {

  private var finishedAttacksCount: Int = 0
  private var lastAttackFinishedCheck: Boolean = false

  override def apply(): Boolean = {
    if (this.attackStrategy.isAttackFinished && !lastAttackFinishedCheck) {
      finishedAttacksCount += 1
      println("detected a completed attack")
      lastAttackFinishedCheck = true
    }

    lastAttackFinishedCheck = this.attackStrategy.isAttackFinished
    finishedAttacksCount >= this.numAttacks
  }

  override def reset(): Unit = {
    this.finishedAttacksCount = 0
    this.lastAttackFinishedCheck = true
  }
}

abstract class AggregatedPredicate(val predicates: Seq[Predicate]) extends Predicate {
  override def reset(): Unit = predicates.foreach(p => p.reset())
}

class AnyPredicate(override val predicates: Seq[Predicate]) extends AggregatedPredicate(predicates) {
  override def apply(): Boolean = {
    var output:Boolean = false
    for (predicate <- predicates) {
      if (predicate.apply()) {
        output = true
      }
    }
    output
  }
}

class AllPredicate(override val predicates: Seq[Predicate]) extends AggregatedPredicate(predicates) {
  override def apply(): Boolean = {
    var count:Int = 0
    for (predicate <- predicates) {
      if (predicate.apply()) count += 1
    }
    count == predicates.size
  }
}


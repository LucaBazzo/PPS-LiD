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

  var enemyBehaviour:Option[EnemyBehaviour] = None

  override def getScore: Int = this.score

  override def update(): Unit = {
    super.update()
    if (state != State.Dying && enemyBehaviour.isDefined) {
      enemyBehaviour.get.apply()
    }
  }

  override def setBehaviour(enemyBehaviour: EnemyBehaviour): Unit = this.enemyBehaviour = Option(enemyBehaviour)

  override def sufferDamage(damage: Float): Unit = {
    super.sufferDamage(damage)
    if (this.getStatistic(Statistic.CurrentHealth).get <= 0) {
      this.state = State.Dying
    }

    // TODO: spostare qui la creazione di items
  }
}

// TODO: usare movement e attack strategy factory per agevolare la creazione degli strategy

trait EnemyBehaviour {

  def addBehaviour(name:String, collisionStrategy: CollisionStrategy, movementStrategy: MovementStrategy, attackStrategy: AttackStrategy): Unit

  def addTransition(behaviour:String, nextBehaviour:String, predicate:Predicate): Unit

  def apply(): Unit

}

class EnemyBehaviourImpl(protected val sourceEntity:LivingEntity)
  extends EnemyBehaviour {

  protected var behaviours:List[(String, CollisionStrategy, MovementStrategy, AttackStrategy)] = List()
  protected var transitions:List[(String, String, Predicate)] = List.empty

  protected var currentBehaviour:Option[(String, CollisionStrategy, MovementStrategy, AttackStrategy)] = None

  protected val randomGenerator = new Random()

  override def addBehaviour(name:String, collisionStrategy: CollisionStrategy, movementStrategy: MovementStrategy, attackStrategy: AttackStrategy): Unit = {
    if (this.behaviours.map(b => b._1).exists(n => n equals name))
      throw new IllegalArgumentException()
    else
      this.behaviours = (name, collisionStrategy, movementStrategy, attackStrategy) :: this.behaviours

    if (this.behaviours.size == 1)
      this.currentBehaviour = Option(name, collisionStrategy, movementStrategy, attackStrategy)
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
        println(currentBehaviour.get._1, activeTransitions.map(t => t._1), activeTransitions.map(t => t._2))
        // TODO: implementare?
        // reset current state allowing reuse of cyclic behaviours
        //      this.currentBehaviour._1.reset()
        //      this.currentBehaviour._2.reset()
        //      this.currentBehaviour._3.reset()
        //      this.currentBehaviour._4.reset()

        val pickedTransition = activeTransitions(randomGenerator.nextInt(activeTransitions.length))

        this.resetBehaviour()
        this.currentBehaviour = Option(this.behaviours.find(b => b._1.equals(pickedTransition._2)).get)
      }
      this.currentBehaviour.get._3.apply()
      this.currentBehaviour.get._4.apply()
    }
  }

  protected def resetBehaviour(): Unit = {
    this.transitions.filter(t => t._1 == this.currentBehaviour.get._1).foreach(t => t._3.reset())
    this.currentBehaviour.get._4.stopAttack()
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
      println("detected a completed attack")
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


package model.entities

import model.attack.AttackStrategy
import model.collisions.{CollisionStrategy, EntityCollisionBit}
import model.entities.EntityType.EntityType
import model.entities.Statistic.Statistic
import model.helpers.{EntitiesFactoryImpl, EntitiesUtilities}
import model.movement.MovementStrategy
import model.{EntityBody, Score}
import utils.ApplicationConstants.RANDOM
import utils.EnemiesConstants.{ENEMY_BOSS_TYPES, ENEMY_TYPES}
import model.collisions.ImplicitConversions._
import model.entities.Items.Items

trait Enemy {
  // TODO: rifattorizzare a livello di living entity?
  def setBehaviour(enemyBehaviour: EnemyBehaviour): Unit
}

class EnemyImpl(private val entityType: EntityType,
                private var entityBody: EntityBody,
                private val size: (Float, Float),
                private val stats: Map[Statistic, Float],
                private val statsModifiers: Map[Statistic, Float],
                private val levelNumber: Int,
                private val score: Int = 100,
                private val heroEntity: Hero) extends LivingEntityImpl(entityType, entityBody, size, stats)
          with LivingEntity with Score with Enemy {

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
    if (this.getStatistic(Statistic.CurrentHealth).get <= 0) {
      this.state = State.Dying
      this.enemyBehaviour.get.getAttackStrategy.stopAttack()
    }
    super.sufferDamage(damage)
  }

  override def destroyEntity(): Unit = {
    super.destroyEntity()

    if (ENEMY_TYPES.contains(this.entityType)) {
      EntitiesFactoryImpl.createItem(ItemPools.Enemy_Drops,
        position=(this.getPosition._1, this.getPosition._2).MPP,
        collisions = EntityCollisionBit.Hero)
    }

    if (ENEMY_BOSS_TYPES.contains(this.getType)) {
      if (this.heroEntity.getItemsPicked.contains((i:Items) => i == Items.Bow)) {
        // TODO: cambiare ItemPools.Enemy_Drops con la giusta item pool
        EntitiesFactoryImpl.createItem(ItemPools.Enemy_Drops,
          position=(this.getPosition._1, this.getPosition._2).MPP,
          collisions = EntityCollisionBit.Hero)
      } else
        EntitiesFactoryImpl.createItem(ItemPools.Boss,
          position=(this.getPosition._1, this.getPosition._2).MPP,
          collisions = EntityCollisionBit.Hero)
    }
  }

  override def getStatistic(statistic: Statistic): Option[Float] = {
    val value:Option[Float] = super.getStatistic(statistic)
    if (this.statsModifiers.contains(statistic))
        Option(value.get + this.statsModifiers(statistic)*(this.levelNumber-1))
    else
      value
  }

  override def getStatistics: Map[Statistic, Float] = {
    this.stats.map {case (key, value) => (key, value + this.statsModifiers.getOrElse(key, 0f))}
  }
}

// TODO: usare movement e attack strategy factory per agevolare la creazione degli strategy

trait EnemyBehaviour {

  def addBehaviour(name:String, collisionStrategy: CollisionStrategy, movementStrategy: MovementStrategy, attackStrategy: AttackStrategy): Unit

  def addTransition(behaviour:String, nextBehaviour:String, predicate:Predicate): Unit

  def apply(): Unit

  def getAttackStrategy: AttackStrategy
  def getMovementStrategy: MovementStrategy
}

class EnemyBehaviourImpl(protected val sourceEntity:LivingEntity)
  extends EnemyBehaviour {

  protected var behaviours:List[(String, CollisionStrategy, MovementStrategy, AttackStrategy)] = List()
  protected var transitions:List[(String, String, Predicate)] = List.empty

  protected var currentBehaviour:Option[(String, CollisionStrategy, MovementStrategy, AttackStrategy)] = None

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

        val pickedTransition = activeTransitions(RANDOM.nextInt(activeTransitions.length))

        this.resetBehaviour()
        this.currentBehaviour = Option(this.behaviours.find(b => b._1.equals(pickedTransition._2)).get)
        this.sourceEntity.setMovementStrategy(this.currentBehaviour.get._3)
        this.sourceEntity.setAttackStrategy(this.currentBehaviour.get._4)
      }
      this.currentBehaviour.get._3.apply()
      this.currentBehaviour.get._4.apply()
    }
  }

  protected def resetBehaviour(): Unit = {
    this.transitions.filter(t => t._1 == this.currentBehaviour.get._1).foreach(t => t._3.reset())
    this.currentBehaviour.get._3.stopMovement()
    this.currentBehaviour.get._4.stopAttack()
  }

  override def getAttackStrategy: AttackStrategy = this.currentBehaviour.get._4

  override def getMovementStrategy: MovementStrategy = this.currentBehaviour.get._3
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

abstract class DistancePredicate(sourceEntity: Entity,
                                 targetEntity: Entity,
                                 distance: Float) extends Predicate {

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
                   distance: Float) extends DistancePredicate(sourceEntity, targetEntity, distance) {

  override def distanceCheck(): Boolean =
    EntitiesUtilities.getEntitiesDistance(this.sourceEntity, this.targetEntity) <= this.distance
}


class TargetIsFarPredicate(sourceEntity: Entity,
                  targetEntity:Entity,
                  distance: Float) extends DistancePredicate(sourceEntity, targetEntity, distance) {

  override def distanceCheck(): Boolean =
    EntitiesUtilities.getEntitiesDistance(this.sourceEntity, this.targetEntity) > this.distance
}

class RandomTruePredicate(val percentage: Float) extends Predicate {
  private var lastCheckTime:Long = System.currentTimeMillis()
  private val checkPeriod: Long = 3000
  private var response: Boolean = false

  override def apply(): Boolean = {
    if (System.currentTimeMillis() - this.lastCheckTime > this.checkPeriod) {
      this.lastCheckTime = System.currentTimeMillis()
      RANDOM.nextFloat() match {
        case x if x < this.percentage => this.response = true
        case _ => this.response = false
      }
    }
    response
  }

  override def reset(): Unit = {
    this.response = false
    this.lastCheckTime = System.currentTimeMillis()
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

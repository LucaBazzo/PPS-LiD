package model.behaviour

import model.attack.AttackStrategy
import model.collisions.CollisionStrategy
import model.movement.MovementStrategy
import utils.ApplicationConstants.RANDOM

trait Behaviours {

  type Behaviour

  def addBehaviour(behaviour:Behaviour): Behaviour

  def addTransition(state:Behaviour, nextState:Behaviour, predicate:Predicate): Unit

  def update: Unit

  def getCurrentBehaviour: Behaviour

  def getCurrentTransitions: Map[(Behaviour, Behaviour), Predicate]
}

abstract class BehavioursImpl() extends Behaviours {

  protected var behaviours: List[Behaviour] = List.empty

  protected var transitions: Map[(Behaviour, Behaviour), Predicate] = Map.empty

  protected var currentBehaviour:Option[Behaviour] = None

  override def addBehaviour(behaviour: Behaviour): Behaviour = {
    this.behaviours = behaviour :: this.behaviours

    // set first behaviour automatically
    if (this.behaviours.size == 1) this.currentBehaviour = Option(behaviour)

    behaviour
  }

  override def addTransition(behaviour: Behaviour, nextBehaviour: Behaviour, predicate: Predicate): Unit =
    if (!this.behaviours.contains(behaviour) || !this.behaviours.contains(nextBehaviour)) {
      throw new IllegalArgumentException()
    } else {
      this.transitions += (behaviour, nextBehaviour) -> predicate
    }

  override def update: Unit = {
    if (currentBehaviour.isDefined) {

      val temp = this.getCurrentTransitions
      val activeTransitions: Map[(Behaviour, Behaviour), Predicate] =
        temp.filter(t => t._2.apply())

      if (activeTransitions.nonEmpty) {
        val pickedTransition: ((Behaviour, Behaviour), Predicate) =
          activeTransitions.toList(RANDOM.nextInt(activeTransitions.size))
        val picketBehaviour = this.behaviours.find(b => b equals pickedTransition._1._2).get

        // reset the current behaviour transitions to enable reuse of recurring behaviours
        this.onBehaviourEnd()
        this.getCurrentTransitions.foreach(t => t._2.reset())

        this.currentBehaviour = Option(picketBehaviour)
        this.onBehaviourBegin()
      }
    }
  }

  override def getCurrentBehaviour: Behaviour =
    this.currentBehaviour.getOrElse(throw new IllegalArgumentException())

  override def getCurrentTransitions: Map[(Behaviour, Behaviour), Predicate] =
    this.transitions.filter(t => t._1._1 equals this.getCurrentBehaviour)

  def onBehaviourBegin(): Unit

  def onBehaviourEnd(): Unit
}

trait EnemyBehaviours extends BehavioursImpl {
  override type Behaviour = (CollisionStrategy, MovementStrategy, AttackStrategy)

  def getCollisionStrategy: CollisionStrategy
  def getMovementStrategy: MovementStrategy
  def getAttackStrategy: AttackStrategy
}

class EnemyBehavioursImpl extends EnemyBehaviours {
  override def getCollisionStrategy: CollisionStrategy = this.getCurrentBehaviour._1

  override def getMovementStrategy: MovementStrategy = this.getCurrentBehaviour._2

  override def getAttackStrategy: AttackStrategy = this.getCurrentBehaviour._3

  override def onBehaviourBegin(): Unit = { }

  override def onBehaviourEnd(): Unit = { }
}

trait MovementBehaviours extends BehavioursImpl {
  override type Behaviour = MovementStrategy

  def getMovementStrategy: MovementStrategy
}

class MovementBehavioursImpl extends MovementBehaviours {
  override type Behaviour = MovementStrategy

  override def getMovementStrategy: MovementStrategy = this.getCurrentBehaviour

  override def onBehaviourBegin(): Unit = this.getMovementStrategy.onBegin()

  override def onBehaviourEnd(): Unit = this.getMovementStrategy.onEnd()
}




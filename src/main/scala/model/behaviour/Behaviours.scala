package model.behaviour

import model.attack.AttackStrategy
import model.collisions.CollisionStrategy
import model.movement.MovementStrategy
import utils.ApplicationConstants.RANDOM

trait Behaviours {

  type Name
  type Behaviour

  def addBehaviour(state:Name, behaviour:Behaviour): Unit

  def addTransition(state:Name, nextState:Name, predicate:Predicate): Unit

  def update(): Unit
}

abstract class BehavioursImpl()
  extends Behaviours {

  protected var behaviours: Map[Name, Behaviour] = Map.empty

  protected var transitions: Map[(Name, Name), Predicate] = Map.empty

  protected var currentBehaviourName:Option[Name] = None

  override def addBehaviour(state:Name, behaviour: Behaviour): Unit = {
    if (this.behaviours.contains(state)) {
      throw new IllegalArgumentException()
    } else {
      this.behaviours += state -> behaviour
    }

    // set first behaviour automatically
    if (this.behaviours.size == 1) this.currentBehaviourName = Option(state)
  }

  override def addTransition(state: Name, nextState: Name, predicate: Predicate): Unit =
    if (!this.behaviours.contains(state) || !this.behaviours.contains(nextState)) {
      throw new IllegalArgumentException()
    } else {
      this.transitions += (state, nextState) -> predicate
    }

  override def update(): Unit = {
    if (currentBehaviourName.isDefined) {
      val activeTransitions: Map[(Name, Name), Predicate] =
        this.getCurrentTransitions.filter(t => t._2.apply())

      if (activeTransitions.nonEmpty) {
        val pickedTransition: ((Name, Name), Predicate) =
          activeTransitions.toList(RANDOM.nextInt(activeTransitions.size))
        val picketBehaviour = this.behaviours.find(b => b._1 equals pickedTransition._1._2).get

        // reset the current behaviour transitions to enable reuse of recurring behaviours
        this.getCurrentTransitions.foreach(t => t._2.reset())
        this.resetBehaviour()

        this.currentBehaviourName = Option(picketBehaviour._1)
      }
    }
  }

  def resetBehaviour(): Unit

  def getCurrentBehaviour: Behaviour = this.behaviours(this.currentBehaviourName.get)

  def getCurrentTransitions: Map[(Name, Name), Predicate] =
    this.transitions.filter(t => t._1._1 equals this.currentBehaviourName.get)
}

trait EnemyBehaviours extends BehavioursImpl {
  override type Name = String
  override type Behaviour = (CollisionStrategy, MovementStrategy, AttackStrategy)

  def getCollisionStrategy: CollisionStrategy
  def getMovementStrategy: MovementStrategy
  def getAttackStrategy: AttackStrategy
}

class EnemyBehavioursImpl extends EnemyBehaviours {
  override def resetBehaviour(): Unit = {
  }

  override def getCollisionStrategy: CollisionStrategy = this.getCurrentBehaviour._1

  override def getMovementStrategy: MovementStrategy = this.getCurrentBehaviour._2

  override def getAttackStrategy: AttackStrategy = this.getCurrentBehaviour._3
}




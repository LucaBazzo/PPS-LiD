package model.behaviour

import utils.ApplicationConstants.RANDOM

trait Behaviours {

  type Behaviour

  def addBehaviour(behaviour:Behaviour): Behaviour

  def addTransition(state:Behaviour, nextState:Behaviour, transition:Transition): Unit

  def update(): Unit

  def getCurrentBehaviour: Behaviour

  def getCurrentTransitions: Map[(Behaviour, Behaviour), Transition]
}

abstract class BehavioursImpl() extends Behaviours {

  protected var behaviours: List[Behaviour] = List.empty

  protected var transitions: Map[(Behaviour, Behaviour), Transition] = Map.empty

  protected var currentBehaviour:Option[Behaviour] = None

  override def addBehaviour(behaviour: Behaviour): Behaviour = {
    this.behaviours = behaviour :: this.behaviours

    // set first behaviour automatically
    if (this.behaviours.size == 1) this.currentBehaviour = Option(behaviour)

    behaviour
  }

  override def addTransition(behaviour: Behaviour, nextBehaviour: Behaviour, transition: Transition): Unit =
    this.transitions += (behaviour, nextBehaviour) -> transition

  override def update(): Unit = {
    if (currentBehaviour.isDefined) {

      val activeTransitions: Map[(Behaviour, Behaviour), Transition] =
        this.getCurrentTransitions.filter(t => t._2.apply())

      if (activeTransitions.nonEmpty) {
        val pickedTransition: ((Behaviour, Behaviour), Transition) =
          activeTransitions.toList(RANDOM.nextInt(activeTransitions.size))
        val pickedBehaviour = this.behaviours.find(b => b == pickedTransition._1._2).get

        this.onBehaviourEnd()
        // reset the current behaviour transitions to enable reuse of recurring behaviours
        this.getCurrentTransitions.foreach(t => t._2.reset())

        this.currentBehaviour = Option(pickedBehaviour)
        this.onBehaviourBegin()
      }
    }
  }

  override def getCurrentBehaviour: Behaviour =
    this.currentBehaviour.getOrElse(throw new IllegalArgumentException())

  override def getCurrentTransitions: Map[(Behaviour, Behaviour), Transition] =
    this.transitions.filter(t => t._1._1 == this.getCurrentBehaviour)

  def onBehaviourBegin(): Unit

  def onBehaviourEnd(): Unit
}




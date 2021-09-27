package model.behaviour

import utils.ApplicationConstants.RANDOM

trait StateManager {

  type State

  def addBehaviour(behaviour:State): State

  def addTransition(state:State, nextState:State, transition:Transition): Unit

  def update(): Unit

  def getCurrentBehaviour: State

  def getCurrentTransitions: Map[(State, State), Transition]
}

abstract class StateManagerImpl extends StateManager {

  protected var behaviours: List[State] = List.empty

  protected var transitions: Map[(State, State), Transition] = Map.empty

  protected var currentBehaviour:Option[State] = None

  override def addBehaviour(behaviour: State): State = {
    this.behaviours = behaviour :: this.behaviours

    // set first behaviour automatically
    if (this.behaviours.size == 1) this.currentBehaviour = Option(behaviour)

    behaviour
  }

  override def addTransition(behaviour: State, nextBehaviour: State, transition: Transition): Unit =
    this.transitions += (behaviour, nextBehaviour) -> transition

  override def update(): Unit = {
    if (currentBehaviour.isDefined) {

      val activeTransitions: Map[(State, State), Transition] =
        this.getCurrentTransitions.filter(t => t._2.apply())

      if (activeTransitions.nonEmpty) {
        val pickedTransition: ((State, State), Transition) =
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

  override def getCurrentBehaviour: State =
    this.currentBehaviour.getOrElse(throw new IllegalArgumentException())

  override def getCurrentTransitions: Map[(State, State), Transition] =
    this.transitions.filter(t => t._1._1 == this.getCurrentBehaviour)

  def onBehaviourBegin(): Unit

  def onBehaviourEnd(): Unit
}




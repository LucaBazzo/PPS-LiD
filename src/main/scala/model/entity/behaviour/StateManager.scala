package model.entity.behaviour

import utils.ApplicationConstants.RANDOM

trait StateManager {

  type State

  def addState(state:State): State

  def addTransition(state:State, nextState:State, transition:Transition): Unit

  def update(): Unit

  def getCurrentState: State

  def getCurrentTransitions: Map[(State, State), Transition]
}

abstract class StateManagerImpl extends StateManager {

  protected var states: List[State] = List.empty

  protected var transitions: Map[(State, State), Transition] = Map.empty

  protected var currentState:Option[State] = None

  override def addState(state: State): State = {
    this.states = state :: this.states

    // set first behaviour automatically
    if (this.states.size == 1) this.currentState = Option(state)

    state
  }

  override def addTransition(state: State, nextState: State, transition: Transition): Unit =
    this.transitions += (state, nextState) -> transition

  override def update(): Unit = {
    if (currentState.isDefined) {

      val activeTransitions: Map[(State, State), Transition] =
        this.getCurrentTransitions.filter(t => t._2.apply())

      if (activeTransitions.nonEmpty) {
        val pickedTransition: ((State, State), Transition) =
          activeTransitions.toList(RANDOM.nextInt(activeTransitions.size))
        val pickedBehaviour = this.states.find(b => b == pickedTransition._1._2).get

        this.onStateEnd()
        // reset the current behaviour transitions to enable reuse of recurring behaviours
        this.getCurrentTransitions.foreach(t => t._2.reset())

        this.currentState = Option(pickedBehaviour)
        this.onStateBegin()
      }
    }
  }

  override def getCurrentState: State =
    this.currentState.getOrElse(throw new IllegalArgumentException())

  override def getCurrentTransitions: Map[(State, State), Transition] =
    this.transitions.filter(t => t._1._1 == this.getCurrentState)

  def onStateBegin(): Unit

  def onStateEnd(): Unit
}




package model.entity.behaviour

import utils.ApplicationConstants.RANDOM

/** This trait defines a standardized interface for the management of
 * simple states owned by an entity. This interface finds application
 * in the management of enemies behaviours and movement behaviours
 * respectively.
 *
 * This is an elaboration of the State programming pattern.
 */
trait StateManager {

  /** Type representing a generic state. At this level is not important
   * to know what a concrete state is.
   */
  type State

  /** Adds and returns a new state to the pool or managed states. It is
   * important to make this new state reachable by providing transitions which
   * includes it.
   *
   * @param state the new State to be added
   * @return the state itself. This is a shorthand for a simplified used of
   *         this structure
   */
  def addState(state:State): State

  /** Adds a new transition linking a state to another. This transition is
   * similar to a predicate which can be tested repeatedly. As soon the
   * transition is active (the test has success) the StateManager current state
   * can be modified
   *
   * @param state the current state of the state machine
   * @param nextState the next state of the state machine
   * @param transition the transition which decides when the state must be
   *                   changed
   */
  def addTransition(state:State, nextState:State, transition:Transition): Unit

  /** Check the available transitions starting from the current state and find
   * out if a new state can be reached. If reachable, the current state is
   * updated.
   */
  def update(): Unit
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

  def getCurrentState: State =
    this.currentState.getOrElse(throw new IllegalArgumentException())

  def getCurrentTransitions: Map[(State, State), Transition] =
    this.transitions.filter(t => t._1._1 == this.getCurrentState)

  def onStateBegin(): Unit

  def onStateEnd(): Unit
}




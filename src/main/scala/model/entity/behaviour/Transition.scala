package model.entity.behaviour

import _root_.utils.ApplicationConstants._
import com.badlogic.gdx.physics.box2d.World
import model.entity.attack.AttackStrategy
import model.helpers.ImplicitConversions.entityToBody
import model.entity._
import model.helpers.EntitiesFactoryImpl
import model.helpers.EntitiesUtilities._
import model.helpers.GeometricUtilities.{getBodiesDistance, isBodyOnTheLeft, isBodyOnTheRight}
import model.helpers.ImplicitConversions.RichWorld

/** This trait define a common interface for the implementation of state
 * manager transitions. A transition resembles the Predicate construct.
 *
 * A transition object can be checked and if active must define a state
 * change in the StateManager.
 *
 * @see [[model.entity.behaviour.StateManager]]
 */
trait Transition {
  def apply(): Boolean
  def reset(): Unit = { }
}

/**
 * Pimping of the interface Transition to enable more interesting uses of
 * the implementation.
 */
object RichTransitions {
  /** A transition may be effectively defined without an inner state and
   * solely characterized by a completely static behaviour. It should be
   * allowed to initialize implicitly a transition only by defining it's
   * apply method.
   *
   * @param f the functional predicate "wrapped" be the Transition interface
   * @return a Transition object wrapping the provided function
   */
  implicit def functionToTransition(f:() => Boolean): Transition = () => f.apply()

  implicit class LogicalTransition(p:Transition){
    def ||(x: Transition): Transition = () => p.apply || x.apply
    def &&(x: Transition): Transition = () => p.apply && x.apply
  }
}

/** Particular case of composition of a Transition. The opposite of a
 * transition can be easily defined with a Transition receive another one.
 *
 * @param transition the Transition t o negate
 */
case class Not(transition: Transition) extends Transition {
  override def apply(): Boolean = !transition.apply()

  override def reset(): Unit = transition.reset()
}

case class TimePredicate(time:Long) extends Transition {
  var startTime: Long = System.currentTimeMillis()

  override def apply(): Boolean = System.currentTimeMillis() - startTime >= time

  override def reset(): Unit = this.startTime = System.currentTimeMillis()
}

case class HealthThreshold(entity:LivingEntity, percentage:Float) extends Transition {
  val healthThreshold: Float = entity.getStatistic(Statistic.Health).get * this.percentage / 100

  override def apply(): Boolean = this.entity.getLife <= this.healthThreshold
}

case class CompletedAttacks(attackStrategy: AttackStrategy,
                            numAttacks: Int = 1) extends Transition {

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

case class RandomlyTrue(percentage: Float) extends Transition {
  private var lastCheckTime:Long = 0
  private val checkPeriod: Long = 2000
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

case class IsEntityAttacking(entity: Entity) extends Transition {
  override def apply(): Boolean = List(State.Attack01, State.Attack02, State.Attack03) contains this.entity.getState
}

case class CanMoveToTheLeft(sourceEntity:Entity) extends Transition {
  override def apply(): Boolean =
    !isPathObstructedOnTheLeft(sourceEntity, vOffset = 0) &&
      isFloorPresentOnTheLeft(sourceEntity, vOffset = 0)
}

case class CanMoveToTheRight(sourceEntity:Entity) extends Transition {
  override def apply(): Boolean =
    !isPathObstructedOnTheRight(sourceEntity, vOffset = 0) &&
      isFloorPresentOnTheRight(sourceEntity, vOffset = 0)
}

case class IsTargetNearby(sourceEntity:Entity,
                          targetEntity:Entity,
                          distance:Float) extends Transition {
  override def apply(): Boolean =
    getBodiesDistance(this.sourceEntity, this.targetEntity) <= distance
}
case class IsTargetVisible(sourceEntity:MobileEntity,
                           targetEntity:Entity) extends Transition {
  val world: World = EntitiesFactoryImpl.getEntitiesContainerMonitor.getWorld.get
  override def apply(): Boolean =
    world.isBodyVisible(this.sourceEntity, this.targetEntity)
}

case class IsPathWalkable(sourceEntity:MobileEntity,
                          targetEntity:Entity) extends Transition {
  override def apply(): Boolean =
    (!isPathObstructedOnTheLeft(this.sourceEntity, vOffset = 0) &&
    isFloorPresentOnTheLeft(this.sourceEntity, vOffset = 0) &&
    isBodyOnTheLeft(this.sourceEntity, this.targetEntity)) ||
    (!isPathObstructedOnTheRight(this.sourceEntity, vOffset = 0) &&
      isFloorPresentOnTheRight(this.sourceEntity, vOffset = 0) &&
      isBodyOnTheRight(this.sourceEntity, this.targetEntity))
}

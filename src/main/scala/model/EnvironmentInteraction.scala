package model

import controller.GameEvent
import controller.GameEvent.GameEvent
import model.collisions.ImplicitConversions.RichTuple2
import model.collisions.CollisionMonitor
import model.entities.State.State
import model.entities._
import model.helpers.{EntitiesFactoryImpl, ItemPools}
import model.movement.{HeroMovements, LadderClimbMovementStrategy}
import utils.EnvironmentConstants.{OPEN_CHEST_COLLISION_BIT, OPEN_DOOR_COLLISION_BIT, THROUGH_PLATFORM_COLLISION_BIT}
import utils.ItemConstants._

/** Represent the hero interaction with a certain environment interaction. The hero will start
 *  the interaction when the command given is notified to him
 *
 *  @param command the command that start the interaction
 *  @param environmentInteraction the interaction that specified the new behavior for the hero
 */
case class HeroInteraction(command: GameEvent, environmentInteraction: EnvironmentInteraction)

/** An environment interaction between the hero and another entity in the world. Changes
 *  the normal behaviour of the hero or change world props properties.
 *
 */
trait EnvironmentInteraction {

  /** Change the behaviour of the hero or the entity attached.
   *
   */
  def apply(): Unit

}

/** Implementation of the interaction between hero and ladder. With the apply the hero
 *  will change his movement strategy with the LadderClimbMovementStrategy.
 *
 *  @constructor the hero-ladder environment interaction
 *  @param entity the entity that will change its behavior
 */
class LadderInteraction(entity: Hero) extends EnvironmentInteraction {

  private var applied: Boolean = false

  override def apply(): Unit = {
    this.entity.stopMovement()

    if(!applied)
      this.startLadderInteraction()
    else {
      this.restoreNormalMovementStrategy()
      val state: State = this.entity.getState
      if(!state.equals(State.Jumping) && !state.equals(State.Somersault))
        this.entity.setState(State.Jumping)
      else
        this.entity.setState(State.Falling)
    }

    this.applied = !applied
  }

  private def startLadderInteraction(): Unit = {
    this.entity.setMovementStrategy(new LadderClimbMovementStrategy(this.entity, this.entity.getStatistic(Statistic.MovementSpeed).get))
    this.entity.setState(State.LadderIdle)
    this.entity.getEntityBody.setGravityScale(0)
  }

  private def restoreNormalMovementStrategy(): Unit = {
    this.entity.setMovementStrategy(new HeroMovements(this.entity, this.entity.getStatistic(Statistic.MovementSpeed).get))
    this.entity.getEntityBody.setGravityScale()
    this.entity.setState(State.Falling)
    this.entity.getBody.setAwake(true)
  }
}

/** Implementation of the interaction between hero and doors. With the apply the door will open.
*
*  @constructor the hero-door environment interaction
*  @param hero the hero
*  @param door the entity that represent the door, which will open upon interaction
*  @param leftSensor the sensor at door's left
*  @param rightSensor the sensor at door's right
*/
class DoorInteraction(hero: Hero, door: Entity, leftSensor: Entity, rightSensor: Entity) extends EnvironmentInteraction {

  override def apply(): Unit = {
    this.door.changeCollisions(OPEN_DOOR_COLLISION_BIT)
    this.leftSensor.changeCollisions(OPEN_DOOR_COLLISION_BIT)
    this.rightSensor.changeCollisions(OPEN_DOOR_COLLISION_BIT)
    this.door.setState(State.Opening)
    this.hero.setEnvironmentInteraction(Option.empty)
    print("Hero opened door")
  }
}

/** Implementation of the interaction between hero and chests. With the apply the chest will open
 *  and an item will drop.
 *
 *  @constructor the hero-chest environment interaction
 *  @param hero the hero
 *  @param chest the entity that represent the chest, which will open upon interaction
 */
class ChestInteraction(private val hero: Hero, private val chest: ImmobileEntity) extends EnvironmentInteraction {

  override def apply(): Unit = {
    chest.setState(State.Opening)
    chest.changeCollisions(OPEN_CHEST_COLLISION_BIT)
    val itemPos: (Float, Float) = chest.getPosition
    EntitiesFactoryImpl.addPendingFunction(() =>
      Item(ItemPools.Enemy_Drops, EntitiesFactoryImpl.getItemPool,
        EntitiesFactoryImpl.getEntitiesContainerMonitor, DEFAULT_POTION_SIZE, itemPos.MPP))
    hero.setEnvironmentInteraction(Option.empty)
  }
}

/** Implementation of the interaction between hero and platforms. With the apply the hero will pass
 *  through platforms
 *
 *  @constructor the hero-chest environment interaction
 *  @param hero the hero
 *  @param platform the entity that represent the platform on which the hero is standing
 *  @param monitor the collision monitor, used to determine the next interaction available for the hero
 */
class PlatformInteraction(private val hero: Hero,
                          private val platform: Entity,
                          private val monitor: CollisionMonitor) extends EnvironmentInteraction {

  override def apply(): Unit = {
    if(monitor.isPlayerOnLadder)
      hero.setEnvironmentInteraction(Option.apply(HeroInteraction(GameEvent.Interaction, new LadderInteraction(hero))))
    else if(monitor.isPlayerTouchingPlatformEdges)
      platform.changeCollisions(THROUGH_PLATFORM_COLLISION_BIT)
    hero.setEnvironmentInteraction(Option.empty)
  }

}

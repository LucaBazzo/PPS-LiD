package model

import controller.GameEvent
import controller.GameEvent.GameEvent
import model.collisions.{CollisionMonitor, EntityCollisionBit}
import model.entities.{Entity, Hero, ImmobileEntity, ItemPools, State, Statistic}
import model.helpers.EntitiesFactoryImpl
import model.movement.{HeroMovementStrategy, LadderClimbMovementStrategy}

import java.util.concurrent.{ExecutorService, Executors}

/** Represent the hero interaction with a certain environment interaction. The hero will start
 *  the interaction when the command given is notified to him
 *
 *  @param command the command that start the interaction
 *  @param environmentInteraction the interaction that specified the new behavior for the hero
 */
case class HeroInteraction(command: GameEvent, environmentInteraction: EnvironmentInteraction)

/** An environment interaction between the hero and another entity in the world. Changes
 *  the normal behaviour of the hero.
 *
 */
trait EnvironmentInteraction {

  /** Change the behaviour of the entity attached.
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
      this.entity.setState(State.Jumping)
    }

    this.applied = !applied
  }

  private def startLadderInteraction(): Unit = {
    this.entity.setMovementStrategy(new LadderClimbMovementStrategy(this.entity, this.entity.getStatistic(Statistic.MovementSpeed).get))
    this.entity.setState(State.LadderIdle)
    this.entity.getEntityBody.setGravityScale(0)
  }

  private def restoreNormalMovementStrategy(): Unit = {
    this.entity.setMovementStrategy(new HeroMovementStrategy(this.entity, this.entity.getStatistic(Statistic.MovementSpeed).get))
    this.entity.getEntityBody.setGravityScale()
    this.entity.setState(State.Falling)
    this.entity.getBody.setAwake(true)
  }
}

class DoorInteraction(hero: Hero, door: Entity, leftSensor: Entity, rightSensor: Entity) extends EnvironmentInteraction {

  override def apply(): Unit = {
    this.door.changeCollisions(EntityCollisionBit.OpenedDoor)
    this.leftSensor.changeCollisions(EntityCollisionBit.OpenedDoor)
    this.rightSensor.changeCollisions(EntityCollisionBit.OpenedDoor)
    this.door.setState(State.Opening)
    this.hero.setEnvironmentInteraction(Option.empty)
    print("Hero opened door")
  }
}

class ChestInteraction(private val hero: Hero, private val chest: ImmobileEntity) extends EnvironmentInteraction {

  override def apply(): Unit = {
    chest.setState(State.Opening)
    chest.changeCollisions(EntityCollisionBit.DestroyedDoor)
    val itemPos: (Float, Float) = chest.getPosition
    //EntitiesFactoryImpl.createItem(ItemPools.Level_1, position = (itemPos._1 + 1f, itemPos._2 + 1f))
    hero.setEnvironmentInteraction(Option.empty)
  }
}

class PlatformInteraction(private val hero: Hero,
                          private val upperPlatform: Entity,
                          private val platform: Entity,
                          private val lowerPlatform: Entity,
                          private val monitor: CollisionMonitor) extends EnvironmentInteraction {

  override def apply(): Unit = {
    if(monitor.isPlayerOnLadder)
      hero.setEnvironmentInteraction(Option.apply(HeroInteraction(GameEvent.Interaction, new LadderInteraction(hero))))
    else
      hero.setEnvironmentInteraction(Option.empty)
    this.platformCollisions(EntityCollisionBit.Enemy)
    val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    executorService.execute(() => {
      Thread.sleep(1000)
      this.platformCollisions((EntityCollisionBit.Enemy | EntityCollisionBit.Hero).toShort)
      println("Enabled platform collisions")
    })
  }

  private def platformCollisions(collisions: Short): Unit = {
    platform.changeCollisions(collisions)
    upperPlatform.changeCollisions(collisions)
    lowerPlatform.changeCollisions(collisions)
  }
}

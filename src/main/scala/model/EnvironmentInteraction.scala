package model

import controller.GameEvent
import controller.GameEvent.GameEvent
import model.collisions.{CollisionMonitor, EntityCollisionBit}
import model.entities.{Entity, Hero, State, Statistic}
import model.movement.{HeroMovementStrategy, LadderClimbMovementStrategy}

import java.util.concurrent.{ExecutorService, Executors}

case class HeroInteraction(command: GameEvent, environmentInteraction: EnvironmentInteraction)

trait EnvironmentInteraction {

  def apply(): Unit

}

class LadderInteraction(entity: Hero) extends EnvironmentInteraction {

  private var applied: Boolean = false

  override def apply(): Unit = {
    this.entity.stopMovement()

    if(!applied) {
      this.entity.setMovementStrategy(new LadderClimbMovementStrategy(this.entity, this.entity.getStatistic(Statistic.MovementSpeed)))
      this.entity.setState(State.LadderIdle)
      this.entity.getEntityBody.setGravityScale(0)
      //this.entity.changeCollisions((EntityCollisionBit.Enemy | EntityCollisionBit.EnemyAttack | EntityCollisionBit.Ladder).toShort)
    }
    else {
      this.entity.setMovementStrategy(new HeroMovementStrategy(this.entity, this.entity.getStatistic(Statistic.MovementSpeed)))
      this.entity.getEntityBody.setGravityScale()
      this.entity.setState(State.Falling)
      this.entity.getBody.setAwake(true)
      /*this.entity.changeCollisions((EntityCollisionBit.Enemy | EntityCollisionBit.EnemyAttack).toShort)*/
    }
    this.applied = !applied
  }
}

class DoorInteraction(hero: Hero, door: Entity) extends EnvironmentInteraction {

  override def apply(): Unit = {
    this.door.changeCollisions(EntityCollisionBit.OpenedDoor)
    this.door.setState(State.Opening)
    this.hero.setEnvironmentInteraction(Option.empty)
    print("Hero opened door")
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
    platform.changeCollisions(EntityCollisionBit.Enemy)
    upperPlatform.changeCollisions(EntityCollisionBit.Enemy)
    lowerPlatform.changeCollisions(EntityCollisionBit.Enemy)
    val executorService: ExecutorService = Executors.newSingleThreadExecutor()
    executorService.execute(() => {
      Thread.sleep(1000)
      platform.changeCollisions((EntityCollisionBit.Enemy | EntityCollisionBit.Hero).toShort)
      upperPlatform.changeCollisions((EntityCollisionBit.Enemy | EntityCollisionBit.Hero).toShort)
      lowerPlatform.changeCollisions((EntityCollisionBit.Enemy | EntityCollisionBit.Hero).toShort)
      println("Enabled platform collisions")
    })
  }
}

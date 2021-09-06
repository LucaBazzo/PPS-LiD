package model.collisions

import controller.GameEvent
import model.{DoorInteraction, HeroInteraction, LadderInteraction, PlatformInteraction}
import model.entities.{CircularMobileEntity, Entity, Hero, ImmobileEntity, Item, _}
import model.helpers.{EntitiesContainerMonitor, EntitiesSetter}

import java.util.concurrent.{ExecutorService, Executors}

class ItemCollisionStrategy(private val item: Item, private val entitiesMonitor: EntitiesSetter) extends DoNothingOnCollision {
  override def apply(entity: Entity): Unit = entity match {
    case h:Hero => println("Hero picked up item")
                   val effect = item.collect()
                   println(effect._3 + "\n +" + item.getScore + " points")
                   h.itemPicked(item.getEnumVal)
                   entitiesMonitor.addMessage(effect._3)
                   h.alterStatistics(effect._1, effect._2)
    case _ => println("____")
  }
}

class DoorCollisionStrategy(private val door: ImmobileEntity) extends DoNothingOnCollision {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => h.setEnvironmentInteraction(Option.apply(HeroInteraction(GameEvent.Interaction, new DoorInteraction(h,this.door))))
    case s: CircularMobileEntity => println("Hero destroyed door")
                    this.door.changeCollisions(EntityCollisionBit.DestroyedDoor)
  }

  override def release(entity: Entity): Unit = entity match {
    case h: Hero => h.setEnvironmentInteraction(Option.empty)
  }
}

class WaterCollisionStrategy() extends DoNothingOnCollision {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => println("Hero stands in water")
      h.alterStatistics(Statistic.MovementSpeed, -0.7f)
  }

  override def release(entity: Entity): Unit = entity match {
    case h: Hero => println("Hero out of water")
      h.alterStatistics(Statistic.MovementSpeed, +0.7f)
  }
}

class LavaCollisionStrategy(private val collisMonitor: CollisionMonitor) extends DoNothingOnCollision {
  val executorService: ExecutorService = Executors.newSingleThreadExecutor()
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => println("Hero stands in lava")
      collisMonitor.playerInLava()
      executorService.execute(() => {
        while(collisMonitor.isPlayerInsideLava) {
          h.sufferDamage(100)
          println("Enabled platform collisions")
          Thread.sleep(1000)
        }
      })
  }

  override def release(entity: Entity): Unit = entity match {
    case h: Hero => println("Hero out of lava")
      collisMonitor.playerOutOfLava()
  }
}

class UpperPlatformCollisionStrategy(private val platform: ImmobileEntity,
                                private val upperPlatform: ImmobileEntity,
                                private val lowerPlatform: ImmobileEntity,
                                     private val monitor: CollisionMonitor) extends DoNothingOnCollision {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => println("Hero standing on Platform" + "\n")
                    h.setEnvironmentInteraction(Option.apply(HeroInteraction(GameEvent.Interaction, new PlatformInteraction(h,
                      this.upperPlatform, this.platform, this.lowerPlatform, monitor))))
  }

  override def release(entity: Entity): Unit = entity match {
    case h: Hero => println("Hero leaving Platform")
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

class LowerPlatformCollisionStrategy(private val platform: ImmobileEntity,
                                private val upperPlatform: ImmobileEntity,
                                private val lowerPlatform: ImmobileEntity) extends DoNothingOnCollision {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => println("Hero touching lower Platform")
      platform.changeCollisions(EntityCollisionBit.Enemy)
      upperPlatform.changeCollisions(EntityCollisionBit.Enemy)
      lowerPlatform.changeCollisions(EntityCollisionBit.Enemy)
  }

  override def release(entity: Entity): Unit = entity match {
    case h: Hero => println("Hero leaving Platform")
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

class LadderCollisionStrategy(private val monitor: CollisionMonitor) extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => print("Hero touches ladder" + "\n")
                    monitor.playerOnLadder()
                    h.setEnvironmentInteraction(Option.apply(HeroInteraction(GameEvent.Interaction, new LadderInteraction(h))))
  }

  override def release(entity: Entity): Unit = entity match {
    case h:Hero => print("Hero leaving ladder" + "\n")
      monitor.playerQuitLadder()
      h.setEnvironmentInteraction(Option.empty)
  }
}

package model.collisions

import controller.GameEvent
import model.entities.{CircularMobileEntity, Entity, Hero, ImmobileEntity, Item, _}
import model.helpers.EntitiesSetter
import model.{ChestInteraction, DoorInteraction, HeroInteraction, LadderInteraction, PlatformInteraction}

import java.util.concurrent.{ExecutorService, Executors}

class ItemCollisionStrategy(private val item: Item, private val entitiesMonitor: EntitiesSetter) extends DoNothingOnCollision {
  override def apply(entity: Entity): Unit = entity match {
    case h:Hero => println("Hero picked up item")
                   val effect = item.collect()
                   println(effect._2 + "\n +" + item.getScore + " points")
                   h.itemPicked(item.getEnumVal)
                   entitiesMonitor.addMessage(effect._2)
                   if(effect._1.nonEmpty) {
                     for(stat <- effect._1.get)
                       h.alterStatistics(stat._1, stat._2)
                   }
    case _ => println("____")
  }
}

class DoorCollisionStrategy(private val entitySetter: EntitiesSetter,
                            private val door: ImmobileEntity,
                            private val doorSensorLeft: ImmobileEntity,
                            private val doorSensorRight: ImmobileEntity) extends DoNothingOnCollision {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => {
      this.entitySetter.addMessage("Press F to open the door")
      h.setEnvironmentInteraction(Option.apply(HeroInteraction(GameEvent.Interaction, new DoorInteraction(h, this.door,
        doorSensorLeft, doorSensorRight))))
    }
    case _: CircularMobileEntity => println("Hero destroyed door")
      this.door.changeCollisions(EntityCollisionBit.OpenedDoor)
      this.doorSensorLeft.changeCollisions(EntityCollisionBit.OpenedDoor)
      this.doorSensorRight.changeCollisions(EntityCollisionBit.OpenedDoor)
      this.door.setState(State.Opening)
    case _ =>
  }

  override def release(entity: Entity): Unit = entity match {
    case h: Hero => h.setEnvironmentInteraction(Option.empty)
    case _ =>
  }
}

class BossDoorCollisionStrategy(private val entitySetter: EntitiesSetter,
                                private val door: ImmobileEntity,
                                private val doorSensorLeft: ImmobileEntity,
                                private val doorSensorRight: ImmobileEntity) extends DoNothingOnCollision {

  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => if(h.isItemPicked(Items.Key) || h.isItemPicked(Items.SkeletonKey))
        {
          this.entitySetter.addMessage("Press F to open the door")
          h.setEnvironmentInteraction(Option.apply(HeroInteraction(GameEvent.Interaction, new DoorInteraction(h,this.door,
            doorSensorLeft,doorSensorRight))))
        }
      else
        this.entitySetter.addMessage("A key is needed to proceed")
    case _ =>
  }

  override def release(entity: Entity): Unit = entity match {
    case h: Hero => h.setEnvironmentInteraction(Option.empty)
    case _ =>
  }

}

class WaterCollisionStrategy() extends DoNothingOnCollision {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => println("Hero stands in water")
      h.alterStatistics(Statistic.MovementSpeed, -0.7f)
    case _ =>
  }

  override def release(entity: Entity): Unit = entity match {
    case h: Hero => println("Hero out of water")
      h.alterStatistics(Statistic.MovementSpeed, +0.7f)
    case _ =>
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
    case _ =>
  }

  override def release(entity: Entity): Unit = entity match {
    case _: Hero => println("Hero out of lava")
      collisMonitor.playerOutOfLava()
    case _ =>
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
    case _ =>
  }

  override def release(entity: Entity): Unit = entity match {
    case _: Hero => println("Hero leaving Platform")
      val executorService: ExecutorService = Executors.newSingleThreadExecutor()
      executorService.execute(() => {
        Thread.sleep(1000)
        platform.changeCollisions((EntityCollisionBit.Enemy | EntityCollisionBit.Hero).toShort)
        upperPlatform.changeCollisions((EntityCollisionBit.Enemy | EntityCollisionBit.Hero).toShort)
        lowerPlatform.changeCollisions((EntityCollisionBit.Enemy | EntityCollisionBit.Hero).toShort)
        println("Enabled platform collisions")
      })
    case _ =>
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
    case _ =>
  }

  override def release(entity: Entity): Unit = entity match {
    case _: Hero => println("Hero leaving Platform")
      val executorService: ExecutorService = Executors.newSingleThreadExecutor()
      executorService.execute(() => {
        Thread.sleep(1000)
        platform.changeCollisions((EntityCollisionBit.Enemy | EntityCollisionBit.Hero).toShort)
        upperPlatform.changeCollisions((EntityCollisionBit.Enemy | EntityCollisionBit.Hero).toShort)
        lowerPlatform.changeCollisions((EntityCollisionBit.Enemy | EntityCollisionBit.Hero).toShort)
        println("Enabled platform collisions")
      })
    case _ =>
  }
}

class ChestCollisionStrategy(private val entitiesSetter: EntitiesSetter,
                             private val chest: ImmobileEntity) extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => print("Hero touches chest" + "\n")
      this.entitiesSetter.addMessage("Press F to open the chest")
      h.setEnvironmentInteraction(Option.apply(HeroInteraction(GameEvent.Interaction, new ChestInteraction(h,chest))))
    case _ =>
  }

  override def release(entity: Entity): Unit = entity match {
    case h:Hero => print("Hero not touching chest anymore" + "\n")
      h.setEnvironmentInteraction(Option.empty)
    case _ =>
  }
}

class PortalCollisionStrategy() extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => print("Hero touches portal" + "\n")
      //h.setEnvironmentInteraction(Option.apply(HeroInteraction(GameEvent.Interaction, new PortalInteraction(h))))
    case _ =>
  }

  override def release(entity: Entity): Unit = entity match {
    case h:Hero => print("Hero not touching portal anymore" + "\n")
      h.setEnvironmentInteraction(Option.empty)
    case _ =>
  }
}

class LadderCollisionStrategy(private val monitor: CollisionMonitor) extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => print("Hero touches ladder" + "\n")
                    monitor.playerOnLadder()
                    h.setEnvironmentInteraction(Option.apply(HeroInteraction(GameEvent.Interaction, new LadderInteraction(h))))
    case _ =>
  }

  override def release(entity: Entity): Unit = entity match {
    case h:Hero => print("Hero leaving ladder" + "\n")
      monitor.playerQuitLadder()
      h.setState(State.Jumping)
      h.setEnvironmentInteraction(Option.empty)
    case _ =>
  }
}

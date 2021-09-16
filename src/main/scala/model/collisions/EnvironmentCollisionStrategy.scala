package model.collisions

import controller.GameEvent
import model.entities.{CircularMobileEntity, Entity, Hero, ImmobileEntity, Item, _}
import model.helpers.EntitiesSetter
import model.{ChestInteraction, DoorInteraction, HeroInteraction, LadderInteraction, Level, PlatformInteraction}
import utils.EnvironmentConstants._

import java.util.concurrent.{ExecutorService, Executors}

class ItemCollisionStrategy(private val item: Item, private val entitiesMonitor: EntitiesSetter) extends DoNothingOnCollision {
  override def apply(entity: Entity): Unit = entity match {
    case h:Hero => println("Hero picked up item")
                   val effect = item.collect()
                   println(effect._2 + "\n +" + item.getScore + " points")
                   h.itemPicked(item.getName)
                   entitiesMonitor.addMessage(effect._2)
                   entitiesMonitor.heroJustPickedUpItem(item.getName)
                   if(effect._1.nonEmpty) {
                     for(stat <- effect._1.get) {
                       if(stat._1.equals(Statistic.CurrentHealth))
                         h.healLife(stat._2)
                       else
                       h.alterStatistics(stat._1, stat._2)
                     }
                   }
    case _ => println("____")
  }
}

class DoorCollisionStrategy(private val entitySetter: EntitiesSetter,
                            private val door: ImmobileEntity,
                            private val doorSensorLeft: ImmobileEntity,
                            private val doorSensorRight: ImmobileEntity) extends DoNothingOnCollision {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero =>
      this.entitySetter.addMessage("Press Space to open the door")
      h.setEnvironmentInteraction(Option.apply(HeroInteraction(GameEvent.Interaction, new DoorInteraction(h, this.door,
        doorSensorLeft, doorSensorRight))))
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
          this.entitySetter.addMessage("Press Space to open the door")
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
      h.alterStatistics(Statistic.MovementSpeed, -WATER_SPEED_ALTERATION)
    case _ =>
  }

  override def release(entity: Entity): Unit = entity match {
    case h: Hero => println("Hero out of water")
      h.alterStatistics(Statistic.MovementSpeed, +WATER_SPEED_ALTERATION)
    case _ =>
  }
}

class LavaCollisionStrategy(private val collisMonitor: CollisionMonitor) extends DoNothingOnCollision {
  var executorService: ExecutorService = _
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => println("Hero stands in lava")
      collisMonitor.playerInLava()
      this.executorService = Executors.newSingleThreadExecutor()
      executorService.execute(() => {
        while(collisMonitor.isPlayerInsideLava) {
          h.sufferDamage(LAVA_DAMAGE_PER_TICK)
          if(h.getLife <= 0)
            collisMonitor.playerOutOfLava()
          println("Taken damage from lava")
          Thread.sleep(LAVA_DAMAGE_TICK_RATE)
        }
      })
    case _ =>
  }

  override def release(entity: Entity): Unit = entity match {
    case _: Hero => println("Hero out of lava")
      collisMonitor.playerOutOfLava()
      executorService.shutdown()
    case _ =>
  }
}

class UpperPlatformCollisionStrategy(private val platform: ImmobileEntity,
                                     private val monitor: CollisionMonitor) extends DoNothingOnCollision {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => println("Hero standing on Platform")
                    this.monitor.playerTouchesPlatformEdge()
                    h.setEnvironmentInteraction(Option.apply(HeroInteraction(GameEvent.Interaction, new PlatformInteraction(h,
                      this.platform, this.monitor))))
    case _ =>
  }

  override def release(entity: Entity): Unit = entity match {
    case h: Hero => println("Hero leaving Platform")
      if(! this.monitor.isPlayerTouchingPlatformEdges) {
        platform.changeCollisions((EntityCollisionBit.Enemy | EntityCollisionBit.Hero).toShort)
        println("Enabled platform collisions")
      }
      this.monitor.playerQuitPlatform()
      if(! monitor.isPlayerOnLadder)
        h.setEnvironmentInteraction(Option.empty)
    case _ =>
  }
}

class LowerPlatformCollisionStrategy(private val platform: ImmobileEntity,
                                     private val monitor: CollisionMonitor) extends DoNothingOnCollision {
  override def apply(entity: Entity): Unit = entity match {
    case _: Hero => println("Hero touching lower Platform")
      this.monitor.playerTouchesPlatformEdge()
      platform.changeCollisions(EntityCollisionBit.Enemy)
    case _ =>
  }

  override def release(entity: Entity): Unit = entity match {
    case _: Hero => println("Hero leaving Platform")
      this.platformReleaseCollision()
    case e: MobileEntity => if(e.getType.equals(EntityType.HeroFeet)) this.platformReleaseCollision()

  }

  private def platformReleaseCollision(): Unit = {
    if(! this.monitor.isPlayerTouchingPlatformEdges) {
      platform.changeCollisions((EntityCollisionBit.Enemy | EntityCollisionBit.Hero).toShort)
      println("Enabled platform collisions")
    }
    this.monitor.playerQuitPlatform()
  }

}

class ChestCollisionStrategy(private val entitiesSetter: EntitiesSetter,
                             private val chest: ImmobileEntity) extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => println("Hero touches chest")
      this.entitiesSetter.addMessage("Press Space to open the chest")
      h.setEnvironmentInteraction(Option.apply(HeroInteraction(GameEvent.Interaction, new ChestInteraction(h,chest))))
    case _ =>
  }

  override def release(entity: Entity): Unit = entity match {
    case h:Hero => println("Hero not touching chest anymore")
      h.setEnvironmentInteraction(Option.empty)
    case _ =>
  }
}

class PortalCollisionStrategy(private val portal: ImmobileEntity, private val level: Level) extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case _: Hero => println("Hero touches portal")
      if(this.portal.getState == State.Standing)
        this.level.newLevel()
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
      if(! monitor.isPlayerTouchingPlatformEdges)
        h.setEnvironmentInteraction(Option.empty)
    case _ =>
  }
}

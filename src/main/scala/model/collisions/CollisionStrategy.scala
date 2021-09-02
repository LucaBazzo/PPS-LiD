package model.collisions

import controller.GameEvent
import model.{DoorInteraction, HeroInteraction, LadderInteraction, PlatformInteraction}
import model.entities.Statistic.Statistic
import model.entities.{CircularMobileEntity, Entity, Hero, ImmobileEntity, Item, _}

import java.util.concurrent.{ExecutorService, Executors}


trait CollisionStrategy {
  def apply(entity: Entity): Unit
}

class CollisionStrategyImpl extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case i:Item =>  println("Collect item: " + i.getEnumVal)
    case _ => println("Collision Detected with" + entity.toString)
  }
}

class ItemCollisionStrategy(private val item: Item) extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case h:Hero => println("Hero picked up item")
                   val effect = item.collect()
                   println(effect._3 + "\n +" + item.getScore + " points")
                   h.alterStatistics(effect._1, effect._2)
    case _ => println("____")
  }
}

class DoorCollisionStrategy(private val door: ImmobileEntity) extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => h.setEnvironmentInteraction(Option.apply(HeroInteraction(GameEvent.Interaction, new DoorInteraction(h,this.door))))
    case s: CircularMobileEntity => print("Hero destroyed door")
                    this.door.changeCollisions(EntityCollisionBit.DestroyedDoor)
  }
}

class EndDoorCollisionStrategy(private val door: ImmobileEntity) extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => h.setEnvironmentInteraction(Option.empty)

  }
}

class WaterCollisionStrategy() extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => printf("Hero stands in water")
      h.alterStatistics(Statistic.MovementSpeed, -0.7f)
  }
}

class EndWaterCollisionStrategy() extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => printf("Hero out of water")
      h.alterStatistics(Statistic.MovementSpeed, +0.7f)
  }
}

class LavaCollisionStrategy(private val collisMonitor: CollisionMonitor) extends CollisionStrategy {
  val executorService: ExecutorService = Executors.newSingleThreadExecutor()
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => printf("Hero stands in lava")
      collisMonitor.playerInLava()
      executorService.execute(() => {
        while(collisMonitor.isPlayerInsideLava) {
          h.sufferDamage(100)
          println("Enabled platform collisions")
          Thread.sleep(1000)
        }
      })
  }
}

class EndLavaCollisionStrategy(private val collisMonitor: CollisionMonitor) extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => printf("Hero out of lava")
                    collisMonitor.playerOutOfLava()
  }
}

class UpperPlatformCollisionStrategy(private val platform: ImmobileEntity,
                                private val upperPlatform: ImmobileEntity,
                                private val lowerPlatform: ImmobileEntity) extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => print("Hero standing on Platform" + "\n")
                    h.setEnvironmentInteraction(Option.apply(HeroInteraction(GameEvent.Interaction, new PlatformInteraction(h,
                      this.upperPlatform, this.platform, this.lowerPlatform))))
  }
}

class LowerPlatformCollisionStrategy(private val platform: ImmobileEntity,
                                private val upperPlatform: ImmobileEntity,
                                private val lowerPlatform: ImmobileEntity) extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => print("Hero touching lower Platform" + "\n")
      platform.changeCollisions(EntityCollisionBit.Enemy)
      upperPlatform.changeCollisions(EntityCollisionBit.Enemy)
      lowerPlatform.changeCollisions(EntityCollisionBit.Enemy)
  }
}

class PlatformEndCollisionStrategy(private val platform: ImmobileEntity,
                                   private val upperPlatform: ImmobileEntity,
                                   private val lowerPlatform: ImmobileEntity) extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => print("Hero leaving Platform" + "\n")
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

class LadderCollisionStrategy() extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => print("Hero touches ladder")
                    h.setEnvironmentInteraction(Option.apply(HeroInteraction(GameEvent.Interaction, new LadderInteraction(h))))
  }
}

class EndLadderCollisionStrategy() extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case h:Hero => print("Hero leaving ladder")
                   h.setEnvironmentInteraction(Option.empty)
                   /*h.changeCollisions((EntityCollisionBit.Immobile | EntityCollisionBit.Enemy | EntityCollisionBit.Ladder |
                     EntityCollisionBit.Item | EntityCollisionBit.Door | EntityCollisionBit.EnemyAttack).toShort)*/
  }
}

class DoNothingOnCollision() extends CollisionStrategy {
  override def apply(entity: Entity): Unit = {}
}

class ApplyDamage(private val target: Entity => Boolean,
                  private val stats: Map[Statistic, Float])
  extends CollisionStrategy {

  override def apply(entity: Entity): Unit = {
    if (target(entity)) {
      entity.asInstanceOf[LivingEntity].sufferDamage(stats(Statistic.Strength))
    }
  }
}

class ApplyDamageAndDestroyEntity(private val sourceEntity: Entity,
                                 private val target: Entity => Boolean,
                                 private val stats: Map[Statistic, Float])
  extends ApplyDamage(target, stats) {

  override def apply(entity: Entity): Unit = {
    super.apply(entity)

    if ((entity.getBody.getFixtureList.toArray().head.getFilterData.maskBits
      & this.sourceEntity.getBody.getFixtureList.toArray().head.getFilterData.categoryBits) != 0) {
//      this.sourceEntity.setState(State.Dying)
      this.sourceEntity.destroyEntity()
    }
  }
}


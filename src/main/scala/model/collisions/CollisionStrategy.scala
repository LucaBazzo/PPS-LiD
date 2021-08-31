package model.collisions

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import model.entities.Statistic.Statistic
import model.entities.{CircularMobileEntity, Entity, Hero, ImmobileEntity, Item, _}

import java.util.concurrent.{ExecutorService, Executors}


trait CollisionStrategy {
  def apply(entity: Entity)
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
    case h: Hero => print("Hero opened door")
                    this.door.changeCollisions(EntityCollisionBit.OpenedDoor)
    case s: CircularMobileEntity => print("Hero destroyed door")
                    this.door.changeCollisions(EntityCollisionBit.DestroyedDoor)
  }
}

class UpperPlatformCollisionStrategy(private val platform: ImmobileEntity,
                                private val upperPlatform: ImmobileEntity,
                                private val lowerPlatform: ImmobileEntity) extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => print("Hero standing on Platform" + "\n")
                    platform.changeCollisions(EntityCollisionBit.Enemy)
                    upperPlatform.changeCollisions(EntityCollisionBit.Enemy)
                    lowerPlatform.changeCollisions(EntityCollisionBit.Enemy)
  }
}

class LowerPlatformCollisionStrategy(private val platform: ImmobileEntity,
                                private val upperPlatform: ImmobileEntity,
                                private val lowerPlatform: ImmobileEntity) extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => print("Hero standing on Platform" + "\n")
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
                      Thread.sleep(2000)
                      platform.changeCollisions((EntityCollisionBit.Enemy | EntityCollisionBit.Hero).toShort)
                      upperPlatform.changeCollisions((EntityCollisionBit.Enemy | EntityCollisionBit.Hero).toShort)
                      lowerPlatform.changeCollisions((EntityCollisionBit.Enemy | EntityCollisionBit.Hero).toShort)
                      println("Enabled platform collisions")
                    })
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


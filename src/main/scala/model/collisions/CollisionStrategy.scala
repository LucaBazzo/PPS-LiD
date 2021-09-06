package model.collisions

import model.entities.Statistic.Statistic
import model.entities.{CircularMobileEntity, Entity, Hero, ImmobileEntity, Item, _}


trait CollisionStrategy {
  def apply(entity: Entity)
  def release(entity: Entity)
}

class DoNothingOnCollision() extends CollisionStrategy {
  override def apply(entity: Entity): Unit = {}

  override def release(entity: Entity): Unit = {}
}

class FeetCollisionStrategy extends DoNothingOnCollision {
  override def apply(entity: Entity): Unit = entity match {
    case _ => println("Foot Collision Detected with" + entity.toString)
  }

  override def release(entity: Entity): Unit = entity match {
    case _ => println("Foot Release Detected with" + entity.toString)
  }
}

class CollisionStrategyImpl extends DoNothingOnCollision {
  override def apply(entity: Entity): Unit = entity match {
    case i:Item =>  println("Collect item: " + i.getEnumVal)
    case _ => println("Collision Detected with" + entity.toString)
  }
}

class ItemCollisionStrategy(private val item: Item) extends DoNothingOnCollision {
  override def apply(entity: Entity): Unit = entity match {
    case h:Hero => println("Hero picked up item")
                   val effect = item.collect()
                   println(effect._3 + "\n +" + item.getScore + " points")
                   h.alterStatistics(effect._1, effect._2)
    case _ => println("____")
  }
}

class DoorCollisionStrategy(private val door: ImmobileEntity) extends DoNothingOnCollision {
  override def apply(entity: Entity): Unit = entity match {
    case h: Hero => print("Hero opened door")
                    this.door.changeCollisions(EntityCollisionBit.OpenedDoor)
    case s: CircularMobileEntity => print("Hero destroyed door")
                    this.door.changeCollisions(EntityCollisionBit.DestroyedDoor)
  }
}



class ApplyDamage(private val target: Entity => Boolean,
                  private val stats: Map[Statistic, Float])
  extends DoNothingOnCollision {

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

    // source entity should be destroyed in any case
    this.sourceEntity.destroyEntity()
  }
}


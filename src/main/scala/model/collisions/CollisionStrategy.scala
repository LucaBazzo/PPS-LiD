package model.collisions

import model.entities.Statistic.Statistic
import model.entities._

import scala.collection.mutable

trait CollisionStrategy {
  def apply(entity: Entity)
}

class CollisionStrategyImpl extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case i:ItemImpl => println("Collect item: " + entity.toString)
    case _ => println("Collision Detected with" + entity.toString)
  }
}

class ItemCollisionStrategy extends CollisionStrategy {
  override def apply(entity: Entity): Unit = entity match {
    case h:HeroImpl => println("Hero picked up item")
    case _ => println("____")
  }
}

class DoNotCollide() extends CollisionStrategy {
  override def apply(entity: Entity): Unit = {}
}

class ApplyDamage(private val owner:Entity,
                  private val target:Entity => Boolean,
                  private val stats:mutable.Map[Statistic, Float])
  extends CollisionStrategy {

  override def apply(entity: Entity): Unit = {
    if (target(entity)) {
      entity.asInstanceOf[LivingEntity].sufferDamage(stats(Statistic.Strength))
    }
  }
}

class ApplyDamageAndDestroyOwner(private val owner:Entity,
                                 private val target:Entity => Boolean,
                                 private val stats:mutable.Map[Statistic, Float])
  extends ApplyDamage(owner, target, stats) {

  override def apply(entity: Entity): Unit = {
    super.apply(entity)

    if ((entity.getBody.getFixtureList.toArray().head.getFilterData.maskBits & owner.getType) != 0) {
      this.owner.setState(State.Dying)
    }
  }
}

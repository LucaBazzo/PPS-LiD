package model.collisions

import controller.GameEvent
import model.entities.{CircularMobileEntity, Entity, Hero, ImmobileEntity, Item, _}
import model.helpers.EntitiesSetter
import model._

case class ItemCollisionStrategy(private val item: Item,
                            private val entitiesMonitor: EntitiesSetter) extends CollisionStrategyImpl {
  override def contact(entity: Entity): Unit = {
    entity match {
      case h:Hero =>
        println("Hero picked up item")
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
      case _ =>
    }
  }
}

case class DoorCollisionStrategy(private val entitySetter: EntitiesSetter,
                            private val door: ImmobileEntity,
                            private val doorSensorLeft: ImmobileEntity,
                            private val doorSensorRight: ImmobileEntity) extends CollisionStrategyImpl {
  override def contact(entity: Entity): Unit = entity match {
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

case class BossDoorCollisionStrategy(private val entitySetter: EntitiesSetter,
                                private val door: ImmobileEntity,
                                private val doorSensorLeft: ImmobileEntity,
                                private val doorSensorRight: ImmobileEntity) extends CollisionStrategyImpl {

  override def contact(entity: Entity): Unit = entity match {
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

case class WaterCollisionStrategy() extends CollisionStrategyImpl {
  override def contact(entity: Entity): Unit = entity match {
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

case class LavaCollisionStrategy() extends CollisionStrategyImpl {
  private var timer:Long = 0
  private var hero:Option[Hero] = Option.empty
  private val LAVA_DAMAGE_TIME: Long = 1000
  private val LAVA_DAMAGE: Float = 100

  override def contact(entity: Entity): Unit = entity match {
    case hero: Hero =>
      println("Hero stands in lava")
      this.hero = Option(hero)
    case _ =>
  }

  override def release(entity: Entity): Unit = entity match {
    case _: Hero => println("Hero out of lava")
      this.hero = Option.empty
    case _ =>
  }

  override def apply(): Unit = {
    val now:Long = System.currentTimeMillis()
    if (this.hero.isDefined && System.currentTimeMillis() - this.timer > LAVA_DAMAGE_TIME) {
      hero.get.sufferDamage(LAVA_DAMAGE)
      println("Taken damage from lava")
      this.timer = now
    }
  }
}

case class UpperPlatformCollisionStrategy(private val platform: ImmobileEntity,
                                private val upperPlatform: ImmobileEntity,
                                private val lowerPlatform: ImmobileEntity,
                                     private val monitor: CollisionMonitor) extends CollisionStrategyImpl {
  private val PLATFORM_DEACTIVATION_TIME: Long = 1000
  private var timer:Long = 0

  override def contact(entity: Entity): Unit = entity match {
    case h: Hero =>
      println("Hero standing on Platform")
      h.setEnvironmentInteraction(Option.apply(HeroInteraction(GameEvent.Interaction, new PlatformInteraction(h,
        this.upperPlatform, this.platform, this.lowerPlatform, monitor))))
    case _ =>
  }

  override def release(entity: Entity): Unit = entity match {
    case _: Hero => println("Hero leaving Platform")
      this.timer = System.currentTimeMillis()
    case _ =>
  }

  override def apply(): Unit = {
    super.apply()
    if (this.timer != 0 && System.currentTimeMillis() - this.timer > PLATFORM_DEACTIVATION_TIME) {
      platform.changeCollisions((EntityCollisionBit.Enemy | EntityCollisionBit.Hero).toShort)
      upperPlatform.changeCollisions((EntityCollisionBit.Enemy | EntityCollisionBit.Hero).toShort)
      lowerPlatform.changeCollisions((EntityCollisionBit.Enemy | EntityCollisionBit.Hero).toShort)
      println("Enabled platform collisions 1")
      this.timer = 0
    }
  }
}

case class LowerPlatformCollisionStrategy(private val platform: ImmobileEntity,
                                private val upperPlatform: ImmobileEntity,
                                private val lowerPlatform: ImmobileEntity) extends CollisionStrategyImpl {
  private val PLATFORM_DEACTIVATION_TIME: Long = 1200
  private var timer:Long = 0
  
  override def contact(entity: Entity): Unit = entity match {
    case _: Hero => 
      println("Hero touching lower Platform")
      platform.changeCollisions(EntityCollisionBit.Enemy)
      upperPlatform.changeCollisions(EntityCollisionBit.Enemy)
      lowerPlatform.changeCollisions(EntityCollisionBit.Enemy)
    case _ =>
  }

  override def release(entity: Entity): Unit = entity match {
    case _: Hero =>
      this.timer = System.currentTimeMillis()
      println("Hero leaving Platform")
    case _ =>
  }

  override def apply(): Unit = {
    super.apply()
    if (this.timer != 0 && System.currentTimeMillis() - this.timer > PLATFORM_DEACTIVATION_TIME) {
      platform.changeCollisions((EntityCollisionBit.Enemy | EntityCollisionBit.Hero).toShort)
      upperPlatform.changeCollisions((EntityCollisionBit.Enemy | EntityCollisionBit.Hero).toShort)
      lowerPlatform.changeCollisions((EntityCollisionBit.Enemy | EntityCollisionBit.Hero).toShort)
      println("Enabled platform collisions 2")
      this.timer = 0
    }
  }
}

case class ChestCollisionStrategy(private val entitiesSetter: EntitiesSetter,
                             private val chest: ImmobileEntity) extends CollisionStrategyImpl {
  override def contact(entity: Entity): Unit = entity match {
    case h: Hero =>
      println("Hero touches chest")
      this.entitiesSetter.addMessage("Press F to open the chest")
      h.setEnvironmentInteraction(Option.apply(HeroInteraction(GameEvent.Interaction, new ChestInteraction(h,chest))))
    case _ =>
  }

  override def release(entity: Entity): Unit = entity match {
    case h:Hero =>
      println("Hero not touching chest anymore")
      h.setEnvironmentInteraction(Option.empty)
    case _ =>
  }

}

case class PortalCollisionStrategy(private val portal: ImmobileEntity, private val level: Level) extends CollisionStrategyImpl {
  override def contact(entity: Entity): Unit = entity match {
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

case class LadderCollisionStrategy(private val monitor: CollisionMonitor) extends CollisionStrategyImpl {
  override def contact(entity: Entity): Unit = entity match {
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

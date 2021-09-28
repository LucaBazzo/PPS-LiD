package model.entity.collision

import controller.{EntitiesSetter, GameEvent}
import model.entity.{Entity, Hero, ImmobileEntity, Item, _}
import model._
import utils.EnvironmentConstants._

/** Represent the behaviour of Items when collide
 *
 * @param item the Item that is colliding
 * @param entitiesMonitor the monitor used to propagate item description and item picking event
 */
case class ItemCollisionStrategy(private val item: Item,
                            private val entitiesMonitor: EntitiesSetter) extends CollisionStrategy {
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
      case _ => println("____")
    }
  }
}

/** Represent the behaviour of Doors when collide
 *
 * @param entitySetter used to show the info of which button press to open the door
 * @param door the entity that represent the door
 * @param doorSensorLeft the sensor at door's left
 * @param doorSensorRight the sensor at door's right
 */
case class DoorCollisionStrategy(private val entitySetter: EntitiesSetter,
                            private val door: ImmobileEntity,
                            private val doorSensorLeft: ImmobileEntity,
                            private val doorSensorRight: ImmobileEntity) extends CollisionStrategy {
  override def contact(entity: Entity): Unit = entity match {
    case h: Hero =>
      this.entitySetter.addMessage("Press Space to open the door")
      h.setEnvironmentInteraction(Option.apply(HeroInteraction(GameEvent.Interaction, new DoorInteraction(h, this.door,
        doorSensorLeft, doorSensorRight))))
    case m: MobileEntity if m.getEntityBody.getEntityCollisionBit() == EntityCollisionBit.Sword =>
      println("Hero destroyed door")
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

/** Represent the behaviour of Boss Door when collide
 *
 * @param entitySetter used to show the info of which button press to open the door
 * @param door the entity that represent the door
 * @param doorSensorLeft the sensor at door's left
 * @param doorSensorRight the sensor at door's right
 */
case class BossDoorCollisionStrategy(private val entitySetter: EntitiesSetter,
                                private val door: ImmobileEntity,
                                private val doorSensorLeft: ImmobileEntity,
                                private val doorSensorRight: ImmobileEntity) extends CollisionStrategy {

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

/** Represent the behaviour of water pool when collide, slow hero
 *
 */
case class WaterCollisionStrategy() extends CollisionStrategy {
  override def contact(entity: Entity): Unit = entity match {
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

/** Represent the behaviour of lava pool when collide, damaging the hero
 *
 */
case class LavaCollisionStrategy() extends CollisionStrategy {
  private var timer:Long = 0
  private var hero:Option[Hero] = Option.empty

  override def contact(entity: Entity): Unit = entity match {
    case h: Hero =>
      println("Hero stands in lava")
      h.alterStatistics(Statistic.MovementSpeed, -LAVA_SPEED_ALTERATION)
      this.hero = Option(h)
    case _ =>
  }

  override def release(entity: Entity): Unit = entity match {
    case h: Hero => println("Hero out of lava")
      this.hero = Option.empty
      h.alterStatistics(Statistic.MovementSpeed, +LAVA_SPEED_ALTERATION)
    case _ =>
  }

  override def apply(): Unit = {
    val now:Long = System.currentTimeMillis()
    if (hero.isDefined && System.currentTimeMillis() - this.timer > LAVA_DAMAGE_TICK_RATE) {
      hero.get.sufferDamage(LAVA_DAMAGE_PER_TICK)
      println("Taken damage from lava")
      /*if(this.hero.get.getLife <= 0)
        collisMonitor.playerOutOfLava()*/
      this.timer = now
    }
  }
}

/** Represent the behaviour of the solid part of platform when collide, enable pass through interaction
 *
 * @param platform the platform on which hero can stand
 * @param monitor the collision monitor that tracks hero's interactions with world elements
 */
case class UpperPlatformCollisionStrategy(private val platform: ImmobileEntity,
                                          private val monitor: CollisionMonitor) extends CollisionStrategy {

  override def contact(entity: Entity): Unit = entity match {
    case h: Hero =>
      println("Hero standing on Platform")
      this.monitor.playerTouchesPlatformEdge()
      h.setEnvironmentInteraction(Option.apply(HeroInteraction(GameEvent.Interaction, new PlatformInteraction(h,
        this.platform, this.monitor))))
    case _ =>
  }

  override def release(entity: Entity): Unit = entity match {
    case _: Hero =>
      println("Hero leaving Platform")
      if(!this.monitor.isPlayerTouchingPlatformEdges) {
        platform.changeCollisions((EntityCollisionBit.Enemy | EntityCollisionBit.Hero).toShort)
        println("Enabled platform collisions")
      }
      this.monitor.playerQuitPlatform()
    case _ =>
  }
}

/** Represent the behaviour of the solid part of platform when collide, enable pass through interaction
 *
 * @param platform the platform on which hero can stand
 * @param monitor the collision monitor that tracks hero's interactions with world elements
 */
case class LowerPlatformCollisionStrategy(private val platform: ImmobileEntity,
                                          private val monitor: CollisionMonitor) extends CollisionStrategy {

  override def contact(entity: Entity): Unit = entity match {
    case _: Hero => 
      println("Hero touching lower Platform")
      this.monitor.playerTouchesPlatformEdge()
      platform.changeCollisions(EntityCollisionBit.Enemy)
    case _ =>
  }

  override def release(entity: Entity): Unit = entity match {
    case hero: Hero =>
      println("Hero leaving Platform")
      this.platformReleaseCollision(hero)
    case _ =>

  }

  private def platformReleaseCollision(hero: Entity): Unit = {
    if(! this.monitor.isPlayerTouchingPlatformEdges && hero.getState == State.Falling) {
      platform.changeCollisions((EntityCollisionBit.Enemy | EntityCollisionBit.Hero).toShort)
      println("Enabled platform collisions")
    }
    this.monitor.playerQuitPlatform()
  }
}

/** Represent the behaviour of Chest when collide, enable open chest interaction
 *
 * @param entitiesSetter used to show the info of which button press to open the chest
 * @param chest the entity that represent the chest
 */
case class ChestCollisionStrategy(private val entitiesSetter: EntitiesSetter,
                             private val chest: ImmobileEntity) extends CollisionStrategy {
  override def contact(entity: Entity): Unit = entity match {
    case h: Hero =>
      println("Hero touches chest")
      this.entitiesSetter.addMessage("Press Space to open the chest")
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

/** Represent the behaviour of portal when collide, send hero to the next level
 *
 * @param portal the portal entity
 */
case class PortalCollisionStrategy(private val portal: ImmobileEntity,
                                   private val level: Level) extends CollisionStrategy {
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

/** Represent the behaviour of ladders when collide, enable climbing
 *
 * @param monitor the collision monitor that tracks hero's interactions with world elements
 */
case class LadderCollisionStrategy(private val monitor: CollisionMonitor) extends CollisionStrategy {
  override def contact(entity: Entity): Unit = entity match {
    case h: Hero => print("Hero touches ladder" + "\n")
      monitor.playerOnLadder()
      h.setEnvironmentInteraction(Option.apply(HeroInteraction(GameEvent.Interaction, new LadderInteraction(h))))
    case _ =>
  }

  override def release(entity: Entity): Unit = entity match {
    case h:Hero => print("Hero leaving ladder" + "\n")
      monitor.playerQuitLadder()
      if(! monitor.isPlayerTouchingPlatformEdges)
        h.setEnvironmentInteraction(Option.empty)
    case _ =>
  }
}
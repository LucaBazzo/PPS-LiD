package utils

import model.entity.collision.EntityCollisionBit
import model.helpers.ImplicitConversions._

object CollisionConstants {

  val HERO_COLLISIONS: Short = EntityCollisionBit.Immobile | EntityCollisionBit.Ladder |
    EntityCollisionBit.Pool | EntityCollisionBit.Item | EntityCollisionBit.Portal |
    EntityCollisionBit.Door | EntityCollisionBit.EnemyAttack |
    EntityCollisionBit.Platform | EntityCollisionBit.PlatformSensor

  val HERO_FEET_COLLISIONS: Short = EntityCollisionBit.Immobile |
    EntityCollisionBit.Platform | EntityCollisionBit.PlatformSensor | EntityCollisionBit.Door

  val ENEMY_COLLISIONS: Short = EntityCollisionBit.Immobile | EntityCollisionBit.Platform  | EntityCollisionBit.Sword | EntityCollisionBit.Arrow |
    EntityCollisionBit.Door

  val PLATFORM_COLLISIONS: Short = EntityCollisionBit.Hero | EntityCollisionBit.Enemy

  val PLATFORM_SENSOR_COLLISIONS: Short = EntityCollisionBit.Hero

  val LADDER_COLLISIONS: Short = EntityCollisionBit.Hero | EntityCollisionBit.Enemy

  val ITEM_COLLISIONS: Short = EntityCollisionBit.Hero | EntityCollisionBit.Immobile

  val IMMOBILE_COLLISIONS: Short = EntityCollisionBit.Hero | EntityCollisionBit.Enemy |
    EntityCollisionBit.Arrow | EntityCollisionBit.EnemyAttack | EntityCollisionBit.Item

  val DOOR_COLLISIONS: Short = EntityCollisionBit.Hero | EntityCollisionBit.Sword | EntityCollisionBit.Arrow

  val CHEST_COLLISIONS: Short = EntityCollisionBit.Hero | EntityCollisionBit.Enemy

  val PORTAL_COLLISIONS: Short = EntityCollisionBit.Hero | EntityCollisionBit.Sword

  val WATER_LAVA_COLLISIONS: Short = EntityCollisionBit.Hero

  val SWORD_COLLISIONS: Short = EntityCollisionBit.Enemy | EntityCollisionBit.Door

  val NO_COLLISIONS: Short = 0

  val FIREBALL_COLLISIONS: Short = EntityCollisionBit.Immobile | EntityCollisionBit.Hero | EntityCollisionBit.Door

  val ENERGY_BALL_COLLISIONS: Short = EntityCollisionBit.Hero | EntityCollisionBit.Sword

  val ENEMY_MELEE_ATTACK_COLLISIONS: Short = EntityCollisionBit.Hero

  val ARROW_COLLISIONS: Short = EntityCollisionBit.Immobile | EntityCollisionBit.Enemy

}

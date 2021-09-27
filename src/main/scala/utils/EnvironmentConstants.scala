package utils

import model.collisions.EntityCollisionBit

object EnvironmentConstants {

  val DEFAULT_DOOR_SIZE: (Float, Float) = (10, 10)
  val DEFAULT_DOOR_POSITION: (Float, Float) = (0, 0)

  val OPEN_DOOR_COLLISION_BIT: Short = EntityCollisionBit.OpenedDoor
  val OPEN_CHEST_COLLISION_BIT: Short = EntityCollisionBit.OpenedDoor

  val DEFAULT_CHEST_SIZE: (Float, Float) = (70, 70)
  val DEFAULT_CHEST_POSITION: (Float, Float) = (0, 0)

  val DEFAULT_PORTAL_SIZE: (Float, Float) = (10,30)
  val DEFAULT_PORTAL_POSITION: (Float, Float) = (0, 0)

  val PLATFORM_SENSOR_SIZE_X_OFFSET: Float = -2
  val UPPER_PLATFORM_SENSOR_POSITION_Y_OFFSET: Float = 1
  val LOWER_PLATFORM_SENSOR_POSITION_Y_OFFSET: Float = -5
  val SIDE_PLATFORM_SENSOR_SIDE_Y_OFFSET: Float = 1
  val SIDE_PLATFORM_SENSOR_POSITION_X_OFFSET: Float = 1
  val SIDE_PLATFORM_SENSOR_POSITION_Y_OFFSET: Float = -2

  val THROUGH_PLATFORM_COLLISION_BIT: Short = EntityCollisionBit.Enemy

  val WATER_SPEED_ALTERATION: Float = 0.3f

  val LAVA_DAMAGE_PER_TICK: Int = 100
  val LAVA_DAMAGE_TICK_RATE: Int = 1000
  val LAVA_SPEED_ALTERATION: Float = 0.2f
}

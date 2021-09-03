package utils

object ApplicationConstants {

  val TITLE = "Lost in Dungeons"

  val WIDTH_SCREEN: Int = 400
  val HEIGHT_SCREEN: Int = 208

  val PIXELS_PER_METER: Float = 50

  val TIME_STEP: Float = 1 / 60f
  val VELOCITY_ITERATIONS: Int = 8
  val POSITION_ITERATIONS: Int = 3

  val GRAVITY_FORCE: (Float, Float) = (0f, -5f)

  val GAME_LOOP_STEP = 16666666

  val SPRITES_PACK_LOCATION = "assets/sprites/sprites.pack"

  val HERO_SIZE: (Float, Float) = (8.5f, 14.9f)

  val HERO_SIZE_SMALL: (Float, Float) = (8.5f, 9f)
}

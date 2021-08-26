package utils

object ApplicationConstants {

  val TITLE = "Lost in Dungeons"

  val WIDTH_SCREEN: Int = 400
  val HEIGHT_SCREEN: Int = 208

  val PIXELS_PER_METER: Float = 50

  val TIME_STEP: Float = 1 / 60f
  val VELOCITY_ITERATIONS: Int = 6
  val POSITION_ITERATIONS: Int = 2

  val GRAVITY_FORCE: (Float, Float) = (0f, -5f)

  val GAME_LOOP_STEP = 16666666

  val SPRITES_PACK_LOCATION = "assets/sprites/sprites.pack"

  val HERO_SIZE: (Float, Float) = (8.5f, 15f)
  val HERO_POSITION: (Float, Float) = (200f, 300f)

  val HERO_SIZE_SMALL: (Float, Float) = (8.5f, 8f)
  val HERO_POSITION_SMALL: (Float, Float) = (0,-6f)

}

package utils

import com.badlogic.gdx.math.Vector2

object ApplicationConstants {

  val TITLE = "Lost in Dungeons"

  val WIDTH_SCREEN: Int = 400
  val HEIGHT_SCREEN: Int = 208

  //val PIXEL_PER_METER: Float = 32.8f

  val TIME_STEP: Float = 1 / 60f
  val VELOCITY_ITERATIONS: Int = 6
  val POSITION_ITERATIONS: Int = 2

  val GRAVITY_FORCE = new Vector2(0, -10)

  val GAME_LOOP_STEP = 16666666

  val SPRITES_PACK_LOCATION = "assets/sprites/Hero.pack"

  val HERO_SIZE: (Float, Float) = (0.85f, 1.4f)

}

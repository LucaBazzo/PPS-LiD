package utils

import model.collisions.ImplicitConversions._

object HeroConstants {

  val HERO_SIZE: (Float, Float) = (8.5f, 14.9f)
  val HERO_SIZE_SMALL: (Float, Float) = (8.5f, 9f)

  val CROUCH_OFFSET: (Float, Float) = (0f, -20f)
  val CROUCH_END_OFFSET: (Float, Float) = (0f, 6f)
  val SLIDE_OFFSET: (Float, Float) = (0f, -20f)

  val RUN_VELOCITY: Float = 60f.PPM
  val JUMP_VELOCITY: Float = 150f.PPM
  val SLIDE_VELOCITY: Float = 150f.PPM

}

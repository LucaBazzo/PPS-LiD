package utils

import model.collisions.ImplicitConversions._
import model.entities.Statistic
import model.entities.Statistic.Statistic

object HeroConstants {

  val HERO_STATISTICS_DEFAULT: Map[Statistic, Float] = Map(
    Statistic.Health -> 1000,
    Statistic.CurrentHealth -> 1000,
    Statistic.Strength -> 2000,
    Statistic.MovementSpeed -> 1,
    Statistic.Defence -> 0)

  val HERO_SIZE: (Float, Float) = (8.5f, 14.9f)
  val HERO_SIZE_SMALL: (Float, Float) = (8.5f, 6f)
  val HERO_OFFSET: (Float, Float) = (30f, 45)

  val CROUCH_OFFSET: (Float, Float) = (0f, -30f)
  val CROUCH_END_OFFSET: (Float, Float) = (0f, 6f)
  val SLIDE_OFFSET: (Float, Float) = (0f, -30f)

  val RUN_VELOCITY: Float = 60f.PPM
  val JUMP_VELOCITY: Float = 175f.PPM
  val SLIDE_VELOCITY: Float = 200f.PPM
  val AIR_DOWN_ATTACK_VELOCITY: Float = 300f.PPM

  val LADDER_CLIMB_VELOCITY: Float = 60f.PPM

  val WAIT_TIME_DECREMENT: Int = 10
  val SHORT_WAIT_TIME: Int = 150
  val LONG_WAIT_TIME: Int = 150

  val WAIT_FOR_ANOTHER_CONSECUTIVE_ATTACK: Int = 75

  val ATTACK_STRATEGY_TIMER_DECREMENT: Int = 3

  val FIRST_SWORD_ATTACK_SIZE: (Float, Float) = (1f, 10f)
  val FIRST_SWORD_ATTACK_OFFSET: (Float, Float) = (0, -15f)
  val FIRST_SWORD_ATTACK_ANGULAR_VELOCITY: Float = 60
  val FIRST_SWORD_ATTACK_STARTING_ANGLE: Float = 100
  val FIRST_SWORD_ATTACK_DURATION: Int = 100

  val SECOND_SWORD_ATTACK_SIZE: (Float, Float) = FIRST_SWORD_ATTACK_SIZE
  val SECOND_SWORD_ATTACK_OFFSET: (Float, Float) = (0, 15f)
  val SECOND_SWORD_ATTACK_ANGULAR_VELOCITY: Float = -60
  val SECOND_SWORD_ATTACK_STARTING_ANGLE: Float = 10
  val SECOND_SWORD_ATTACK_DURATION: Int = 130

  val THIRD_SWORD_ATTACK_SIZE: (Float, Float) = (10f, 2f)
  val THIRD_SWORD_ATTACK_OFFSET: (Float, Float) =  (15f, 0)
  val THIRD_SWORD_ATTACK_ANGULAR_VELOCITY: Float = -80
  val THIRD_SWORD_ATTACK_DURATION: Int = 150

  val THIRD_SWORD_STARTING_TIME: Int = 120
  val THIRD_SWORD_STOP_TIME: Int = 60

  val BOW_ATTACK_DURATION: Int = 175
  val BOW_ATTACK_STARTING_TIME: Int = 20

  val AIR_SWORD_ATTACK_SIZE: (Float, Float) = (12f, 3f)
  val AIR_SWORD_ATTACK_OFFSET: (Float, Float) = (0, -10f)

}

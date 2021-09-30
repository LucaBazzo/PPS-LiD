package utils

import model.entity.Items.Items
import model.helpers.ImplicitConversions._
import model.entity.Statistic
import model.entity.Statistic.Statistic

object HeroConstants {

  val HERO_STATISTICS_DEFAULT: Map[Statistic, Float] = Map(
    Statistic.Health -> 1000,
    Statistic.CurrentHealth -> 1000,
    Statistic.Strength -> 50,
    Statistic.MovementSpeed -> 1,
    Statistic.Defence -> 0)

  val HERO_STARTING_ITEMS: List[Items] = List.empty

  val HERO_SIZE: (Float, Float) = (8.5f, 14.9f)
  val HERO_SIZE_SMALL: (Float, Float) = (8.5f, 6f)
  val FEET_SIZE: (Float, Float) = (8.0f, 0.1f)

  //boss room: (2658.0f,90.0f) (2258.0f,90.0f)
  //top item room: (1250f, 900f)
  //bottom item room: (1250f, -600)
  val HERO_POSITION: (Float, Float) = (2658.0f,90.0f)// (30f, 45)

  val HERO_FRICTION: Float = 1.2f

  val RUN_VELOCITY: Float = 60f.PPM
  val JUMP_VELOCITY: Float = 175f.PPM
  val SLIDE_VELOCITY: Float = 120f.PPM
  val AIR_DOWN_ATTACK_VELOCITY: Float = 300f.PPM

  val LADDER_CLIMB_VELOCITY: Float = 70f.PPM

  val WAIT_TIME_DECREMENT: Int = 10
  val SHORT_WAIT_TIME: Int = 150
  val LONG_WAIT_TIME: Int = 150

  val WAIT_FOR_ANOTHER_CONSECUTIVE_ATTACK: Int = 75

  val ATTACK_STRATEGY_TIMER_DECREMENT: Int = 3

  val SWORD_ATTACK_DENSITY: Float = 1f

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
  val ARROW_SIZE: (Float, Float) = (8, 1)
  val ARROW_VELOCITY: Float = 240

  val AIR_SWORD_ATTACK_SIZE: (Float, Float) = (12f, 3f)
  val AIR_SWORD_ATTACK_OFFSET: (Float, Float) = (0, -10f)

  val PIVOT_SIZE: (Float, Float) = (2f, 2f)

}

package utils

import model.collisions.ImplicitConversions.RichFloat
import model.entities.Statistic
import model.entities.Statistic.Statistic

object EnemiesConstants {
  val SKELETON_STATS:Map[Statistic, Float] = Map(
    Statistic.Strength -> 100f,
    Statistic.Health -> 11f,
    Statistic.CurrentHealth -> 11f,
    Statistic.Defence -> 0f,

    Statistic.MaxMovementSpeed -> 40f.PPM,
    Statistic.Acceleration -> 5f.PPM,

    Statistic.VisionDistance -> 40.PPM,
    Statistic.VisionAngle -> 30,
    Statistic.AttackFrequency -> 4000,
    Statistic.AttackDuration -> 1000)

  val WORM_STATS:Map[Statistic, Float] = Map(
    Statistic.Strength -> 5f,
    Statistic.Health -> 11f,
    Statistic.CurrentHealth -> 11f,
    Statistic.Defence -> 0f,

    Statistic.MaxMovementSpeed -> 40f.PPM,
    Statistic.Acceleration -> 5f.PPM,

    Statistic.VisionDistance -> 100.PPM,
    Statistic.VisionAngle -> 70,
    Statistic.AttackFrequency -> 1500,
    Statistic.AttackDuration -> 900
  )

  val SLIME_STATS:Map[Statistic, Float] = Map(
    Statistic.Strength -> 10f,
    Statistic.Health -> 11f,
    Statistic.CurrentHealth -> 11f,
    Statistic.Defence -> 0f,

    Statistic.MaxMovementSpeed -> 40f.PPM,
    Statistic.Acceleration -> 5f.PPM,

    Statistic.VisionDistance -> 20.PPM,
    Statistic.VisionAngle -> 20,
    Statistic.AttackFrequency -> 2000,
    Statistic.AttackDuration -> 1000,
  )

  val WIZARD_BOSS_STATS:Map[Statistic, Float] = Map(
    Statistic.Strength -> 10f,
    Statistic.Health -> 100f,
    Statistic.CurrentHealth -> 100f,
    Statistic.Defence -> 0f,

    Statistic.MaxMovementSpeed -> 40f.PPM,
    Statistic.Acceleration -> 5f.PPM,

    Statistic.VisionDistance -> 70.PPM,
    Statistic.VisionAngle -> 90,
    Statistic.AttackFrequency -> 2000,
    Statistic.AttackDuration -> 1000,
  )

}

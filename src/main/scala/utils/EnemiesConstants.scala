package utils

import model.collisions.ImplicitConversions.RichFloat
import model.entities.EntityType.EntityType
import model.entities.{EntityType, Statistic}
import model.entities.Statistic.Statistic

object EnemiesConstants {

  val ENEMIES_SPAWN_RATIO = 25 // spawn zone width / ENEMIES_SPAWN_RATIO
  val ENEMIES_ACTIVATION_DISTANCE: Float = 300.PPM
  val ENEMIES_DROP_RATE = 0.2f

  val DYING_STATE_TIME:Long = 1000

  val ENEMY_TYPES: List[EntityType] =
    List(EntityType.EnemySkeleton, EntityType.EnemyWorm, EntityType.EnemySlime)

  val ENEMY_BOSS_TYPES: List[EntityType] =
    List(EntityType.EnemyBossWizard) // EntityType.EnemyBossReaper

  val SKELETON_STATS: Map[Statistic, Float] = Map(
    Statistic.Strength -> 70f,
    Statistic.Health -> 200f,
    Statistic.CurrentHealth -> 200f,
    Statistic.Defence -> 0f,

    Statistic.MaxMovementSpeed -> 40f.PPM,
    Statistic.Acceleration -> 5f.PPM,

    Statistic.VisionDistance -> 40.PPM,
    Statistic.VisionAngle -> 30,
    Statistic.AttackFrequency -> 2500,
    Statistic.AttackDuration -> 1000)

  val WORM_STATS: Map[Statistic, Float] = Map(
    Statistic.Strength -> 30f,
    Statistic.Health -> 150f,
    Statistic.CurrentHealth -> 150f,
    Statistic.Defence -> 0f,

    Statistic.MaxMovementSpeed -> 40f.PPM,
    Statistic.Acceleration -> 5f.PPM,

    Statistic.VisionDistance -> 100.PPM,
    Statistic.VisionAngle -> 70,
    Statistic.AttackFrequency -> 1500,
    Statistic.AttackDuration -> 900
  )

  val SLIME_STATS: Map[Statistic, Float] = Map(
    Statistic.Strength -> 80f,
    Statistic.Health -> 300f,
    Statistic.CurrentHealth -> 300f,
    Statistic.Defence -> 0f,

    Statistic.MaxMovementSpeed -> 40f.PPM,
    Statistic.Acceleration -> 5f.PPM,

    Statistic.VisionDistance -> 20.PPM,
    Statistic.VisionAngle -> 20,
    Statistic.AttackFrequency -> 2000,
    Statistic.AttackDuration -> 1000,
  )

  val WIZARD_BOSS_STATS: Map[Statistic, Float] = Map(
    Statistic.Strength -> 10f,
    Statistic.Health -> 100f,
    Statistic.CurrentHealth -> 100f,
    Statistic.Defence -> 0f,

    Statistic.MaxMovementSpeed -> 40f.PPM,
    Statistic.Acceleration -> 5f.PPM,

    Statistic.VisionDistance -> 200.PPM,
    Statistic.VisionAngle -> 90,
    Statistic.AttackFrequency -> 2000,
    Statistic.AttackDuration -> 1000,
  )

  val STATS_MODIFIER: Map[Statistic, Float] = Map(
    Statistic.Strength -> 1f,
    Statistic.Health -> 5f,
    Statistic.CurrentHealth -> 5f,
    Statistic.Defence -> 1f,
    Statistic.MaxMovementSpeed -> 1f.PPM
  )

  val WIZARD_BOSS_SIZE: (Float, Float) = (13f, 25f)
  val WIZARD_BOSS_SCORE: Int = 1000
  val WIZARD_BOSS_ATTACK1_SIZE: (Float, Float) = (40, 50)
  val WIZARD_BOSS_ATTACK1_OFFSET: (Float, Float) = (55, 30)
  val WIZARD_BOSS_ATTACK2_SIZE: (Float, Float) = (50, 60)
  val WIZARD_BOSS_ATTACK2_OFFSET: (Float, Float) = (60, 25)
  val WIZARD_BOSS_ATTACK3_SIZE: (Float, Float) = (10, 10)
  val WIZARD_BOSS_ATTACK3_OFFSET: (Float, Float) = (13, 25)
  val WIZARD_BOSS_ATTACK3_DISTANCE: Float = 100

  val SLIME_SIZE: (Float, Float) = (23f, 12f)
  val SLIME_SCORE: Int = 100
  val SLIME_ATTACK_SIZE: (Float, Float) = (7f, 15f)
  val SLIME_ATTACK_OFFSET: (Float, Float) = (10f, 5f)

  val SKELETON_SIZE: (Float, Float) = (13f, 23f)
  val SKELETON_SCORE: Int = 100
  val SKELETON_ATTACK_SIZE: (Float, Float) = (23f, 23f)
  val SKELETON_ATTACK_OFFSET: (Float, Float) = (20f, 5f)

  val WORM_SIZE: (Float, Float) = (19f, 12f)
  val WORM_SCORE: Int = 100
  val WORM_FIREBALL_ATTACK_OFFSET: (Float, Float) = (10f, 10f)
  val WORM_FIREBALL_ATTACK_SIZE: (Float, Float) = (7f, 7f)

}

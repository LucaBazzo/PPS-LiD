package utils

import model.collisions.ImplicitConversions.RichFloat
import model.entities.EntityType.EntityType
import model.entities.Statistic.Statistic
import model.entities.Statistic._
import model.entities.EntityType

object EnemiesConstants {
  val ENEMIES_SPAWN_RATIO = 25 // spawn zone width / ENEMIES_SPAWN_RATIO
  val ENEMIES_ACTIVATION_DISTANCE: Float = 300.PPM
  val ENEMIES_DROP_RATE = 0.2f
  val DYING_STATE_TIME:Long = 1000

  val ENEMY_SCORE: Int = 100
  val BOSS_SCORE: Int = 100

  val ENEMY_TYPES: List[EntityType] =
    List(EntityType.EnemySkeleton, EntityType.EnemyWorm, EntityType.EnemySlime)

  val ENEMY_BOSS_TYPES: List[EntityType] =
    List(EntityType.EnemyBossWizard)

  val SKELETON_STATS: Map[Statistic, Float] = Map(
    Strength -> 70f,
    Health -> 200f,
    CurrentHealth -> 200f,
    Defence -> 0f,

    MaxMovementSpeed -> 40f.PPM,
    Acceleration -> 5f.PPM,

    VisionDistance -> 40.PPM,
    VisionAngle -> 30,
    AttackFrequency -> 2500,
    AttackDuration -> 1000)

  val WORM_STATS: Map[Statistic, Float] = Map(
    Strength -> 30f,
    Health -> 150f,
    CurrentHealth -> 150f,
    Defence -> 0f,

    MaxMovementSpeed -> 40f.PPM,
    Acceleration -> 5f.PPM,

    VisionDistance -> 100.PPM,
    VisionAngle -> 70,
    AttackFrequency -> 1500,
    AttackDuration -> 900
  )

  val SLIME_STATS: Map[Statistic, Float] = Map(
    Strength -> 80f,
    Health -> 300f,
    CurrentHealth -> 300f,
    Defence -> 0f,

    MaxMovementSpeed -> 30f.PPM,
    Acceleration -> 1f.PPM,

    VisionDistance -> 20.PPM,
    VisionAngle -> 20,
    AttackFrequency -> 2000,
    AttackDuration -> 1000,
  )

  val WIZARD_BOSS_STATS: Map[Statistic, Float] = Map(
    Strength -> 10f,
    Health -> 500f,
    CurrentHealth -> 500f,
    Defence -> 0f,

    MaxMovementSpeed -> 40f.PPM,
    Acceleration -> 5f.PPM,

    VisionDistance -> 200.PPM,
    VisionAngle -> 90,
    AttackFrequency -> 2000,
    AttackDuration -> 1000
  )

  val STATS_MODIFIER: Map[Statistic, Float] = Map(
    Strength -> 1f,
    Health -> 5f,
    CurrentHealth -> 5f,
    Defence -> 1f,
    MaxMovementSpeed -> 1f.PPM
  )

  val WIZARD_BOSS_SIZE: (Float, Float) = (13f, 25f)
  val WIZARD_BOSS_ATTACK1_SIZE: (Float, Float) = (40, 50)
  val WIZARD_BOSS_ATTACK1_OFFSET: (Float, Float) = (55, 30)
  val WIZARD_BOSS_ATTACK2_SIZE: (Float, Float) = (50, 60)
  val WIZARD_BOSS_ATTACK2_OFFSET: (Float, Float) = (60, 25)
  val WIZARD_BOSS_ATTACK3_SIZE: (Float, Float) = (10, 10)
  val WIZARD_BOSS_ATTACK3_OFFSET: (Float, Float) = (13, 25)
  val WIZARD_BOSS_ATTACK3_DISTANCE: Float = 100
  val WIZARD_BOSS_ATTACK3_SPEED: Float = 2

  val SLIME_SIZE: (Float, Float) = (23f, 12f)
  val SLIME_ATTACK_SIZE: (Float, Float) = (9f, 18f)
  val SLIME_ATTACK_OFFSET: (Float, Float) = (23f, 5f)
  val SLIME_ATTACK_SPEED: Float = 3

  val SKELETON_SIZE: (Float, Float) = (13f, 23f)
  val SKELETON_ATTACK_SIZE: (Float, Float) = (28f, 21f)
  val SKELETON_ATTACK_OFFSET: (Float, Float) = (25f, 5f)

  val WORM_SIZE: (Float, Float) = (19f, 12f)
  val WORM_ATTACK_OFFSET: (Float, Float) = (10f, 10f)
  val WORM_ATTACK_SIZE: (Float, Float) = (7f, 7f)
  val WORM_ATTACK_SPEED: Float = 5f
}

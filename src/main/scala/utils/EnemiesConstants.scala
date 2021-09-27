package utils

import model.helpers.ImplicitConversions.RichFloat
import model.entities.EntityType
import model.entities.EntityType.EntityType
import model.entities.Statistic.{Statistic, _}

object EnemiesConstants {
  val ENEMIES_DROP_RATE = 0.3f
  val PACMAN_SPAWN_RATE = 0.01

  val ENEMY_SCORE: Int = 100
  val BOSS_SCORE: Int = 1000

  val PROJECTILE_ENTITIES_DURATION: Int = 5000
  val PROJECTILE_DYING_STATE_DURATION: Int = 500
  val ENEMIES_DYING_STATE_DURATION:Long = 1000

  val ENEMY_TYPES: List[EntityType] = List(EntityType.EnemySkeleton, EntityType.EnemyWorm, EntityType.EnemySlime, EntityType.EnemyBat)
  val ENEMY_BOSS_TYPES: List[EntityType] = List(EntityType.EnemyBossWizard)

  val SKELETON_STATS: Map[Statistic, Float] = Map(
    Strength -> 60f,
    Health -> 200f,
    CurrentHealth -> 200f,
    Defence -> 0f,
    MovementSpeed -> 40f.PPM,
  )

  val WORM_STATS: Map[Statistic, Float] = Map(
    Strength -> 50f,
    Health -> 150f,
    CurrentHealth -> 150f,
    Defence -> 0f,
    MovementSpeed -> 40f.PPM,
  )

  val SLIME_STATS: Map[Statistic, Float] = Map(
    Strength -> 60f,
    Health -> 300f,
    CurrentHealth -> 300f,
    Defence -> 0f,
    MovementSpeed -> 30f.PPM,
  )

  val BAT_STATS: Map[Statistic, Float] = Map(
    Strength -> 50f,
    Health -> 200f,
    CurrentHealth -> 200f,
    Defence -> 0f,
    MovementSpeed -> 20f.PPM,
  )

  val WIZARD_BOSS_STATS: Map[Statistic, Float] = Map(
    Strength -> 80f,
    Health -> 500f,
    CurrentHealth -> 500f,
    Defence -> 0f,
    MovementSpeed -> 40f.PPM,
  )

  val STATS_MODIFIER: Map[Statistic, Float] = Map(
    Strength -> 1f,
    Health -> 5f,
    CurrentHealth -> 5f,
    Defence -> 1f,
    MovementSpeed -> 1f.PPM
  )

  val SKELETON_SIZE: (Float, Float) = (13f, 23f)
  val SKELETON_ATTACK_SIZE: (Float, Float) = (28f, 21f)
  val SKELETON_ATTACK_OFFSET: (Float, Float) = (25f, 5f)
  val SKELETON_ATTACK_SPEED: Long = 3000
  val SKELETON_ATTACK_DURATION: Long = 1200
  val SKELETON_VISION_DISTANCE: Float = 40.PPM
  val SKELETON_VISION_ANGLE: Int = 30

  val WORM_SIZE: (Float, Float) = (19f, 12f)
  val WORM_ATTACK_SIZE: (Float, Float) = (7f, 7f)
  val WORM_ATTACK_OFFSET: (Float, Float) = (10f, 10f)
  val WORM_ATTACK_PROJECTILE_SPEED: Float = 4f
  val WORM_ATTACK_CREATION_DELAY: Long = 1100
  val WORM_ATTACK_SPEED: Long = 3500
  val WORM_ATTACK_DURATION: Long = 1600
  val WORM_VISION_DISTANCE: Float = 150.PPM
  val WORM_VISION_ANGLE: Int = 45

  val SLIME_SIZE: (Float, Float) = (23f, 12f)
  val SLIME_ATTACK_SIZE: (Float, Float) = (15f, 18f)
  val SLIME_ATTACK_OFFSET: (Float, Float) = (23f, 5f)
  val SLIME_ATTACK_SPEED: Long = 2000
  val SLIME_ATTACK_DURATION: Long = 900
  val SLIME_VISION_DISTANCE: Float = 40.PPM
  val SLIME_VISION_ANGLE: Int = 30

  val BAT_SIZE: (Float, Float) = (15f, 15f)
  val BAT_ATTACK_SIZE: (Float, Float) = (15f, 15f)
  val BAT_ATTACK_OFFSET: (Float, Float) = (0, 0)
  val BAT_ATTACK_SPEED: Long = 1100
  val BAT_ATTACK_DURATION: Long = 1000
  val BAT_VISION_DISTANCE: Float = 200.PPM
  val BAT_VISION_ANGLE: Int = 90

  val WIZARD_BOSS_SIZE: (Float, Float) = (13f, 25f)
  val WIZARD_VISION_DISTANCE: Float = 200.PPM
  val WIZARD_VISION_ANGLE: Int = 90

  val WIZARD_ATTACK1_SIZE: (Float, Float) = (40, 50)
  val WIZARD_ATTACK1_OFFSET: (Float, Float) = (55, 30)
  val WIZARD_ATTACK1_SPEED: Long = 3000
  val WIZARD_ATTACK1_DURATION: Long = 1000

  val WIZARD_ATTACK2_SIZE: (Float, Float) = (50, 60)
  val WIZARD_ATTACK2_OFFSET: (Float, Float) = (60, 25)
  val WIZARD_ATTACK2_SPEED: Long = 4000
  val WIZARD_ATTACK2_DURATION: Long = 1100

  val WIZARD_ATTACK3_SIZE: (Float, Float) = (10, 10)
  val WIZARD_ATTACK3_OFFSET: (Float, Float) = (13, 25)
  val WIZARD_ATTACK3_PROJECTILE_SPEED: Float = 2.5f
  val WIZARD_ATTACK3_SPEED: Long = 2000
  val WIZARD_ATTACK3_DURATION: Long = 1000
  val WIZARD_ATTACK3_HOMING_DURATION: Long = 2000
  val WIZARD_ATTACK3_PROJECTILE_DURATION: Long = 5000
}

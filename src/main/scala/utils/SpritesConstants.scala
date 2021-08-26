package utils

import model.entities.State

object SpritesConstants {

  val HERO_SPRITES_PACK_LOCATION = "assets/sprites/sprites.pack"
  val ITEMS_SPRITES_PACK_LOCATION = "assets/sprites/sprites.pack"
  val ENEMIES_SPRITES_PACK_LOCATION = "assets/sprites/skeleton.pack"

  val HERO_SPRITES_REGION_NAME = "hero"
  val ITEMS_SPRITES_REGION_NAME = "items"
  val ENEMY_SKELETON_SPRITES_REGION_NAME = "skeleton"
  val ENEMY_SLIME_SPRITES_REGION_NAME = "slime"

  val HERO_SPRITES_SIZES:(Int, Int) = (50, 37)
  val ITEMS_SPRITES_SIZES:(Int, Int) = (32, 32)
  val ENEMY_SKELETON_SPRITES_SIZES:(Int, Int) = (150, 150)
  val ENEMY_SLIME_SPRITES_SIZES:(Int, Int) = (0, 0)

  val HERO_ANIMATIONS: Map[State.Value, ((Int, Int), (Int, Int), Float)] = Map(
    State.Standing -> ((0, 0), (0, 3), 0f))
//    State.Running -> ((1, 1), (1, 6), 0f),
//    State.Jumping -> ((2, 0), (2, 3), 0f),
//    State.Falling -> ((3, 1), (3, 2), 0f),
//    State.Sliding -> ((3, 3), (3, 6), 0f),
//    State.Crouch -> ((0, 0), (0, 3), 0f),
//    State.Attack01 -> ((0, 0), (0, 3), 0f),
//    State.Attack02 -> ((0, 0), (0, 3), 0f),
//    State.Attack03 -> ((0, 0), (0, 3), 0f),
//    State.Somersault -> ((0, 0), (0, 3), 0f))
}

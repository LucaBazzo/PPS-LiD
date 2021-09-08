package utils

import scala.util.Random

object ApplicationConstants {

  val DEBUG = true

  val TITLE = "Lost in Dungeons"

  val WIDTH_SCREEN: Int = 400
  val HEIGHT_SCREEN: Int = 208

  val PIXELS_PER_METER: Float = 50

  val TIME_STEP: Float = 1 / 60f
  val VELOCITY_ITERATIONS: Int = 8
  val POSITION_ITERATIONS: Int = 3

  val GRAVITY_FORCE: (Float, Float) = (0f, -5f)

  val GAME_LOOP_STEP = 16666666

  val SPRITES_PACK_LOCATION = "assets/sprites/sprites.pack"

  val HERO_ROOM_MAP_NAME: String = "hero-room"
  val BOSS_ROOM_MAP_NAME: String = "boss-room"
  val ROOM_MAP_NAMES: Array[String] = Array("room1-final", "room2-final", "room3-final")

  val RANDOM_SEED: Int = 42
  val RANDOM: Random = new Random()

  //HUD constants
  val FONT_PATH_LABEL: String = "assets/fonts/arial.fnt"
  val HEALTH_BAR_PATH: String = "assets/textures/health_bar.png"
  val HEALTH_BORDER_PATH: String = "assets/textures/health_bar_border.png"
  val HEALTH_BAR_BOSS_PATH: String = "assets/textures/health_bar_boss.png"
  val HEALTH_BORDER_BOSS_PATH: String = "assets/textures/health_bar_border_boss.png"

  val HUD_FIRST_ROW_PADDING_TOP: Float = 10
  val HUD_FIRST_ROW_PADDING_SIDE: Float = 20
  val HUD_BOSS_HEALTH_BAR_PADDING: Float = 35
  val HUD_PADDING_TOP: Float = 45
}

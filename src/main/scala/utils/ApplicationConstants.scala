package utils

import scala.util.Random

object ApplicationConstants {

  val DEBUG = true

  val TITLE = "Lost in Dungeons"

  val ICON_PATH = "assets/sprites/lid_icon.png"

  val WIDTH_SCREEN: Int = 400
  val HEIGHT_SCREEN: Int = 208

  val PIXELS_PER_METER: Float = 50

  val TIME_STEP: Float = 1 / 60f
  val VELOCITY_ITERATIONS: Int = 8
  val POSITION_ITERATIONS: Int = 3

  val GRAVITY_FORCE: (Float, Float) = (0f, -5f)

  val GAME_LOOP_STEP = 16666666

  val RANDOM_SEED: Int = 42
  val RANDOM: Random = new Random()

  val HERO_ROOM_MAP_NAME: String = "hero-room"
  val BOSS_ROOM_MAP_NAME: String = "boss-room"
  val ROOM_MAP_NAMES: Array[String] = Array("room1-final", "room2-final", "room3-final")

  //Menu Screens Constants
  val MAIN_MENU_BACKGROUND_PATH: String = "assets/backgrounds/background_main_menu.png"
  val GAME_OVER_BACKGROUND_PATH: String = "assets/backgrounds/background_game_over.png"
  val BUTTONS_SKIN_PATH: String = "assets/buttons/buttons.pack"

  val PLAY_BUTTON_STYLE_UP: String = "play_button_inactive"
  val PLAY_BUTTON_STYLE_DOWN: String = "play_button_active"
  val EXIT_BUTTON_STYLE_UP: String = "exit_button_inactive"
  val EXIT_BUTTON_STYLE_DOWN: String = "exit_button_active"

  val DEFAULT_DISTANCE_FROM_TOP: Float = 70
  val GAME_OVER_DISTANCE_FROM_TOP: Float = 50
  val DISTANCE_BUTTONS_DEFAULT: Float = 8
  val DISTANCE_BUTTONS_GAME_OVER: Float = 28
  val BUTTONS_WIDTH: Float = 100
  val BUTTONS_HEIGHT: Float = 26
  val BUTTONS_FONT_SCALE: Float = 0.30f
  val DISTANCE_FROM_TD: Float = 3
  val DISTANCE_FROM_LR: Float = 25
  val DISTANCE_FROM_LR_NO_BTN: Float = 30

  val MAIN_MENU_PLAY_TEXT = "Play"
  val MAIN_MENU_EXIT_TEXT = "Exit"
  val GAME_OVER_MENU_YES_TEXT = "Yes"
  val GAME_OVER_MENU_NO_TEXT = "No"

  //Game Screen HUD constants
  val FONT_PATH_LABEL: String = "assets/fonts/lunch_doubly_so.fnt"

  val HEALTH_BAR_PATH: String = "assets/textures/health_bar.png"
  val HEALTH_BORDER_PATH: String = "assets/textures/health_bar_border.png"
  val HEALTH_BAR_BOSS_PATH: String = "assets/textures/health_bar_boss.png"
  val HEALTH_BORDER_BOSS_PATH: String = "assets/textures/health_bar_border_boss.png"
  val HEALTH_BAR_BOSS_VISIBILITY_DISTANCE = 200f

  val LABEL_FONT_SCALE: Float = 0.22f
  val HUD_FIRST_ROW_PADDING_TOP: Float = 10
  val HUD_FIRST_ROW_PADDING_SIDE: Float = 20
  val HUD_BOSS_HEALTH_BAR_PADDING: Float = 35
  val HUD_PADDING_TOP: Float = 45
}

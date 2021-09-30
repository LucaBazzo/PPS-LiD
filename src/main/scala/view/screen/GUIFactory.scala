package view.screen

import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.{BitmapFont, TextureAtlas}
import com.badlogic.gdx.graphics.{Color, Texture}
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.ui._
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.{Gdx, Screen}
import controller.{GameEvent, ObserverManager}
import utils.ApplicationConstants._

import java.util.Objects

trait GUIFactory {

  def createTexture(texturePath: String): Texture

  def createBitmapFont(path: String): BitmapFont

  def createLabel(text: String, bitmapFont: BitmapFont, color: Color, fontScale: Float = LABEL_FONT_SCALE): Label

  def createHealthTable(healthImage: Image, healthBorder: Image): Table

  def createImage(path: String): Image

  def createSkin(packPath: String): Skin

  def createTextButtonStyle(styleUp: String, styleDown: String, skin: Skin, bitmapFont: BitmapFont): TextButtonStyle

  def createTextButton(text: String,
                       style: TextButtonStyle,
                       clickListener: ClickListener,
                       tdPadding: Float = 0, lrPadding: Float = 0): TextButton

  def createMainMenuScreen(observerManager: ObserverManager): Screen

  def createGameOverScreen(observerManager: ObserverManager): Screen
}

object GUIFactory extends GUIFactory {

  override def createTexture(texturePath: String): Texture =
    new Texture(Objects.requireNonNull(texturePath))

  override def createBitmapFont(path: String): BitmapFont = {
    val bitmapFont: BitmapFont = new BitmapFont(Gdx.files.internal(Objects.requireNonNull(path)), false)
    bitmapFont.getRegion().getTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear)
    bitmapFont
  }

  override def createLabel(text: String, bitmapFont: BitmapFont, color: Color, fontScale: Float): Label = {
    val label = new Label(Objects.requireNonNull(text),
      new LabelStyle(Objects.requireNonNull(bitmapFont), Objects.requireNonNull(color)))
    label.setFontScale(fontScale)
    label
  }

  override def createHealthTable(healthImage: Image, healthBorder: Image): Table = {

    val diamondBox: Stack = new Stack()
    diamondBox.addActor(healthImage)
    diamondBox.addActor(healthBorder)

    val healthTable: Table = new Table()
    healthTable.add(diamondBox)

    healthTable
  }

  override def createImage(path: String): Image = new Image(new Texture(path))

  override def createSkin(packPath: String): Skin = new Skin(new TextureAtlas(Objects.requireNonNull(packPath)))

  override def createTextButtonStyle(styleUp: String, styleDown: String, skin: Skin,
    bitmapFont: BitmapFont): TextButtonStyle = {
    val style: TextButtonStyle = new TextButtonStyle()
    style.up = skin.getDrawable(Objects.requireNonNull(styleUp))
    style.down = skin.getDrawable(Objects.requireNonNull(styleDown))
    style.font = Objects.requireNonNull(bitmapFont)
    style
  }

  override def createTextButton(text: String,
                                style: TextButtonStyle,
                                clickListener: ClickListener,
                                tdPadding: Float = 0, lrPadding: Float = 0): TextButton = {
    val buttonStyle: TextButtonStyle = Objects.requireNonNull(style)
    buttonStyle.fontColor = Color.BLACK
    val button: TextButton = new TextButton(Objects.requireNonNull(text), buttonStyle)
    button.addListener(clickListener)
    button.pad(tdPadding, lrPadding, tdPadding, lrPadding)
    button.getLabel.setFontScale(BUTTONS_FONT_SCALE)
    button
  }

  override def createMainMenuScreen(observerManager: ObserverManager): Screen = {
    val mainMenu: MenuScreen = MainMenuScreen(observerManager, MAIN_MENU_BACKGROUND_PATH)

    // creating buttons
    mainMenu.addButton(createMenuButton(MAIN_MENU_PLAY_TEXT, PLAY_BUTTON_STYLE_UP, PLAY_BUTTON_STYLE_DOWN,
      defineClickListener(() => observerManager.notifyEvent(GameEvent.StartGame))))

    mainMenu.addButton(createMenuButton(MAIN_MENU_EXIT_TEXT, EXIT_BUTTON_STYLE_UP, EXIT_BUTTON_STYLE_DOWN,
      defineClickListener(() => observerManager.notifyEvent(GameEvent.CloseApplication))))

    mainMenu
  }

  override def createGameOverScreen(observerManager: ObserverManager): Screen = {
    val gameOver: MenuScreen = GameOverScreen(observerManager, GAME_OVER_BACKGROUND_PATH,
      GAME_OVER_DISTANCE_FROM_TOP, DISTANCE_BUTTONS_GAME_OVER)

    gameOver.addButton(createMenuButton(GAME_OVER_MENU_YES_TEXT, PLAY_BUTTON_STYLE_UP, PLAY_BUTTON_STYLE_DOWN,
      defineClickListener(() => observerManager.notifyEvent(GameEvent.StartGame))))

    gameOver.addButton(createMenuButton(GAME_OVER_MENU_NO_TEXT, EXIT_BUTTON_STYLE_UP, EXIT_BUTTON_STYLE_DOWN,
      defineClickListener(() => observerManager.notifyEvent(GameEvent.ReturnToMenu)),
      distanceFromLR = DISTANCE_FROM_LR_NO_BTN))

    gameOver
  }

  private def defineClickListener(f: () => Unit): ClickListener =
    new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = f.apply()
    }

  private def createMenuButton(text: String,
                               styleUp: String, styleDown: String,
                               clickListener: ClickListener,
                               distanceFromTD: Float = DISTANCE_FROM_TD,
                               distanceFromLR: Float = DISTANCE_FROM_LR): TextButton = {

    val startStyle: TextButtonStyle = this.createTextButtonStyle(styleUp, styleDown,
      GUIFactory.createSkin(BUTTONS_SKIN_PATH), GUIFactory.createBitmapFont(FONT_PATH_LABEL))

    this.createTextButton(text, startStyle, clickListener, distanceFromTD, distanceFromLR)
  }
}


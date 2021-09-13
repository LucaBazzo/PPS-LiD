package view.screens.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.{BitmapFont, TextureAtlas}
import com.badlogic.gdx.graphics.{Color, Texture}
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.ui._
import utils.ApplicationConstants.{DISTANCE_FROM_LR, DISTANCE_FROM_TD, LABEL_FONT_SCALE}

import java.util.Objects

trait GUIFactory {

  def createTexture(texturePath: String): Texture

  def createBitmapFont(path: String): BitmapFont

  def createLabel(text: String, bitmapFont: BitmapFont, color: Color, fontScale: Float = LABEL_FONT_SCALE): Label

  def createHealthTable(healthImage: Image, healthBorder: Image): Table

  def createImage(path: String): Image

  def createSkin(packPath: String): Skin

  def createTextButtonStyle(styleUp: String, styleDown: String, skin: Skin, bitmapFont: BitmapFont): TextButtonStyle

  def createTextButton(text: String, style: TextButtonStyle): TextButton
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

  override def createTextButton(text: String, style: TextButtonStyle): TextButton = {
    val buttonStyle: TextButtonStyle = Objects.requireNonNull(style)
    buttonStyle.fontColor = Color.BLACK
    val button: TextButton = new TextButton(Objects.requireNonNull(text), buttonStyle)
    if(text == "No") {
      button.pad(DISTANCE_FROM_TD,DISTANCE_FROM_LR + 5,DISTANCE_FROM_TD,DISTANCE_FROM_LR + 5)
    } else
      button.pad(DISTANCE_FROM_TD,DISTANCE_FROM_LR,DISTANCE_FROM_TD,DISTANCE_FROM_LR)
    button
  }
}


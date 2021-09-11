package view.screens.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.{Color, Texture}
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.scenes.scene2d.ui.{Image, Label, Stack, Table}

import java.util.Objects

trait GUIFactory {

  def createBitmapFont(path: String): BitmapFont

  def createLabel(text: String, bitmapFont: BitmapFont, color: Color, fontScale: Float = 0.2f): Label

  def createHealthTable(healthImage: Image, healthBorder: Image): Table

  def createImage(path: String): Image
}

object GUIFactory extends GUIFactory {

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
}


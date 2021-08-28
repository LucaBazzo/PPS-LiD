package view.screens.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle

import java.util.Objects

trait GUIFactory {

  def createBitmapFont(path: String): BitmapFont

  def createLabel(text: String, bitmapFont: BitmapFont, color: Color): Label
}

object GUIFactoryImpl extends GUIFactory {

  override def createBitmapFont(path: String): BitmapFont = {
    val bitmapFont: BitmapFont = new BitmapFont(Gdx.files.internal(Objects.requireNonNull(path)), false)
    bitmapFont.getRegion().getTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear)
    bitmapFont
  }

  override def createLabel(text: String, bitmapFont: BitmapFont, color: Color): Label = {
    new Label(Objects.requireNonNull(text),
      new LabelStyle(Objects.requireNonNull(bitmapFont), Objects.requireNonNull(color)))
  }
}
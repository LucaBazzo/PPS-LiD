package view.screens.sprites

import com.badlogic.gdx.graphics.g2d.{Animation, TextureAtlas, TextureRegion}
import com.badlogic.gdx.utils.Array

trait SpriteFactory {

  def createEntitySprite(spritesFile: String, regionName: String, width: Float, height: Float): EntitySprite

  def createSpriteAnimation(sprite: EntitySprite, rowNumber: Int,
                            startIndex: Int, endIndex: Int,
                            frameDuration: Float = 0.10f): Animation[TextureRegion]

  def createSpriteAnimationFromTwoRows(sprite: EntitySprite,
                                       rowNumber: Int, startIndex: Int, endIndex: Int,
                                       rowNumber2: Int, startIndex2: Int, endIndex2: Int,
                                       frameDuration: Float = 0.10f): Animation[TextureRegion]
}

class SpriteFactoryImpl extends SpriteFactory {

  private var offsetY: Int = 0
  private var offsetX: Int = 0

  override def createEntitySprite(spritesFile: String, regionName: String, width: Float, height: Float): EntitySprite = {
    val atlas: TextureAtlas = new TextureAtlas(spritesFile)
    val sprite = new EntitySpriteImpl()
    println(atlas.findRegion(regionName))
    sprite.setRegion(atlas.findRegion(regionName))
    this.offsetY = sprite.getRegionY - 1
    this.offsetX = sprite.getRegionX - 1

    sprite.setBounds(0, 0, width, height)
    sprite
  }

  override def createSpriteAnimation(sprite: EntitySprite, rowNumber: Int,
                                     startIndex: Int, endIndex: Int,
                                     frameDuration: Float = 0.10f): Animation[TextureRegion] = {
    //array from gdx.utils.Array
    val frames: Array[TextureRegion] = new Array[TextureRegion]()

    for(i <- startIndex to endIndex){
      frames.add(new TextureRegion(sprite.getTexture, i * sprite.getIntWidth + this.offsetX,
        sprite.getIntHeight * rowNumber + this.offsetY , sprite.getIntWidth, sprite.getIntHeight))
    }

    new Animation(frameDuration, frames)
  }

  def createSpriteAnimationFromTwoRows(sprite: EntitySprite,
                            rowNumber: Int, startIndex: Int, endIndex: Int,
                            rowNumber2: Int, startIndex2: Int, endIndex2: Int,
                            frameDuration: Float = 0.10f): Animation[TextureRegion] = {
    //array from gdx.utils.Array
    val frames: Array[TextureRegion] = new Array[TextureRegion]()

    for(i <- startIndex to endIndex){
      frames.add(new TextureRegion(sprite.getTexture, i * sprite.getIntWidth + this.offsetX ,
        sprite.getIntHeight * rowNumber + this.offsetY , sprite.getIntWidth, sprite.getIntHeight))
    }

    for(i <- startIndex2 to endIndex2){
      frames.add(new TextureRegion(sprite.getTexture, i * sprite.getIntWidth + this.offsetX ,
        sprite.getIntHeight * rowNumber2 + this.offsetY , sprite.getIntWidth, sprite.getIntHeight))
    }

    new Animation(frameDuration, frames)
  }
}
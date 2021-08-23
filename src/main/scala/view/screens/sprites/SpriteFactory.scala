package view.screens.sprites

import com.badlogic.gdx.graphics.g2d.{Animation, TextureAtlas, TextureRegion}
import com.badlogic.gdx.utils.Array
import utils.ApplicationConstants.SPRITES_PACK_LOCATION

trait SpriteFactory {

  def createEntitySprite(regionName: String, width: Float, height: Float): EntitySprite

  def createSpriteAnimation(sprite: EntitySprite, rowNumber: Int,
                            startIndex: Int, endIndex: Int,
                            frameDuration: Float = 0.10f): Animation[TextureRegion]

  def createSpriteAnimationFromTwoRows(sprite: EntitySprite,
                                       rowNumber: Int, startIndex: Int, endIndex: Int,
                                       rowNumber2: Int, startIndex2: Int, endIndex2: Int,
                                       frameDuration: Float = 0.10f): Animation[TextureRegion]
}

class SpriteFactoryImpl extends SpriteFactory {

  private val atlas: TextureAtlas = new TextureAtlas(SPRITES_PACK_LOCATION)
  private var offset: Int = 0

  override def createEntitySprite(regionName: String, width: Float, height: Float): EntitySprite = {
    val sprite = new EntitySpriteImpl()
    sprite.setRegion(this.atlas.findRegion(regionName))
    this.offset = sprite.getRegionY - 1
    sprite.setBounds(0, 0, width, height)
    sprite
  }

  override def createSpriteAnimation(sprite: EntitySprite, rowNumber: Int,
                                     startIndex: Int, endIndex: Int,
                                     frameDuration: Float = 0.10f): Animation[TextureRegion] = {
    //array from gdx.utils.Array
    val frames: Array[TextureRegion] = new Array[TextureRegion]()

    for(i <- startIndex to endIndex){
      frames.add(new TextureRegion(sprite.getTexture, i * sprite.getIntWidth,
        sprite.getIntHeight * rowNumber + this.offset , sprite.getIntWidth, sprite.getIntHeight))
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
      frames.add(new TextureRegion(sprite.getTexture, i * sprite.getIntWidth,
        sprite.getIntHeight * rowNumber + this.offset , sprite.getIntWidth, sprite.getIntHeight))
    }

    for(i <- startIndex2 to endIndex2){
      frames.add(new TextureRegion(sprite.getTexture, i * sprite.getIntWidth,
        sprite.getIntHeight * rowNumber2 + this.offset , sprite.getIntWidth, sprite.getIntHeight))
    }

    new Animation(frameDuration, frames)
  }
}
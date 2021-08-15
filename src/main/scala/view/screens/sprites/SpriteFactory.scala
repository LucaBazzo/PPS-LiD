package view.screens.sprites

import com.badlogic.gdx.graphics.g2d.{Animation, TextureAtlas, TextureRegion}
import com.badlogic.gdx.utils.Array
import utils.ApplicationConstants.SPRITES_PACK_LOCATION

trait SpriteFactory {

  def createEntitySprite(regionName: String, width: Float, height: Float): EntitySprite

  def createSpriteAnimation(sprite: EntitySprite, rowNumber: Int,
                            startIndex: Int, endIndex: Int,
                            frameDuration: Float = 0.10f): Animation[TextureRegion]
}

class SpriteFactoryImpl extends SpriteFactory {

  private val atlas: TextureAtlas = new TextureAtlas(SPRITES_PACK_LOCATION)

  override def createEntitySprite(regionName: String, width: Float, height: Float): EntitySprite = {
    val sprite = new EntitySpriteImpl()
    sprite.setRegion(this.atlas.findRegion(regionName))
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
        sprite.getIntHeight * rowNumber , sprite.getIntWidth, sprite.getIntHeight))
    }

    new Animation(frameDuration, frames)
  }
}
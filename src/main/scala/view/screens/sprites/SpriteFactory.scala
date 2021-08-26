package view.screens.sprites

import com.badlogic.gdx.graphics.g2d.{Animation, TextureAtlas, TextureRegion}
import com.badlogic.gdx.utils.Array
import utils.ApplicationConstants.SPRITES_PACK_LOCATION

trait SpriteFactory {

  def createHeroSprite(spriteSheetName:String, regionName: String, spriteWidth: Float, spriteHeight: Float): EntitySprite

  def createEntitySprite(spriteSheetName:String, regionName: String, spriteWidth: Float, spriteHeight: Float,
                         entitySpriteWidth: Float, entitySpriteHeight: Float,
                         sizeMultiplicative: Float = 0): EntitySprite

  def createSpriteAnimation(sprite: EntitySprite, rowNumber: Int,
                            startIndex: Int, endIndex: Int,
                            frameDuration: Float = 0.10f): Animation[TextureRegion]

  def createSpriteAnimationFromTwoRows(sprite: EntitySprite,
                                       rowNumber: Int, startIndex: Int, endIndex: Int,
                                       rowNumber2: Int, startIndex2: Int, endIndex2: Int,
                                       frameDuration: Float = 0.10f): Animation[TextureRegion]
}

class SpriteFactoryImpl extends SpriteFactory {
  private var atlasLookup: Map[String, TextureAtlas] = Map.empty


  // TODO: move entities (sprite file name, region name, image width, image heigth) into constants file

  override def createHeroSprite(spriteSheetName:String, regionName: String, spriteWidth: Float, spriteHeight: Float): EntitySprite = {
    var atlas: Option[TextureAtlas] = atlasLookup.get(spriteSheetName)
    if (atlas.isEmpty) {
      atlas = Option(new TextureAtlas(spriteSheetName))
    }

    val sprite = new HeroEntitySprite(spriteWidth, spriteHeight)
    sprite.setRegion(atlas.get.findRegion(regionName))
    sprite.setBounds(0, 0, spriteWidth, spriteHeight)
    sprite
  }

  override def createEntitySprite(spriteSheetName: String, regionName: String, spriteWidth: Float, spriteHeight: Float,
                                  entitySpriteWidth: Float, entitySpriteHeight: Float,
                                  sizeMultiplicative: Float = 1): EntitySprite = {
    val atlas: TextureAtlas = new TextureAtlas(spriteSheetName)
    val sprite = new EntitySpriteImpl(entitySpriteWidth * sizeMultiplicative, entitySpriteHeight * sizeMultiplicative)
    sprite.setRegion(atlas.findRegion(regionName))
    sprite.setBounds(0, 0, spriteWidth, spriteHeight)
    sprite
  }

  override def createSpriteAnimation(sprite: EntitySprite, rowNumber: Int,
                                     startIndex: Int, endIndex: Int,
                                     frameDuration: Float = 0.10f): Animation[TextureRegion] = {
    //array from gdx.utils.Array
    val frames: Array[TextureRegion] = new Array[TextureRegion]()

    for(i <- startIndex to endIndex){
      frames.add(new TextureRegion(sprite.getTexture, i * sprite.getIntWidth + sprite.getRegionX - 1,
        sprite.getIntHeight * rowNumber + sprite.getRegionY - 1 , sprite.getIntWidth, sprite.getIntHeight))
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
      frames.add(new TextureRegion(sprite.getTexture, i * sprite.getIntWidth + sprite.getRegionX - 1 ,
        sprite.getIntHeight * rowNumber + sprite.getRegionY - 1 , sprite.getIntWidth, sprite.getIntHeight))
    }

    for(i <- startIndex2 to endIndex2){
      frames.add(new TextureRegion(sprite.getTexture, i * sprite.getIntWidth + sprite.getRegionX - 1 ,
        sprite.getIntHeight * rowNumber2 + sprite.getRegionY - 1 , sprite.getIntWidth, sprite.getIntHeight))
    }

    new Animation(frameDuration, frames)
  }
}
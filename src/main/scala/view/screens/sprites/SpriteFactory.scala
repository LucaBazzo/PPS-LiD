package view.screens.sprites

import com.badlogic.gdx.graphics.g2d.{Animation, TextureAtlas, TextureRegion}
import com.badlogic.gdx.utils.Array
import model.entities.Items.Items
import model.entities.{Items, State}
import utils.ApplicationConstants.SPRITES_PACK_LOCATION

import scala.collection.mutable

trait SpriteFactory {

  def createHeroSprite(regionName: String, spriteWidth: Float, spriteHeight: Float): EntitySprite

  def createEntitySprite(regionName: String, spriteWidth: Float, spriteHeight: Float,
                         entitySpriteWidth: Float, entitySpriteHeight: Float,
                         sizeMultiplicative: Float = 0): EntitySprite

  def createSpriteAnimation(sprite: EntitySprite, rowNumber: Int,
                            startIndex: Int, endIndex: Int,
                            frameDuration: Float = 0.10f): Animation[TextureRegion]

  def createSpriteAnimationFromTwoRows(sprite: EntitySprite,
                                       rowNumber: Int, startIndex: Int, endIndex: Int,
                                       rowNumber2: Int, startIndex2: Int, endIndex2: Int,
                                       frameDuration: Float = 0.10f): Animation[TextureRegion]

  def createItemSprites(): Map[Items.Value, EntitySprite]
}

class SpriteFactoryImpl extends SpriteFactory {

  private val atlas: TextureAtlas = new TextureAtlas(SPRITES_PACK_LOCATION)
  private var offset: Int = 0

  override def createHeroSprite(regionName: String, spriteWidth: Float, spriteHeight: Float): EntitySprite = {
    val sprite = new HeroEntitySprite(spriteWidth, spriteHeight)
    sprite.setRegion(this.atlas.findRegion(regionName))
    this.offset = sprite.getRegionY - 1
    sprite.setBounds(0, 0, spriteWidth, spriteHeight)
    sprite
  }

  override def createEntitySprite(regionName: String, spriteWidth: Float, spriteHeight: Float,
                                  entitySpriteWidth: Float, entitySpriteHeight: Float,
                                  sizeMultiplicative: Float = 1): EntitySprite = {
    val sprite = new EntitySpriteImpl(entitySpriteWidth * sizeMultiplicative, entitySpriteHeight * sizeMultiplicative)
    sprite.setRegion(this.atlas.findRegion(regionName))
    this.offset = sprite.getRegionY - 1
    sprite.setBounds(0, 0, spriteWidth, spriteHeight)
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

  override def createItemSprites(): Map[Items.Value, EntitySprite] = {
    var itemRow: Int = 0
    var itemCol: Int = 0
    val itemsMap: mutable.HashMap[Items.Value, EntitySprite] = new mutable.HashMap[Items, EntitySprite]()
    Items.values.foreach(x => {
      var sprite = this.createEntitySprite("items", 32,
        32, 10, 10, 2)
      sprite.addAnimation(State.Standing,
        this.createSpriteAnimation(sprite, itemRow, itemCol, itemCol, 0.20f))
      itemsMap.addOne(x, sprite )
      itemCol = itemCol + 1
      if(itemCol > 6) {
        itemCol = 0
        itemRow = 1
      }
    })

    itemsMap.toMap
  }
}
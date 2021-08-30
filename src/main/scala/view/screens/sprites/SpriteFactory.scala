package view.screens.sprites

import com.badlogic.gdx.graphics.g2d.{Animation, TextureAtlas, TextureRegion}
import com.badlogic.gdx.utils.Array
import model.entities.State
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

}

class SpriteFactoryImpl extends SpriteFactory {

  private val atlas: TextureAtlas = new TextureAtlas(SPRITES_PACK_LOCATION)

  override def createHeroSprite(regionName: String, spriteWidth: Float, spriteHeight: Float): EntitySprite = {
    val sprite = new HeroEntitySprite(regionName, spriteWidth, spriteHeight)
    sprite.setRegion(this.atlas.findRegion(regionName))
    sprite.setBounds(0, 0, spriteWidth, spriteHeight)
    this.defineHeroSpriteAnimations(sprite)
    sprite
  }

  override def createEntitySprite(regionName: String, spriteWidth: Float, spriteHeight: Float,
                                  entitySpriteWidth: Float, entitySpriteHeight: Float,
                                  sizeMultiplicative: Float = 1): EntitySprite = {
    val sprite = new EntitySpriteImpl(regionName, entitySpriteWidth * sizeMultiplicative, entitySpriteHeight * sizeMultiplicative)
    sprite.setRegion(this.atlas.findRegion(regionName))
    sprite.setBounds(0, 0, spriteWidth, spriteHeight)
    sprite
  }

  override def createSpriteAnimation(sprite: EntitySprite, rowNumber: Int,
                                     startIndex: Int, endIndex: Int,
                                     frameDuration: Float = 0.10f): Animation[TextureRegion] = {
    val offsetX = sprite.getRegionX - 1
    val offsetY = sprite.getRegionY - 1

    //array from gdx.utils.Array
    val frames: Array[TextureRegion] = new Array[TextureRegion]()

    for(i <- startIndex to endIndex){
      frames.add(new TextureRegion(sprite.getTexture, i * sprite.getIntWidth + offsetX,
        sprite.getIntHeight * rowNumber + offsetY , sprite.getIntWidth, sprite.getIntHeight))
    }

    new Animation(frameDuration, frames)
  }

  def createSpriteAnimationFromTwoRows(sprite: EntitySprite,
                            rowNumber: Int, startIndex: Int, endIndex: Int,
                            rowNumber2: Int, startIndex2: Int, endIndex2: Int,
                            frameDuration: Float = 0.10f): Animation[TextureRegion] = {
    val offsetX = sprite.getRegionX - 1
    val offsetY = sprite.getRegionY - 1

    //array from gdx.utils.Array
    val frames: Array[TextureRegion] = new Array[TextureRegion]()

    for(i <- startIndex to endIndex){
      frames.add(new TextureRegion(sprite.getTexture, i * sprite.getIntWidth + offsetX,
        sprite.getIntHeight * rowNumber + offsetY , sprite.getIntWidth, sprite.getIntHeight))
    }

    for(i <- startIndex2 to endIndex2){
      frames.add(new TextureRegion(sprite.getTexture, i * sprite.getIntWidth + offsetX,
        sprite.getIntHeight * rowNumber2 + offsetY , sprite.getIntWidth, sprite.getIntHeight))
    }

    new Animation(frameDuration, frames)
  }

  private def defineHeroSpriteAnimations(heroSprite: EntitySprite): Unit = {
    heroSprite.addAnimation(State.Standing,
        this.createSpriteAnimation(heroSprite, 0, 0, 3, 0.18f), loop = true)
    heroSprite.addAnimation(State.Running,
        this.createSpriteAnimation(heroSprite, 1, 1, 6), loop = true)
    heroSprite.addAnimation(State.Jumping,
        this.createSpriteAnimation(heroSprite, 2, 0, 3))
    heroSprite.addAnimation(State.Falling,
        this.createSpriteAnimation(heroSprite, 3, 1, 2), loop = true)
    heroSprite.addAnimation(State.Sliding,
        this.createSpriteAnimation(heroSprite, 3, 3, 6))
    heroSprite.addAnimation(State.Crouch,
        this.createSpriteAnimationFromTwoRows(heroSprite, 0, 4, 6,
        1,0,0,0.18f), loop = true)
    heroSprite.addAnimation(State.Attack01,
        this.createSpriteAnimation(heroSprite, 6, 0, 6))
    heroSprite.addAnimation(State.Attack02,
        this.createSpriteAnimation(heroSprite, 7, 0, 3, 0.20f))
    heroSprite.addAnimation(State.Attack03,
        this.createSpriteAnimationFromTwoRows(heroSprite, 7, 4, 6,
        8, 0, 2))
    heroSprite.addAnimation(State.Somersault,
        this.createSpriteAnimationFromTwoRows(heroSprite, 2, 4, 6,
        3, 0, 0), loop = true)
    heroSprite.addAnimation(State.BowAttack,
        this.createSpriteAnimationFromTwoRows(heroSprite, 16, 0, 6,
        17, 0, 1))
  }

}
package view.screens.sprites

import com.badlogic.gdx.graphics.g2d.{Animation, TextureAtlas, TextureRegion}
import com.badlogic.gdx.utils.Array
import model.entities.{Items, State}

trait SpriteFactory {
  def createHeroSprite(spriteSheetName: String, regionName: String, spriteWidth: Float, spriteHeight: Float): EntitySprite


  def createEntitySprite(spritePackName: String, regionName: String, spriteWidth: Float, spriteHeight: Float,
                         entitySpriteWidth: Float, entitySpriteHeight: Float,
                         sizeMultiplicative: Float = 0): EntitySprite

  def createSpriteAnimation(sprite: EntitySprite, rowNumber: Int,
                            startIndex: Int, endIndex: Int,
                            frameDuration: Float = 0.10f): Animation[TextureRegion]

  def createSpriteAnimationFromTwoRows(sprite: EntitySprite,
                                       rowNumber: Int, startIndex: Int, endIndex: Int,
                                       rowNumber2: Int, startIndex2: Int, endIndex2: Int,
                                       frameDuration: Float = 0.10f): Animation[TextureRegion]

  def defineEnemySkeletonAnimation(sprite:EntitySprite): Unit
  def defineEnemySlimeAnimation(sprite:EntitySprite): Unit
  def defineEnemyWormAnimation(sprite: EntitySprite):Unit
  def defineAttackFireballAnimation(sprite: EntitySprite):Unit
}

class SpriteFactoryImpl extends SpriteFactory {

  private var atlases: Map[String, TextureAtlas] = Map.empty

  override def createHeroSprite(spritePackName:String, regionName: String, spriteWidth: Float, spriteHeight: Float): EntitySprite = {
    this.lookupSprite(spritePackName)

    val sprite = new HeroEntitySprite(regionName, spriteWidth, spriteHeight)
    sprite.setRegion(this.atlases(spritePackName).findRegion(regionName))

    sprite.setBounds(0, 0, spriteWidth, spriteHeight)
    this.defineHeroSpriteAnimations(sprite)
    sprite
  }

  override def createEntitySprite(spritePackName:String, regionName: String, spriteWidth: Float, spriteHeight: Float,
                                  entitySpriteWidth: Float, entitySpriteHeight: Float,
                                  sizeMultiplicative: Float = 1): EntitySprite = {

    this.lookupSprite(spritePackName)
    val sprite = new EntitySpriteImpl(regionName, entitySpriteWidth * sizeMultiplicative, entitySpriteHeight * sizeMultiplicative)
    sprite.setRegion(this.atlases(spritePackName).findRegion(regionName))

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

  def createSpriteAnimationFromThreeRows(sprite: EntitySprite,
                                       rowNumber: Int, startIndex: Int, endIndex: Int,
                                       rowNumber2: Int, startIndex2: Int, endIndex2: Int,
                                       rowNumber3: Int, startIndex3: Int, endIndex3: Int,
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

    for(i <- startIndex3 to endIndex3){
      frames.add(new TextureRegion(sprite.getTexture, i * sprite.getIntWidth + offsetX,
        sprite.getIntHeight * rowNumber3 + offsetY , sprite.getIntWidth, sprite.getIntHeight))
    }

    new Animation(frameDuration, frames)
  }

  override def defineEnemyWormAnimation(sprite:EntitySprite): Unit = {
    sprite.addAnimation(State.Attack01,
      this.createSpriteAnimationFromThreeRows(sprite,
        0, 0, 6,
        1,0,6,
        2,0,1,0.15f))
    sprite.addAnimation(State.Dying,
      this.createSpriteAnimationFromTwoRows(sprite,
        2, 2, 6,
        3, 0, 2, 0.18f))
    sprite.addAnimation(State.Hurt,
      this.createSpriteAnimation(sprite,
        3, 3, 5,0.18f))
    sprite.addAnimation(State.Standing,
      this.createSpriteAnimationFromThreeRows(sprite,
        3, 6, 6,
        4,0,6,
        5,0,0,0.18f), loop = true)
    sprite.addAnimation(State.Running,
      this.createSpriteAnimationFromTwoRows(sprite,
        5, 1, 6,
        6, 0, 2, 0.18f), loop = true)
  }

  def defineAttackFireballAnimation(sprite: EntitySprite):Unit = {
    sprite.addAnimation(State.Running,
      this.createSpriteAnimation(sprite,
        1, 0, 5,0.15f), loop = true)
    sprite.addAnimation(State.Dying,
      this.createSpriteAnimation(sprite,
        0, 0, 6, 0.18f))
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

  override def defineEnemySkeletonAnimation(sprite:EntitySprite): Unit = {
    sprite.addAnimation(State.Attack01,
      this.createSpriteAnimationFromTwoRows(sprite,
        0, 0, 6,
        1,0,0,0.15f))
    sprite.addAnimation(State.Dying,
      this.createSpriteAnimation(sprite,
        1, 1, 4, 0.18f))
    sprite.addAnimation(State.Standing,
      this.createSpriteAnimationFromTwoRows(sprite,
        1, 5, 6,
        2,0,1,0.18f), loop = true)
    sprite.addAnimation(State.Hurt,
      this.createSpriteAnimationFromTwoRows(sprite,
        2, 6, 6,
        3,0,2,0.18f))
    sprite.addAnimation(State.Running,
      this.createSpriteAnimation(sprite,
        3, 3, 6, 0.18f), loop = true)
  }

  override def defineEnemySlimeAnimation(sprite:EntitySprite): Unit = {
    sprite.addAnimation(State.Attack01,
      this.createSpriteAnimation(sprite, 0, 0, 4, 0.18f))
    sprite.addAnimation(State.Dying,
      this.createSpriteAnimationFromTwoRows(sprite, 0, 5, 6,
        1,0,1,0.15f))
    sprite.addAnimation(State.Hurt,
      this.createSpriteAnimation(sprite, 1, 2, 5, 0.18f))
    sprite.addAnimation(State.Standing,
      this.createSpriteAnimationFromTwoRows(sprite, 1, 6, 6,
        2,0,2,0.15f), loop = true)
    sprite.addAnimation(State.Running,
      this.createSpriteAnimation(sprite, 2, 3, 6, 0.18f), loop = true)
  }

  def lookupSprite(spritePackName: String): Unit = {
    if (!this.atlases.contains(spritePackName))
      this.atlases += spritePackName -> new TextureAtlas(spritePackName)
  }
}
package view.screens.sprites

import com.badlogic.gdx.graphics.g2d.{Animation, TextureAtlas, TextureRegion}
import com.badlogic.gdx.utils.Array
import model.entities.EntityType.EntityType
import model.entities.{EntityType, State}

trait SpriteFactory {
  def createHeroSprite(spriteSheetName: String, regionName: String, spriteWidth: Float, spriteHeight: Float): EntitySprite


  def createEntitySprite(entityType: EntityType, spritePackName: String, regionName: String, spriteWidth: Float,
                         spriteHeight: Float, sizeMultiplicative: Float = 0): EntitySprite

  def createAnimation(sprite: EntitySprite, colNumber: Int,
                      startCell: (Int, Int), endCell: (Int, Int),
                      frameDuration: Float = 0.10f, reverse:Boolean = false): Animation[TextureRegion]
}

class SpriteFactoryImpl extends SpriteFactory {

  private var atlases: Map[String, TextureAtlas] = Map.empty
  private var loadedSprites: Map[EntityType, EntitySprite] = Map.empty

  // TODO: unire la createHeroSprite con createEntitySprite
  // TODO: verificare se è possibile usare un pattern proxy

  override def createHeroSprite(spritePackName:String, regionName: String, spriteWidth: Float, spriteHeight: Float): EntitySprite = {
    // load the atlas (spritesheet)
    if (!this.atlases.contains(spritePackName))
      this.atlases += spritePackName -> new TextureAtlas(spritePackName)

    val sprite = new HeroEntitySprite(regionName, spriteWidth, spriteHeight)
    sprite.setRegion(this.atlases(spritePackName).findRegion(regionName))

    sprite.setBounds(0, 0, spriteWidth, spriteHeight)
    this.defineHeroSpriteAnimations(sprite)

    this.loadedSprites += EntityType.Hero -> sprite
    sprite
  }

  override def createEntitySprite(entityType: EntityType, spritePackName:String, regionName: String, spriteWidth: Float,
                                  spriteHeight: Float, sizeMultiplicative: Float = 1): EntitySprite = {
    // load the atlas (spritesheet)
    if (!this.atlases.contains(spritePackName))
      this.atlases += spritePackName -> new TextureAtlas(spritePackName)

    // load the sprites and define the animations to be displayed
    val sprite = new EntitySpriteImpl(regionName,
      spriteWidth * sizeMultiplicative,
      spriteHeight * sizeMultiplicative)
    sprite.setRegion(this.atlases(spritePackName).findRegion(regionName))
    sprite.setBounds(0, 0, spriteWidth, spriteHeight)

    // define animations for the created sprite
    entityType match {
      case EntityType.Arrow => this.defineAttackArrowAnimation(sprite)
      case EntityType.EnemySkeleton => this.defineEnemySkeletonAnimation(sprite)
      case EntityType.EnemySlime => this.defineEnemySlimeAnimation(sprite)
      case EntityType.EnemyPacman => this.defineEnemyPacmanAnimation(sprite)
      case EntityType.EnemyWorm => this.defineEnemyWormAnimation(sprite)
      case EntityType.EnemyBossWizard => this.defineEnemyWizardAnimation(sprite)
      case EntityType.AttackFireBall => this.defineAttackFireballAnimation(sprite)
      case EntityType.AttackEnergyBall => this.defineAttackEnergyBallAnimation(sprite)
      case EntityType.Door => this.defineDoorAnimation(sprite)
      case EntityType.Chest => this.defineChestAnimation(sprite)
      case EntityType.Portal => this.definePortalAnimation(sprite)

      case _ => null
    }
    sprite
  }

  // TODO: ottimo da fare con Prolog (date due celle produrre la lista di celle nel mezzo)
  override def createAnimation(sprite: EntitySprite, colNumber: Int,
                               startCell: (Int, Int), endCell: (Int, Int),
                               frameDuration: Float = 0.10f, reverse:Boolean = false): Animation[TextureRegion] = {
    val offsetX = sprite.getRegionX - 1
    val offsetY = sprite.getRegionY - 1
    val width = sprite.getIntWidth
    val height = sprite.getIntHeight

    //array from gdx.utils.Array
    val frames: Array[TextureRegion] = new Array[TextureRegion]()

    if (startCell._1 != endCell._1) {
      for (i <- startCell._2 until colNumber)
        frames.add(new TextureRegion(sprite.getTexture, i * width + offsetX,
          height * startCell._1 + offsetY, width, height))

      for (i <- startCell._1 + 1 until endCell._1;
           j <- 0 until colNumber)
        frames.add(new TextureRegion(sprite.getTexture, j * width + offsetX,
          height * i + offsetY, width, height))

      for (i <- 0 until endCell._2 + 1)
        frames.add(new TextureRegion(sprite.getTexture, i * width + offsetX,
          height * endCell._1 + offsetY, width, height))
    } else
      for (i <- startCell._2 until endCell._2 + 1)
        frames.add(new TextureRegion(sprite.getTexture, i * width + offsetX,
          height * startCell._1 + offsetY, width, height))

    if(reverse) frames.reverse()

    new Animation(frameDuration, frames)
  }

  private def defineAttackArrowAnimation(sprite: EntitySpriteImpl): Unit =
    sprite.addAnimation(State.Standing, this.createAnimation(sprite, 7, (0, 0), (0, 0)))

  private def defineHeroSpriteAnimations(sprite: EntitySprite): Unit = {
    sprite.addAnimation(State.Standing, this.createAnimation(sprite, 7, (0, 0), (0, 3), 0.18f), loop = true)
    sprite.addAnimation(State.Running, this.createAnimation(sprite, 7, (1, 1), (1, 6)), loop = true)
    sprite.addAnimation(State.Jumping, this.createAnimation(sprite, 7, (2, 0), (2, 3)))
    sprite.addAnimation(State.Falling, this.createAnimation(sprite, 7, (3, 1), (3, 2)), loop = true)
    sprite.addAnimation(State.Sliding, this.createAnimation(sprite, 7, (3, 3), (3, 6)))
    sprite.addAnimation(State.Crouching, this.createAnimation(sprite, 7, (0, 4), (1, 0)), loop = true)
    sprite.addAnimation(State.Attack01, this.createAnimation(sprite, 7, (6, 0), (6, 6)))
    sprite.addAnimation(State.Attack02, this.createAnimation(sprite, 7, (7, 0), (7, 3), 0.20f))
    sprite.addAnimation(State.Attack03, this.createAnimation(sprite, 7, (7, 4), (8, 2)))
    sprite.addAnimation(State.Somersault, this.createAnimation(sprite, 7, (2, 4), (3, 0)), loop = true)
    sprite.addAnimation(State.BowAttacking, this.createAnimation(sprite, 7, (16, 0), (17, 1)))
    sprite.addAnimation(State.LadderClimbing, this.createAnimation(sprite, 7, (11, 4), (12, 0)), loop = true)
    sprite.addAnimation(State.LadderDescending, this.createAnimation(sprite, 7, (11, 4), (12, 0), reverse = true), loop = true)
    sprite.addAnimation(State.LadderIdle, this.createAnimation(sprite, 7, (11, 6), (11, 6)))
    sprite.addAnimation(State.pickingItem, this.createAnimation(sprite, 7, (13, 2), (13, 4), 0.15f))
    sprite.addAnimation(State.Hurt, this.createAnimation(sprite, 7, (8, 3), (8, 5)))
    sprite.addAnimation(State.Dying, this.createAnimation(sprite, 7, (8, 6), (9, 5), 0.18f))
    sprite.addAnimation(State.AirDownAttacking, this.createAnimation(sprite, 7, (14, 4), (15, 0)))
    sprite.addAnimation(State.AirDownAttackingEnd, this.createAnimation(sprite, 7, (15, 1), (15, 3), 0.18f))
  }

  // TODO: calibrare la velocita delle animaziooni di nemici

  private def defineEnemyWormAnimation(sprite:EntitySprite): Unit = {
    sprite.addAnimation(State.Attack01, this.createAnimation(sprite, 7, (0, 0), (2, 1)))
    sprite.addAnimation(State.Dying, this.createAnimation(sprite, 7, (2, 2), (3, 2)))
    sprite.addAnimation(State.Hurt, this.createAnimation(sprite, 7, (3, 3), (3, 5)))
    sprite.addAnimation(State.Standing, this.createAnimation(sprite, 7, (3, 6), (5, 0)), loop = true)
    sprite.addAnimation(State.Running, this.createAnimation(sprite, 7, (5, 1), (6, 2)), loop = true)
  }

  private def defineEnemyWizardAnimation(sprite:EntitySprite): Unit = {
    sprite.addAnimation(State.Attack01, this.createAnimation(sprite, 7, (0, 0), (1, 0)))
    sprite.addAnimation(State.Attack02, this.createAnimation(sprite, 7, (1, 1), (2, 1)))
    sprite.addAnimation(State.Dying, this.createAnimation(sprite, 7, (2, 2), (3, 1)))
    sprite.addAnimation(State.Standing, this.createAnimation(sprite, 7, (3, 2), (4, 2)), loop = true)
    sprite.addAnimation(State.Running, this.createAnimation(sprite, 7, (4, 3), (5, 3)), loop = true)
    sprite.addAnimation(State.Hurt, this.createAnimation(sprite, 7, (5, 4), (5, 6)))
  }

  // TODO: provare ad inserire anche l'animazione di esplosione
  private def defineAttackFireballAnimation(sprite: EntitySprite):Unit = {
    //    sprite.addAnimation(State.Dying,
    //      this.createAnimation(sprite, 7, (0, 0), (0, 6)))
    sprite.addAnimation(State.Standing, this.createAnimation(sprite, 7, (1, 0), (1, 5)), loop = true)
  }

  private def defineAttackEnergyBallAnimation(sprite: EntitySprite):Unit = {
    //    sprite.addAnimation(State.Standing,
    //      this.createAnimation(sprite, 7, (0, 0), (0, 6)))
    sprite.addAnimation(State.Standing, this.createAnimation(sprite, 7, (1, 0), (2, 1)), loop = true)
  }

  private  def defineEnemySkeletonAnimation(sprite:EntitySprite): Unit = {
    sprite.addAnimation(State.Attack01, this.createAnimation(sprite, 7, (0, 0), (1, 0)))
    sprite.addAnimation(State.Dying, this.createAnimation(sprite, 7, (1, 1), (1, 4)))
    sprite.addAnimation(State.Standing, this.createAnimation(sprite, 7, (1, 5), (2, 1)), loop = true)
    sprite.addAnimation(State.Hurt, this.createAnimation(sprite, 7, (2, 6), (3, 2)))
    sprite.addAnimation(State.Running, this.createAnimation(sprite, 7, (3, 3), (3, 6)), loop = true)
  }

  private  def defineEnemySlimeAnimation(sprite:EntitySprite): Unit = {
    sprite.addAnimation(State.Attack01, this.createAnimation(sprite, 7, (0, 0), (0, 4)))
    sprite.addAnimation(State.Dying, this.createAnimation(sprite, 7, (0, 5), (1, 1)))
    sprite.addAnimation(State.Hurt, this.createAnimation(sprite, 7, (1, 2), (1, 5)))
    sprite.addAnimation(State.Standing, this.createAnimation(sprite, 7, (1, 6), (2, 2)), loop = true)
    sprite.addAnimation(State.Running, this.createAnimation(sprite, 7, (2, 3), (2, 6)), loop = true)
  }

  private  def defineEnemyPacmanAnimation(sprite:EntitySprite): Unit = {
    sprite.addAnimation(State.Attack01, this.createAnimation(sprite, 7, (0, 0), (0, 1)))
    sprite.addAnimation(State.Dying, this.createAnimation(sprite, 7, (0, 0), (0, 1)))
    sprite.addAnimation(State.Hurt, this.createAnimation(sprite, 7, (0, 0), (0, 1)))
    sprite.addAnimation(State.Standing, this.createAnimation(sprite, 7, (0, 0), (0, 1)), loop = true)
    sprite.addAnimation(State.Running, this.createAnimation(sprite, 7, (0, 0), (0, 1)), loop = true)
  }

  private def defineDoorAnimation(sprite: EntitySpriteImpl): Unit = {
    sprite.addAnimation(State.Standing, this.createAnimation(sprite, 7, (0, 0), (0, 0)))
    sprite.addAnimation(State.Opening, this.createAnimation(sprite, 7, (0, 0), (0, 3)))
  }

  private def defineChestAnimation(sprite: EntitySpriteImpl): Unit = {
    sprite.addAnimation(State.Standing, this.createAnimation(sprite, 7, (0, 0), (0, 0)))
    sprite.addAnimation(State.Opening, this.createAnimation(sprite, 7, (0, 0), (0, 1)))
  }

  private def definePortalAnimation(sprite: EntitySpriteImpl): Unit = {
    sprite.addAnimation(State.Standing, this.createAnimation(sprite, 8, (0, 0), (0, 7)), loop = true)
    sprite.addAnimation(State.Opening, this.createAnimation(sprite, 8, (1, 0), (1, 7)))
    sprite.addAnimation(State.Closed, this.createAnimation(sprite, 8, (1, 3), (1, 3)))
  }
}
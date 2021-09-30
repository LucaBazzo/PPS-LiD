package view.sprite

import com.badlogic.gdx.graphics.g2d.{Animation, TextureAtlas, TextureRegion}
import com.badlogic.gdx.utils.Array
import model.entity.EntityType.EntityType
import model.entity.{EntityType, State}
import utils.SpritesConstants.{ARMOR_SPRITES_POSITION, BOOTS_SPRITES_POSITION, BOW_SPRITES_POSITION, CAKE_SPRITES_POSITION, CUSTOM_COLUMNS_NUMBER, DEFAULT_COLUMNS_NUMBER, HUGE_POTION_SPRITES_POSITION, KEY_SPRITES_POSITION, LARGE_POTION_SPRITES_POSITION, MAP_SPRITES_POSITION, SHIELD_SPRITES_POSITION, SKELETON_KEY_SPRITES_POSITION, SMALL_POTION_SPRITES_POSITION, SPRITES_SPRITES_POSITION, SWORD_SPRITES_POSITION, WRENCH_SPRITES_POSITION}

trait SpriteFactory {
  def createEntitySprite(entityType: EntityType, spritePackName: String, regionName: String, spriteWidth: Float,
                         spriteHeight: Float, sizeMultiplicative: Float = 0): EntitySprite

  def createItemSprite(entityType: EntityType, spritePackName: String, regionName: String, spriteWidth: Float,
                         spriteHeight: Float, sizeMultiplicative: Float = 0): EntitySprite

  def createAnimation(sprite: EntitySprite, colNumber: Int,
                      startCell: (Int, Int), endCell: (Int, Int),
                      frameDuration: Float = 0.10f, reverse:Boolean = false): Animation[TextureRegion]
}

class SpriteFactoryImpl extends SpriteFactory {

  private var atlases: Map[String, TextureAtlas] = Map.empty

  override def createEntitySprite(entityType: EntityType, spritePackName:String, regionName: String, spriteWidth: Float,
                                  spriteHeight: Float, sizeMultiplicative: Float = 1): EntitySprite = {
    // load the atlas (spritesheet)
    if (!this.atlases.contains(spritePackName))
      this.atlases += spritePackName -> new TextureAtlas(spritePackName)

    // load the sprites and define the animations to be displayed
    val sprite: EntitySprite = entityType match {
      case EntityType.Hero =>
        new HeroEntitySprite(regionName, spriteWidth, spriteHeight)
      case _ =>
        new EntitySpriteImpl(spriteWidth * sizeMultiplicative, spriteHeight * sizeMultiplicative)
    }
    sprite.setRegion(this.atlases(spritePackName).findRegion(regionName))
    sprite.setBounds(0, 0, spriteWidth, spriteHeight)

    // define animations for the created sprite
    entityType match {
      case EntityType.Hero => this.defineHeroSpriteAnimations(sprite)
      case EntityType.Arrow => this.defineAttackArrowAnimation(sprite)
      case EntityType.EnemySkeleton => this.defineEnemySkeletonAnimation(sprite)
      case EntityType.EnemySlime => this.defineEnemySlimeAnimation(sprite)
      case EntityType.EnemyPacman => this.defineEnemyPacmanAnimation(sprite)
      case EntityType.EnemyWorm => this.defineEnemyWormAnimation(sprite)
      case EntityType.EnemyBat => this.defineEnemyBatAnimation(sprite)
      case EntityType.EnemyBossWizard => this.defineEnemyWizardAnimation(sprite)
      case EntityType.AttackFireBall => this.defineAttackFireballAnimation(sprite)
      case EntityType.AttackEnergyBall => this.defineAttackEnergyBallAnimation(sprite)
      case EntityType.Door => this.defineDoorAnimation(sprite)
      case EntityType.Chest => this.defineChestAnimation(sprite)
      case EntityType.Portal => this.definePortalAnimation(sprite)
      case _ =>
    }
    sprite
  }

  override def createItemSprite(entityType: EntityType, spritePackName: String, regionName: String, spriteWidth: Float,
                                spriteHeight: Float, sizeMultiplicative: Float): EntitySprite = {
    val sprite = entityType match {
      case EntityType.ArmorItem => this.createEntitySprite(entityType, spritePackName, regionName, spriteWidth, spriteHeight, sizeMultiplicative)
      case EntityType.CakeItem => this.createEntitySprite(entityType, spritePackName, regionName, spriteWidth, spriteHeight, sizeMultiplicative)
      case EntityType.BootsItem => this.createEntitySprite(entityType, spritePackName, regionName, spriteWidth, spriteHeight, sizeMultiplicative)
      case EntityType.ShieldItem => this.createEntitySprite(entityType, spritePackName, regionName, spriteWidth, spriteHeight, sizeMultiplicative)
      case EntityType.MapItem => this.createEntitySprite(entityType, spritePackName, regionName, spriteWidth, spriteHeight, sizeMultiplicative)
      case EntityType.WrenchItem => this.createEntitySprite(entityType, spritePackName, regionName, spriteWidth, spriteHeight, sizeMultiplicative)
      case EntityType.KeyItem => this.createEntitySprite(entityType, spritePackName, regionName, spriteWidth, spriteHeight, sizeMultiplicative)
      case EntityType.SmallPotionItem => this.createEntitySprite(entityType, spritePackName, regionName, spriteWidth, spriteHeight, sizeMultiplicative)
      case EntityType.PotionItem => this.createEntitySprite(entityType, spritePackName, regionName, spriteWidth, spriteHeight, sizeMultiplicative)
      case EntityType.LargePotionItem => this.createEntitySprite(entityType, spritePackName, regionName, spriteWidth, spriteHeight, sizeMultiplicative)
      case EntityType.HugePotionItem => this.createEntitySprite(entityType, spritePackName, regionName, spriteWidth, spriteHeight, sizeMultiplicative)
      case EntityType.SkeletonKeyItem => this.createEntitySprite(entityType, spritePackName, regionName, spriteWidth, spriteHeight, sizeMultiplicative)
      case EntityType.BowItem => this.createEntitySprite(entityType, spritePackName, regionName, spriteWidth, spriteHeight, sizeMultiplicative)
      case EntityType.BFSwordItem => this.createEntitySprite(entityType, spritePackName, regionName, spriteWidth, spriteHeight, sizeMultiplicative)
    }

    entityType match {
      case EntityType.ArmorItem => sprite.addAnimation(State.Standing, this.createAnimation(sprite,
        DEFAULT_COLUMNS_NUMBER, ARMOR_SPRITES_POSITION, ARMOR_SPRITES_POSITION))
      case EntityType.CakeItem => sprite.addAnimation(State.Standing, this.createAnimation(sprite,
        DEFAULT_COLUMNS_NUMBER, CAKE_SPRITES_POSITION, CAKE_SPRITES_POSITION))
      case EntityType.BootsItem => sprite.addAnimation(State.Standing, this.createAnimation(sprite,
        DEFAULT_COLUMNS_NUMBER, BOOTS_SPRITES_POSITION, BOOTS_SPRITES_POSITION))
      case EntityType.ShieldItem => sprite.addAnimation(State.Standing, this.createAnimation(sprite,
        DEFAULT_COLUMNS_NUMBER, SHIELD_SPRITES_POSITION, SHIELD_SPRITES_POSITION))
      case EntityType.MapItem => sprite.addAnimation(State.Standing, this.createAnimation(sprite,
        DEFAULT_COLUMNS_NUMBER, MAP_SPRITES_POSITION, MAP_SPRITES_POSITION))
      case EntityType.WrenchItem => sprite.addAnimation(State.Standing, this.createAnimation(sprite,
        DEFAULT_COLUMNS_NUMBER, WRENCH_SPRITES_POSITION, WRENCH_SPRITES_POSITION))
      case EntityType.KeyItem => sprite.addAnimation(State.Standing, this.createAnimation(sprite,
        DEFAULT_COLUMNS_NUMBER, KEY_SPRITES_POSITION, KEY_SPRITES_POSITION))
      case EntityType.SmallPotionItem => sprite.addAnimation(State.Standing, this.createAnimation(sprite,
        DEFAULT_COLUMNS_NUMBER, SMALL_POTION_SPRITES_POSITION, SMALL_POTION_SPRITES_POSITION))
      case EntityType.PotionItem => sprite.addAnimation(State.Standing, this.createAnimation(sprite,
        DEFAULT_COLUMNS_NUMBER, LARGE_POTION_SPRITES_POSITION, LARGE_POTION_SPRITES_POSITION))
      case EntityType.LargePotionItem => sprite.addAnimation(State.Standing, this.createAnimation(sprite,
        DEFAULT_COLUMNS_NUMBER, HUGE_POTION_SPRITES_POSITION, HUGE_POTION_SPRITES_POSITION))
      case EntityType.HugePotionItem => sprite.addAnimation(State.Standing, this.createAnimation(sprite,
        DEFAULT_COLUMNS_NUMBER, SKELETON_KEY_SPRITES_POSITION, SKELETON_KEY_SPRITES_POSITION))
      case EntityType.SkeletonKeyItem => sprite.addAnimation(State.Standing, this.createAnimation(sprite,
        DEFAULT_COLUMNS_NUMBER, BOW_SPRITES_POSITION, BOW_SPRITES_POSITION))
      case EntityType.BowItem => sprite.addAnimation(State.Standing, this.createAnimation(sprite,
        DEFAULT_COLUMNS_NUMBER, SWORD_SPRITES_POSITION, SWORD_SPRITES_POSITION))
      case EntityType.BFSwordItem => sprite.addAnimation(State.Standing, this.createAnimation(sprite,
        DEFAULT_COLUMNS_NUMBER, SPRITES_SPRITES_POSITION, SPRITES_SPRITES_POSITION))}
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

  private def defineAttackArrowAnimation(sprite: EntitySprite): Unit =
    sprite.addAnimation(State.Standing, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (0, 0), (0, 0)))

  private def defineHeroSpriteAnimations(sprite: EntitySprite): Unit = {
    sprite.addAnimation(State.Standing, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (0, 0), (0, 3), 0.18f), loop = true)
    sprite.addAnimation(State.Running, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (1, 1), (1, 6)), loop = true)
    sprite.addAnimation(State.Jumping, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (2, 0), (2, 3)))
    sprite.addAnimation(State.Falling, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (3, 1), (3, 2)), loop = true)
    sprite.addAnimation(State.Sliding, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (3, 3), (3, 6)))
    sprite.addAnimation(State.Crouching, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (0, 4), (1, 0)), loop = true)
    sprite.addAnimation(State.Attack01, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (6, 0), (6, 6)))
    sprite.addAnimation(State.Attack02, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (7, 0), (7, 3), 0.20f))
    sprite.addAnimation(State.Attack03, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (7, 4), (8, 2)))
    sprite.addAnimation(State.Somersault, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (2, 4), (3, 0)), loop = true)
    sprite.addAnimation(State.BowAttacking, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (16, 0), (17, 1)))
    sprite.addAnimation(State.LadderClimbing, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (11, 4), (12, 0)), loop = true)
    sprite.addAnimation(State.LadderDescending, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (11, 4), (12, 0), reverse = true), loop = true)
    sprite.addAnimation(State.LadderIdle, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (11, 6), (11, 6)))
    sprite.addAnimation(State.PickingItem, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (13, 2), (13, 4), 0.15f))
    sprite.addAnimation(State.Hurt, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (8, 3), (8, 5)))
    sprite.addAnimation(State.Dying, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (8, 6), (9, 5), 0.18f))
    sprite.addAnimation(State.AirDownAttacking, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (14, 4), (15, 0)))
    sprite.addAnimation(State.AirDownAttackingEnd, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (15, 1), (15, 3), 0.18f))
  }

  private def defineEnemyWormAnimation(sprite:EntitySprite): Unit = {
    sprite.addAnimation(State.Attack01, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (0, 0), (2, 1)))
    sprite.addAnimation(State.Dying, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (2, 2), (3, 2)))
    sprite.addAnimation(State.Hurt, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (3, 3), (3, 5)))
    sprite.addAnimation(State.Standing, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (3, 6), (5, 0)), loop = true)
    sprite.addAnimation(State.Running, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (5, 1), (6, 2)), loop = true)
  }

  private def defineEnemyBatAnimation(sprite:EntitySprite): Unit = {
    sprite.addAnimation(State.Attack01, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (0, 0), (1, 0)))
    sprite.addAnimation(State.Dying, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (1, 1), (1, 4)))
    sprite.addAnimation(State.Standing, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (1, 5), (2, 5)), loop = true)
    sprite.addAnimation(State.Running, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (1, 5), (2, 5)), loop = true)
    sprite.addAnimation(State.Hurt, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (2, 6), (3, 2)))
  }

  private def defineEnemyWizardAnimation(sprite:EntitySprite): Unit = {
    sprite.addAnimation(State.Attack01, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (0, 0), (1, 0)))
    sprite.addAnimation(State.Attack02, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (1, 1), (2, 1)))
    sprite.addAnimation(State.Attack03, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (3, 2), (4, 2)), loop = true)
    sprite.addAnimation(State.Dying, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (2, 2), (3, 1)))
    sprite.addAnimation(State.Standing, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (3, 2), (4, 2)), loop = true)
    sprite.addAnimation(State.Running, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (4, 3), (5, 3)), loop = true)
    sprite.addAnimation(State.Hurt, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (5, 4), (5, 6)))
  }

  private def defineAttackFireballAnimation(sprite: EntitySprite):Unit = {
    sprite.addAnimation(State.Dying, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (0, 0), (0, 6)))
    sprite.addAnimation(State.Standing, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (1, 0), (1, 5)), loop = true)
  }

  private def defineAttackEnergyBallAnimation(sprite: EntitySprite):Unit = {
    sprite.addAnimation(State.Dying, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (0, 0), (0, 6)))
    sprite.addAnimation(State.Standing, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (1, 0), (2, 1)), loop = true)
  }

  private  def defineEnemySkeletonAnimation(sprite:EntitySprite): Unit = {
    sprite.addAnimation(State.Attack01, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (0, 0), (1, 0)))
    sprite.addAnimation(State.Dying, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (1, 1), (1, 4)))
    sprite.addAnimation(State.Standing, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (1, 5), (2, 1)), loop = true)
    sprite.addAnimation(State.Hurt, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (2, 6), (3, 2)))
    sprite.addAnimation(State.Running, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (3, 3), (3, 6)), loop = true)
  }

  private  def defineEnemySlimeAnimation(sprite:EntitySprite): Unit = {
    sprite.addAnimation(State.Attack01, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (0, 0), (0, 4)))
    sprite.addAnimation(State.Dying, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (0, 5), (1, 1)))
    sprite.addAnimation(State.Hurt, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (1, 2), (1, 5)))
    sprite.addAnimation(State.Standing, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (1, 6), (2, 2)), loop = true)
    sprite.addAnimation(State.Running, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (2, 3), (2, 6)), loop = true)
  }

  private  def defineEnemyPacmanAnimation(sprite:EntitySprite): Unit = {
    sprite.addAnimation(State.Attack01, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (0, 0), (0, 1)))
    sprite.addAnimation(State.Dying, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (0, 0), (0, 1)))
    sprite.addAnimation(State.Hurt, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (0, 0), (0, 1)))
    sprite.addAnimation(State.Standing, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (0, 0), (0, 1)), loop = true)
    sprite.addAnimation(State.Running, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (0, 0), (0, 1)), loop = true)
  }

  private def defineDoorAnimation(sprite: EntitySprite): Unit = {
    sprite.addAnimation(State.Standing, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (0, 0), (0, 0)))
    sprite.addAnimation(State.Opening, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (0, 0), (0, 3)))
  }

  private def defineChestAnimation(sprite: EntitySprite): Unit = {
    sprite.addAnimation(State.Standing, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (0, 0), (0, 0)))
    sprite.addAnimation(State.Opening, this.createAnimation(sprite, DEFAULT_COLUMNS_NUMBER, (0, 0), (0, 1)))
  }

  private def definePortalAnimation(sprite: EntitySprite): Unit = {
    sprite.addAnimation(State.Standing, this.createAnimation(sprite, CUSTOM_COLUMNS_NUMBER, (0, 0), (0, 7)), loop = true)
    sprite.addAnimation(State.Opening, this.createAnimation(sprite, CUSTOM_COLUMNS_NUMBER, (1, 0), (1, 7)))
    sprite.addAnimation(State.Closed, this.createAnimation(sprite, CUSTOM_COLUMNS_NUMBER, (1, 3), (1, 3)))
  }
}

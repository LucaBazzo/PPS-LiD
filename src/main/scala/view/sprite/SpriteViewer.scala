package view.sprite

import com.badlogic.gdx.graphics.g2d.Batch
import model.entity.{Entity, EntityType}
import utils.SpritesConstants._
trait SpriteViewer {

  def loadSprites(entities: List[Entity]): Unit
  def updateSprites(deltaTime: Float): Unit
  def drawSprites(): Unit
}

class SpriteViewerImpl(batch: Batch) extends SpriteViewer {

  private val spriteFactory: SpriteFactory = new SpriteFactoryImpl()

  private var sprites: Map[Entity, Option[EntitySprite]] = Map()

  override def loadSprites(entities: List[Entity]): Unit = {
    val newEntities = entities.filterNot((e: Entity) => sprites.contains(e))
    for(newEntity <- newEntities) createEntitySprite(newEntity)

    //I remove the sprites not more present
    this.sprites = this.sprites.filter(m => entities.contains(m._1))
  }

  override def updateSprites(deltaTime: Float): Unit =
    for((e, s) <- sprites) if(s.isDefined) s.get.update(deltaTime, e)

  override def drawSprites(): Unit = this.sprites.values.foreach((sprite: Option[EntitySprite]) =>
    if(sprite.isDefined) sprite.get.draw(this.batch))

  private def createEntitySprite(entity: Entity): Unit = {
    sprites += (entity -> getSprite(entity))
  }

  private def getSprite(entity: Entity): Option[EntitySprite] = {
    entity.getType match {
      case EntityType.Hero => Option(spriteFactory.createEntitySprite(
        entity.getType, SPRITES_PACK_LOCATION, HERO_REGION_NAME, HERO_SPRITE_WIDTH,
        HERO_SPRITE_HEIGHT, HERO_SPRITE_MULTIPLIER))
      case EntityType.Arrow => Option(spriteFactory.createEntitySprite(
        entity.getType, SPRITES_PACK_LOCATION, ARROW_REGION_NAME, ARROW_SPRITE_WIDTH,
        ARROW_SPRITE_HEIGHT, ARROW_SPRITE_MULTIPLIER))
      case EntityType.EnemySkeleton => Option(spriteFactory.createEntitySprite(
        entity.getType, SKELETON_PACK_LOCATION, SKELETON_REGION_NAME, SKELETON_SPRITE_WIDTH,
        SKELETON_SPRITE_HEIGHT, SKELETON_SPRITE_MULTIPLIER))
      case EntityType.EnemySlime => Option(spriteFactory.createEntitySprite(
        entity.getType, SLIME_PACK_LOCATION, SLIME_REGION_NAME, SLIME_SPRITE_WIDTH,
        SLIME_SPRITE_HEIGHT, SLIME_SPRITE_MULTIPLIER))
      case EntityType.EnemyPacman => Option(spriteFactory.createEntitySprite(
        entity.getType, PACMAN_PACK_LOCATION, PACMAN_REGION_NAME, PACMAN_SPRITE_WIDTH,
        PACMAN_SPRITE_HEIGHT, PACMAN_SPRITE_MULTIPLIER))
      case EntityType.EnemyBat => Option(spriteFactory.createEntitySprite(
        entity.getType, BAT_PACK_LOCATION, BAT_REGION_NAME, BAT_SPRITE_WIDTH,
        BAT_SPRITE_HEIGHT, BAT_SPRITE_MULTIPLIER))
      case EntityType.EnemyWorm => Option(spriteFactory.createEntitySprite(
        entity.getType, WORM_PACK_LOCATION, WORM_REGION_NAME, WORM_SPRITE_WIDTH,
        WORM_SPRITE_HEIGHT, WORM_SPRITE_MULTIPLIER))
      case EntityType.EnemyBossWizard => Option(spriteFactory.createEntitySprite(
        entity.getType, WIZARD_PACK_LOCATION, WIZARD_REGION_NAME, WIZARD_SPRITE_WIDTH,
        WIZARD_SPRITE_HEIGHT, WIZARD_SPRITE_MULTIPLIER))
      case EntityType.AttackFireBall => Option(spriteFactory.createEntitySprite(
        entity.getType, FIREBALL_PACK_LOCATION, FIREBALL_REGION_NAME, FIREBALL_SPRITE_WIDTH,
        FIREBALL_SPRITE_HEIGHT, FIREBALL_SPRITE_MULTIPLIER))
      case EntityType.AttackEnergyBall => Option(spriteFactory.createEntitySprite(
        entity.getType, ENERGY_BALL_PACK_LOCATION, ENERGY_BALL_REGION_NAME, ENERGY_BALL_SPRITE_WIDTH,
        ENERGY_BALL_SPRITE_HEIGHT, ENERGY_BALL_SPRITE_MULTIPLIER))
      case EntityType.ArmorItem => Option(spriteFactory.createItemSprite(
        entity.getType, SPRITES_PACK_LOCATION, ARMOR_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH,
        ITEM_SPRITE_HEIGHT, ARMOR_ITEM_SPRITE_MULTIPLIER))
      case EntityType.CakeItem => Option(spriteFactory.createItemSprite(
        entity.getType, SPRITES_PACK_LOCATION, CAKE_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH,
        ITEM_SPRITE_HEIGHT, CAKE_ITEM_SPRITE_MULTIPLIER))
      case EntityType.BootsItem => Option(spriteFactory.createItemSprite(
        entity.getType, SPRITES_PACK_LOCATION, BOOTS_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH,
        ITEM_SPRITE_HEIGHT, BOOTS_ITEM_SPRITE_MULTIPLIER))
      case EntityType.ShieldItem => Option(spriteFactory.createItemSprite(
        entity.getType, SPRITES_PACK_LOCATION, SHIELD_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH,
        ITEM_SPRITE_HEIGHT, SHIELD_ITEM_SPRITE_MULTIPLIER))
      case EntityType.MapItem => Option(spriteFactory.createItemSprite(
        entity.getType, SPRITES_PACK_LOCATION, MAP_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH,
        ITEM_SPRITE_HEIGHT, MAP_ITEM_SPRITE_MULTIPLIER))
      case EntityType.WrenchItem => Option(spriteFactory.createItemSprite(
        entity.getType, SPRITES_PACK_LOCATION, WRENCH_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH,
        ITEM_SPRITE_HEIGHT, WRENCH_ITEM_SPRITE_MULTIPLIER))
      case EntityType.KeyItem => Option(spriteFactory.createItemSprite(
        entity.getType, SPRITES_PACK_LOCATION, KEY_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH,
        ITEM_SPRITE_HEIGHT, KEY_ITEM_SPRITE_MULTIPLIER))
      case EntityType.SmallPotionItem => Option(spriteFactory.createItemSprite(
        entity.getType, SPRITES_PACK_LOCATION, SMALL_POTION_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH,
        ITEM_SPRITE_HEIGHT, SMALL_POTION_ITEM_SPRITE_MULTIPLIER))
      case EntityType.PotionItem => Option(spriteFactory.createItemSprite(
        entity.getType, SPRITES_PACK_LOCATION, LARGE_POTION_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH,
        ITEM_SPRITE_HEIGHT, LARGE_POTION_ITEM_SPRITE_MULTIPLIER))
      case EntityType.LargePotionItem => Option(spriteFactory.createItemSprite(
        entity.getType, SPRITES_PACK_LOCATION, HUGE_POTION_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH,
        ITEM_SPRITE_HEIGHT, HUGE_POTION_ITEM_SPRITE_MULTIPLIER))
      case EntityType.HugePotionItem => Option(spriteFactory.createItemSprite(
        entity.getType, SPRITES_PACK_LOCATION, SKELETON_KEY_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH,
        ITEM_SPRITE_HEIGHT, SKELETON_KEY_ITEM_SPRITE_MULTIPLIER))
      case EntityType.SkeletonKeyItem => Option(spriteFactory.createItemSprite(
        entity.getType, SPRITES_PACK_LOCATION, BOW_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH,
        ITEM_SPRITE_HEIGHT, BOW_ITEM_SPRITE_MULTIPLIER))
      case EntityType.BowItem => Option(spriteFactory.createItemSprite(
        entity.getType, SPRITES_PACK_LOCATION, SWORD_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH,
        ITEM_SPRITE_HEIGHT, SWORD_ITEM_SPRITE_MULTIPLIER))
      case EntityType.BFSwordItem => Option(spriteFactory.createItemSprite(
        entity.getType, SPRITES_PACK_LOCATION, SPRITES_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH,
        ITEM_SPRITE_HEIGHT, SPRITES_ITEM_SPRITE_MULTIPLIER))
      case EntityType.Door => Option(spriteFactory.createEntitySprite(
        entity.getType, IRON_DOOR_PACK_LOCATION, IRON_DOOR_REGION_NAME, DOOR_SPRITE_WIDTH,
        DOOR_SPRITE_HEIGHT, DOOR_SPRITE_MULTIPLIER))
      case EntityType.Chest => Option(spriteFactory.createEntitySprite(
        entity.getType, CHEST_PACK_LOCATION, CHEST_REGION_NAME, CHEST_SPRITE_WIDTH,
        CHEST_SPRITE_HEIGHT, CHEST_SPRITE_MULTIPLIER))
      case EntityType.Portal => Option(spriteFactory.createEntitySprite(
        entity.getType, PORTAL_PACK_LOCATION, PORTAL_REGION_NAME, PORTAL_SPRITE_WIDTH,
        PORTAL_SPRITE_HEIGHT, PORTAL_SPRITE_MULTIPLIER))
      case _ => None
    }
  }
}

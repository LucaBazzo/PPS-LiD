package view.screens.sprites

import com.badlogic.gdx.graphics.g2d.Batch
import model.entities.{Entity, EntityType}
import utils.SpritesConstants._
trait SpriteViewer {

  def loadSprites(entities: List[Entity]): Unit
  def updateSprites(deltaTime: Float): Unit
  def drawSprites(): Unit
}

class SpriteViewerImpl(batch: Batch) extends SpriteViewer {

  private val spriteFactory: SpriteFactory = new SpriteFactoryImpl()

  private var sprites: Map[Entity, EntitySprite] = Map()

  override def loadSprites(entities: List[Entity]): Unit = {
    val newEntities = entities.filterNot((e: Entity) => sprites.contains(e))
    for(newEntity <- newEntities) createEntitySprite(newEntity)

    //I remove the sprites not more present
    this.sprites = this.sprites.filter(m => entities.contains(m._1))
  }

  override def updateSprites(deltaTime: Float): Unit = for((e, s) <- sprites) if(s != null) s.update(deltaTime, e)

  override def drawSprites(): Unit = this.sprites.values.foreach((sprite: EntitySprite) => if(sprite != null) sprite.draw(this.batch))

  private def createEntitySprite(entity: Entity): Unit = {
    sprites += (entity -> getSprite(entity))
  }

  // TODO: qualche magic number qui sotto

  private def getSprite(entity: Entity): EntitySprite = entity.getType match {
    case EntityType.Enemy | EntityType.Immobile | EntityType.Mobile => null
    case EntityType.Hero => spriteFactory.createEntitySprite(entity.getType, SPRITES_PACK_LOCATION, HERO_REGION_NAME, HERO_SPRITE_WIDTH , HERO_SPRITE_HEIGHT, 1)
    case EntityType.Arrow => spriteFactory.createEntitySprite(entity.getType, SPRITES_PACK_LOCATION, ARROW_REGION_NAME, ARROW_SPRITE_WIDTH, ARROW_SPRITE_HEIGHT, 0.25f)
    case EntityType.EnemySkeleton => spriteFactory.createEntitySprite(entity.getType, SKELETON_PACK_LOCATION, SKELETON_REGION_NAME, SKELETON_SPRITE_WIDTH, SKELETON_SPRITE_HEIGHT, 0.9f)
    case EntityType.EnemySlime => spriteFactory.createEntitySprite(entity.getType, SLIME_PACK_LOCATION, SLIME_REGION_NAME, SLIME_SPRITE_WIDTH, SLIME_SPRITE_HEIGHT, 1.9f)
    case EntityType.EnemyPacman => spriteFactory.createEntitySprite(entity.getType, PACMAN_PACK_LOCATION, PACMAN_REGION_NAME, PACMAN_SPRITE_WIDTH, PACMAN_SPRITE_HEIGHT, 1.9f)
    case EntityType.EnemyBat => spriteFactory.createEntitySprite(entity.getType, BAT_PACK_LOCATION, BAT_REGION_NAME, BAT_SPRITE_WIDTH, BAT_SPRITE_HEIGHT, 1f)
    case EntityType.EnemyWorm => spriteFactory.createEntitySprite(entity.getType, WORM_PACK_LOCATION, WORM_REGION_NAME, WORM_SPRITE_WIDTH, WORM_SPRITE_HEIGHT, 0.8f)
    case EntityType.EnemyBossWizard => spriteFactory.createEntitySprite(entity.getType, WIZARD_PACK_LOCATION, WIZARD_REGION_NAME, WIZARD_SPRITE_WIDTH, WIZARD_SPRITE_HEIGHT, 0.9f)
    case EntityType.AttackFireBall => spriteFactory.createEntitySprite(entity.getType, FIREBALL_PACK_LOCATION, FIREBALL_REGION_NAME, FIREBALL_SPRITE_WIDTH, FIREBALL_SPRITE_HEIGHT, 1.5f)
    case EntityType.AttackEnergyBall => spriteFactory.createEntitySprite(entity.getType, ENERGY_BALL_PACK_LOCATION, ENERGY_BALL_REGION_NAME, ENERGY_BALL_SPRITE_WIDTH, ENERGY_BALL_SPRITE_HEIGHT, 0.5f)
    case EntityType.ArmorItem => spriteFactory.createItemSprite(entity.getType, SPRITES_PACK_LOCATION, ARMOR_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH, ITEM_SPRITE_HEIGHT, 1, 0, 0)
    case EntityType.CakeItem => spriteFactory.createItemSprite(entity.getType, SPRITES_PACK_LOCATION, CAKE_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH, ITEM_SPRITE_HEIGHT, 1, 0, 1)
    case EntityType.BootsItem => spriteFactory.createItemSprite(entity.getType, SPRITES_PACK_LOCATION, BOOTS_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH, ITEM_SPRITE_HEIGHT, 1, 0, 2)
    case EntityType.ShieldItem => spriteFactory.createItemSprite(entity.getType, SPRITES_PACK_LOCATION, SHIELD_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH, ITEM_SPRITE_HEIGHT, 1, 0, 3)
    case EntityType.MapItem => spriteFactory.createItemSprite(entity.getType, SPRITES_PACK_LOCATION, MAP_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH, ITEM_SPRITE_HEIGHT, 1, 0, 4)
    case EntityType.WrenchItem => spriteFactory.createItemSprite(entity.getType, SPRITES_PACK_LOCATION, WRENCH_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH, ITEM_SPRITE_HEIGHT, 1, 0, 5)
    case EntityType.KeyItem => spriteFactory.createItemSprite(entity.getType, SPRITES_PACK_LOCATION, KEY_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH, ITEM_SPRITE_HEIGHT, 1, 0, 6)
    case EntityType.SmallPotionItem => spriteFactory.createItemSprite(entity.getType, SPRITES_PACK_LOCATION, SMALL_POTION_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH, ITEM_SPRITE_HEIGHT, 1, 1, 0)
    case EntityType.PotionItem => spriteFactory.createItemSprite(entity.getType, SPRITES_PACK_LOCATION, LARGE_POTION_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH, ITEM_SPRITE_HEIGHT, 1, 1, 1)
    case EntityType.LargePotionItem => spriteFactory.createItemSprite(entity.getType, SPRITES_PACK_LOCATION, HUGE_POTION_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH, ITEM_SPRITE_HEIGHT, 1, 1, 2)
    case EntityType.HugePotionItem => spriteFactory.createItemSprite(entity.getType, SPRITES_PACK_LOCATION, SKELETON_KEY_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH, ITEM_SPRITE_HEIGHT, 1, 1, 3)
    case EntityType.SkeletonKeyItem => spriteFactory.createItemSprite(entity.getType, SPRITES_PACK_LOCATION, BOW_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH, ITEM_SPRITE_HEIGHT, 1, 1, 4)
    case EntityType.BowItem => spriteFactory.createItemSprite(entity.getType, SPRITES_PACK_LOCATION, SWORD_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH, ITEM_SPRITE_HEIGHT, 1, 1, 5)
    case EntityType.BFSwordItem => spriteFactory.createItemSprite(entity.getType, SPRITES_PACK_LOCATION, SPRITES_ITEM_REGION_NAME, ITEM_SPRITE_WIDTH, ITEM_SPRITE_HEIGHT, 1, 1, 6)
    case EntityType.Door => spriteFactory.createEntitySprite(entity.getType, IRON_DOOR_PACK_LOCATION, IRON_DOOR_REGION_NAME, DOOR_SPRITE_WIDTH, DOOR_SPRITE_HEIGHT, 1)
    case EntityType.Chest => spriteFactory.createEntitySprite(entity.getType, CHEST_PACK_LOCATION, CHEST_REGION_NAME, CHEST_SPRITE_WIDTH, CHEST_SPRITE_HEIGHT, 0.2f)
    case EntityType.Portal => spriteFactory.createEntitySprite(entity.getType, PORTAL_PACK_LOCATION, PORTAL_REGION_NAME, PORTAL_SPRITE_WIDTH, PORTAL_SPRITE_HEIGHT, 1)
    case _ => null
  }
}

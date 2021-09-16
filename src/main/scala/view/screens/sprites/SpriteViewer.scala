package view.screens.sprites

import com.badlogic.gdx.graphics.g2d.Batch
import model.entities.{Entity, EntityType, State}
import utils.ApplicationConstants
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

  override def updateSprites(deltaTime: Float): Unit = for((e,s) <- sprites) if(s != null) s.update(deltaTime, e)

  override def drawSprites(): Unit = this.sprites.values.foreach((sprite: EntitySprite) => if(sprite != null) sprite.draw(this.batch))

  private def createEntitySprite(entity: Entity): Unit = {
    sprites += (entity -> getSprite(entity))
  }

  // TODO: rimuovere magic numbers

  private def getSprite(entity: Entity): EntitySprite = entity.getType match {
    case EntityType.Enemy | EntityType.Immobile | EntityType.Mobile => null
    case EntityType.Hero => spriteFactory.createHeroSprite(ApplicationConstants.SPRITES_PACK_LOCATION,
      "hero", HERO_SPRITE_WIDTH ,HERO_SPRITE_HEIGHT)
    case EntityType.Arrow => spriteFactory.createEntitySprite(entity.getType, ApplicationConstants.SPRITES_PACK_LOCATION,
      "arrow", ARROW_SPRITE_WIDTH, ARROW_SPRITE_HEIGHT, 10, 1, 2)
    case EntityType.EnemySkeleton => spriteFactory.createEntitySprite(entity.getType, "assets/sprites/skeleton.pack",
      "skeleton", SKELETON_SPRITE_WIDTH, SKELETON_SPRITE_HEIGHT, SKELETON_SPRITE_WIDTH, SKELETON_SPRITE_HEIGHT, 0.9f)
    case EntityType.EnemySlime => spriteFactory.createEntitySprite(entity.getType,"assets/sprites/slime.pack",
      "slime", SLIME_SPRITE_WIDTH, SLIME_SPRITE_HEIGHT, SLIME_SPRITE_WIDTH, SLIME_SPRITE_HEIGHT, 1.3f)
    case EntityType.EnemyWorm => spriteFactory.createEntitySprite(entity.getType,"assets/sprites/worm.pack",
      "worm", WORM_SPRITE_WIDTH, WORM_SPRITE_HEIGHT, WORM_SPRITE_WIDTH, WORM_SPRITE_HEIGHT, 0.8f)
    case EntityType.EnemyBossWizard => spriteFactory.createEntitySprite(entity.getType,"assets/sprites/evil_wizard.pack",
      "evil_wizard", WIZARD_SPRITE_WIDTH, WIZARD_SPRITE_HEIGHT, WIZARD_SPRITE_WIDTH, WIZARD_SPRITE_HEIGHT, 0.9f)
//    case EntityType.EnemyBossReaper => spriteFactory.createEntitySprite(entity.getType,"assets/sprites/reaper.pack",
//      "reaper", 211, 130, entity.getSize._1*500, entity.getSize._2*250, 1)
    case EntityType.AttackFireBall => spriteFactory.createEntitySprite(entity.getType,"assets/sprites/fireball.pack",
      "fireball", FIREBALL_SPRITE_WIDTH, FIREBALL_SPRITE_HEIGHT, FIREBALL_SPRITE_WIDTH, FIREBALL_SPRITE_HEIGHT, 1.5f)
    case EntityType.AttackEnergyBall => spriteFactory.createEntitySprite(entity.getType,"assets/sprites/energy_ball.pack",
      "energy_ball", ENERGYBALL_SPRITE_WIDTH, ENERGYBALL_SPRITE_HEIGHT, ENERGYBALL_SPRITE_WIDTH, ENERGYBALL_SPRITE_HEIGHT, 0.5f)
    case EntityType.AttackSmite => spriteFactory.createEntitySprite(entity.getType,"assets/sprites/reaper.pack",
      "reaper", REAPER_SPRITE_WIDTH, REAPER_SPRITE_HEIGHT, entity.getSize._1, entity.getSize._2, 100)
    case EntityType.ArmorItem =>
      createItemSprite(entity, 0, 0)
    case EntityType.CakeItem =>
      createItemSprite(entity, 0, 1)
    case EntityType.BootsItem =>
      createItemSprite(entity, 0, 2)
    case EntityType.ShieldItem =>
      createItemSprite(entity, 0, 3)
    case EntityType.MapItem =>
      createItemSprite(entity, 0, 4)
    case EntityType.WrenchItem =>
      createItemSprite(entity, 0, 5)
    case EntityType.KeyItem =>
      createItemSprite(entity, 0, 6)
    case EntityType.SmallPotionItem =>
      createItemSprite(entity, 1, 0)
    case EntityType.PotionItem =>
      createItemSprite(entity, 1, 1)
    case EntityType.LargePotionItem =>
      createItemSprite(entity, 1, 2)
    case EntityType.HugePotionItem =>
      createItemSprite(entity, 1, 3)
    case EntityType.SkeletonKeyItem =>
      createItemSprite(entity, 1, 4)
    case EntityType.BowItem =>
      createItemSprite(entity, 1, 5)
    case EntityType.BFSwordItem =>
      createItemSprite(entity, 1, 6)
    case EntityType.Door =>
      this.createDoorSprite(entity)
    case EntityType.Chest =>
      this.createChestSprite(entity)
    case EntityType.Portal =>
      this.createPortalSprite(entity)
    case EntityType.Enemy | EntityType.Immobile | EntityType.Mobile | EntityType.Ladder | EntityType.Platform => null
    case _ => null
  }

  private def createItemSprite(entity: Entity, row: Int, column: Int): EntitySprite = {
    val sprite = spriteFactory.createEntitySprite(entity.getType,
      ApplicationConstants.SPRITES_PACK_LOCATION, "items", ITEM_SPRITE_WIDTH,
      ITEM_SPRITE_HEIGHT, ITEM_ENTITY_SPRITE_SIDE, ITEM_ENTITY_SPRITE_SIDE, 1)
    sprite.addAnimation(State.Standing,
      spriteFactory.createSpriteAnimation(sprite, row, column, column))
    sprite
  }

  private def createPortalSprite(entity: Entity): EntitySprite = {
    val sprite = spriteFactory.createEntitySprite(entity.getType, "assets/sprites/portal.pack", "portal", PORTAL_SPRITE_WIDTH, PORTAL_SPRITE_HEIGHT,
      PORTAL_SPRITE_WIDTH, PORTAL_SPRITE_HEIGHT, 1)
    sprite.addAnimation(State.Standing,
      spriteFactory.createSpriteAnimation(sprite, 0, 0, 7), loop = true)
    sprite.addAnimation(State.Opening,
      spriteFactory.createSpriteAnimation(sprite, 1, 0, 7))
    sprite.addAnimation(State.Closed,
      spriteFactory.createSpriteAnimation(sprite, 1, 3, 3))
    sprite
  }

  private def createChestSprite(entity: Entity): EntitySprite = {
    val sprite = spriteFactory.createEntitySprite(entity.getType, "assets/sprites/chest.pack", "ChestClosed", CHEST_SPRITE_WIDTH, CHEST_SPRITE_HEIGHT,
      CHEST_SPRITE_WIDTH, CHEST_SPRITE_HEIGHT, 0.2f)
    sprite.addAnimation(State.Standing,
      spriteFactory.createSpriteAnimation(sprite, 0, 0, 0))
    sprite.addAnimation(State.Opening,
      spriteFactory.createSpriteAnimation(sprite, 0, 0, 1))
    sprite
  }

  private def createDoorSprite(entity: Entity): EntitySprite = {
    val sprite = spriteFactory.createEntitySprite(entity.getType, "assets/sprites/ironDoor.pack", "ironDoor0", DOOR_SPRITE_WIDTH, DOOR_SPRITE_HEIGHT,
      DOOR_SPRITE_WIDTH, DOOR_SPRITE_HEIGHT, 1)
    sprite.addAnimation(State.Standing,
      spriteFactory.createSpriteAnimation(sprite, 0, 0, 0))
    sprite.addAnimation(State.Opening,
      spriteFactory.createSpriteAnimation(sprite, 0, 0, 3))
    sprite
  }
}

package view.screens.sprites

import com.badlogic.gdx.graphics.g2d.Batch
import model.entities.{Entity, EntityType, State}
import utils.ApplicationConstants
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
    case EntityType.Hero => spriteFactory.createHeroSprite(ApplicationConstants.SPRITES_PACK_LOCATION, "hero", 50, 37)
    case EntityType.Arrow => spriteFactory.createEntitySprite(entity.getType, ApplicationConstants.SPRITES_PACK_LOCATION, "arrow", 40, 5, 0.25f)
    case EntityType.EnemySkeleton => spriteFactory.createEntitySprite(entity.getType, "assets/sprites/skeleton.pack", "skeleton", 150, 150, 0.9f)
    case EntityType.EnemySlime => spriteFactory.createEntitySprite(entity.getType,"assets/sprites/slime.pack", "slime", 125, 125, 1.9f)
    case EntityType.EnemyPacman => spriteFactory.createEntitySprite(entity.getType,"assets/sprites/pacman.pack", "pacman", 16, 16, 1.9f)
    case EntityType.EnemyWorm => spriteFactory.createEntitySprite(entity.getType,"assets/sprites/worm.pack", "worm", 90, 90, 0.8f)
    case EntityType.EnemyBossWizard => spriteFactory.createEntitySprite(entity.getType,"assets/sprites/evil_wizard.pack", "evil_wizard", 250, 250, 0.9f)
    case EntityType.AttackFireBall => spriteFactory.createEntitySprite(entity.getType,"assets/sprites/fireball.pack", "fireball", 46, 46, 1.5f)
    case EntityType.AttackEnergyBall => spriteFactory.createEntitySprite(entity.getType,"assets/sprites/energy_ball.pack", "energy_ball", 128, 128, 0.5f)
    case EntityType.ArmorItem => createItemSprite(entity, 0, 0)
    case EntityType.CakeItem => createItemSprite(entity, 0, 1)
    case EntityType.BootsItem => createItemSprite(entity, 0, 2)
    case EntityType.ShieldItem => createItemSprite(entity, 0, 3)
    case EntityType.MapItem => createItemSprite(entity, 0, 4)
    case EntityType.WrenchItem => createItemSprite(entity, 0, 5)
    case EntityType.KeyItem => createItemSprite(entity, 0, 6)
    case EntityType.SmallPotionItem => createItemSprite(entity, 1, 0)
    case EntityType.PotionItem => createItemSprite(entity, 1, 1)
    case EntityType.LargePotionItem => createItemSprite(entity, 1, 2)
    case EntityType.HugePotionItem => createItemSprite(entity, 1, 3)
    case EntityType.SkeletonKeyItem => createItemSprite(entity, 1, 4)
    case EntityType.BowItem => createItemSprite(entity, 1, 5)
    case EntityType.BFSwordItem => createItemSprite(entity, 1, 6)
    case EntityType.Door => spriteFactory.createEntitySprite(entity.getType, "assets/sprites/ironDoor.pack", "ironDoor1", 80, 67, 1)
    case EntityType.Chest => spriteFactory.createEntitySprite(entity.getType, "assets/sprites/chest.pack", "ChestClosed", 64, 68, 1)
    case EntityType.Portal => spriteFactory.createEntitySprite(entity.getType, "assets/sprites/portal.pack", "portal", 64, 62, 1)
    case EntityType.Enemy | EntityType.Immobile | EntityType.Mobile | EntityType.Ladder | EntityType.Platform => null
    case _ => null
  }

  private def createItemSprite(entity: Entity, row: Int, column: Int): EntitySprite = {
    val sprite = spriteFactory.createEntitySprite(entity.getType,
      ApplicationConstants.SPRITES_PACK_LOCATION, "items", 32, 32, 1)
    sprite.addAnimation(State.Standing,
      spriteFactory.createAnimation(sprite, 7, (row, column), (row, column)))
    sprite
  }
}

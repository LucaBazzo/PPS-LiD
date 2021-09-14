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
    case EntityType.Hero => spriteFactory.createHeroSprite(ApplicationConstants.SPRITES_PACK_LOCATION,
      "hero", 50, 37)
    case EntityType.Arrow => spriteFactory.createEntitySprite(entity.getType, ApplicationConstants.SPRITES_PACK_LOCATION,
      "arrow", 40, 5, 10, 1, 2)
    case EntityType.EnemySkeleton => spriteFactory.createEntitySprite(entity.getType, "assets/sprites/skeleton.pack",
      "skeleton", 150, 150, 150, 150, 0.9f)
    case EntityType.EnemySlime => spriteFactory.createEntitySprite(entity.getType,"assets/sprites/slime.pack",
      "slime", 125, 125, 125, 125, 1.3f)
    case EntityType.EnemyWorm => spriteFactory.createEntitySprite(entity.getType,"assets/sprites/worm.pack",
      "worm", 90, 90, 90, 90, 0.8f)
    case EntityType.EnemyBossWizard => spriteFactory.createEntitySprite(entity.getType,"assets/sprites/evil_wizard.pack",
      "evil_wizard", 250, 250, 250, 250, 0.9f)
//    case EntityType.EnemyBossReaper => spriteFactory.createEntitySprite(entity.getType,"assets/sprites/reaper.pack",
//      "reaper", 211, 130, entity.getSize._1*500, entity.getSize._2*250, 1)
    case EntityType.AttackFireBall => spriteFactory.createEntitySprite(entity.getType,"assets/sprites/fireball.pack",
      "fireball", 46, 46, 46, 46, 1.5f)
    case EntityType.AttackEnergyBall => spriteFactory.createEntitySprite(entity.getType,"assets/sprites/energy_ball.pack",
      "energy_ball", 128, 128, 128, 128, 0.5f)
    case EntityType.AttackSmite => spriteFactory.createEntitySprite(entity.getType,"assets/sprites/reaper.pack",
      "reaper", 211, 130, entity.getSize._1, entity.getSize._2, 100)
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
      val sprite = spriteFactory.createEntitySprite(entity.getType, "assets/sprites/ironDoor.pack", "ironDoor0", 80, 67,
        entity.getSize._1 + 0.5f, entity.getSize._2, 120)
      sprite.addAnimation(State.Standing,
        spriteFactory.createSpriteAnimation(sprite, 0, 0, 0))
      sprite.addAnimation(State.Opening,
        spriteFactory.createSpriteAnimation(sprite, 0, 0, 3))
      sprite
    case EntityType.Chest =>
      val sprite = spriteFactory.createEntitySprite(entity.getType, "assets/sprites/chest.pack", "ChestClosed", 64, 68,
        entity.getSize._1, entity.getSize._2, 100)
      sprite.addAnimation(State.Standing,
        spriteFactory.createSpriteAnimation(sprite, 0, 0, 0))
      sprite.addAnimation(State.Opening,
        spriteFactory.createSpriteAnimation(sprite, 0, 0, 1))
      sprite
    case EntityType.Portal =>
      val sprite = spriteFactory.createEntitySprite(entity.getType, "assets/sprites/portal.pack", "portal", 64, 62,
        entity.getSize._1, entity.getSize._2, 150)
      sprite.addAnimation(State.Standing,
        spriteFactory.createSpriteAnimation(sprite, 0, 0, 7), loop = true)
      sprite.addAnimation(State.Opening,
        spriteFactory.createSpriteAnimation(sprite, 1, 0, 7))
      sprite.addAnimation(State.Closed,
        spriteFactory.createSpriteAnimation(sprite, 1, 3, 3))
      sprite
    case EntityType.Enemy | EntityType.Immobile | EntityType.Mobile | EntityType.Ladder | EntityType.Platform => null
    case _ => null
  }

  private def createItemSprite(entity: Entity, row: Int, column: Int): EntitySprite = {
    val sprite = spriteFactory.createEntitySprite(entity.getType,
      ApplicationConstants.SPRITES_PACK_LOCATION, "items", 32,
      34, 15, 15, 1)
    sprite.addAnimation(State.Standing,
      spriteFactory.createSpriteAnimation(sprite, row, column, column))
    sprite
  }
}

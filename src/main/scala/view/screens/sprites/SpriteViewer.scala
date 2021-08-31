package view.screens.sprites

import com.badlogic.gdx.graphics.g2d.Batch
import model.collisions.ImplicitConversions.RichInt
import model.entities.{Entity, EntityType, State}
import utils.ApplicationConstants

trait SpriteViewer {

  def loadSprites(entities: List[Entity])
  def updateSprites(deltaTime: Float)
  def drawSprites()
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

  private def getSprite(entity: Entity): EntitySprite = entity.getType match {
    case EntityType.Hero => spriteFactory.createHeroSprite(ApplicationConstants.SPRITES_PACK_LOCATION, "hero", 50, 37)
    case EntityType.Arrow =>
      val sprite = spriteFactory.createEntitySprite(ApplicationConstants.SPRITES_PACK_LOCATION, "arrow", 40, 5, 10, 1, 2)
      sprite.addAnimation(State.Standing, spriteFactory.createSpriteAnimation(sprite, 0, 0, 0))
      sprite
    case EntityType.Enemy | EntityType.Immobile | EntityType.Mobile => null
    case EntityType.EnemySkeleton =>
      val e:EntitySprite = spriteFactory.createEntitySprite("assets/sprites/skeleton.pack",
        "skeleton", 150, 150, 19.PPM, 23.PPM, 300)
      spriteFactory.defineEnemySkeletonAnimation(e)
      e
    case EntityType.EnemySlime =>
      val e:EntitySprite = spriteFactory.createEntitySprite("assets/sprites/slime.pack",
        "slime", 32, 25, 13.PPM, 13.PPM, 100)
      spriteFactory.defineEnemySlimeAnimation(e)
      e
    case EntityType.EnemyWorm =>
      val e:EntitySprite = spriteFactory.createEntitySprite("assets/sprites/worm.pack",
        "worm", 90, 90, 15.PPM, 15.PPM, 200)
      spriteFactory.defineEnemyWormAnimation(e)
      e
    case EntityType.AttackFireBall =>
      val e:EntitySprite = spriteFactory.createEntitySprite("assets/sprites/fireball.pack",
        "fireball", 46, 46, 5.PPM, 5.PPM, 200)
      spriteFactory.defineAttackFireballAnimation(e)
      e
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
      val sprite = spriteFactory.createEntitySprite("assets/sprites/ironDoor.pack", "ironDoor1", 80, 67,
        entity.getSize._1 + 0.5f, entity.getSize._2, 120)
      sprite.addAnimation(State.Standing,
        spriteFactory.createSpriteAnimation(sprite, 0, 0, 0))
      sprite.addAnimation(State.Opening,
        spriteFactory.createSpriteAnimation(sprite, 0, 0, 2))
      sprite
    case EntityType.Enemy | EntityType.Immobile | EntityType.Mobile => null
    case _ => null
  }

  private def createItemSprite(entity: Entity, row: Int, column: Int): EntitySprite = {
    val sprite = spriteFactory.createEntitySprite(ApplicationConstants.SPRITES_PACK_LOCATION, "items", 32,
      32, entity.getSize._1, entity.getSize._2, 2)
    sprite.addAnimation(State.Standing,
      spriteFactory.createSpriteAnimation(sprite, row, column, column))
    sprite
  }
}

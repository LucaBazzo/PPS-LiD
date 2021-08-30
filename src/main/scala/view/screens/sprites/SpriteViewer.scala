package view.screens.sprites

import com.badlogic.gdx.graphics.g2d.Batch
import model.collisions.ImplicitConversions.RichInt
import model.entities.{Entity, EntityId, State}
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
    case EntityId.Hero => spriteFactory.createHeroSprite(ApplicationConstants.SPRITES_PACK_LOCATION, "hero", 50, 37)
    case EntityId.Arrow =>
      val sprite = spriteFactory.createEntitySprite(ApplicationConstants.SPRITES_PACK_LOCATION, "arrow", 40, 5, 10, 1, 2)
      sprite.addAnimation(State.Standing, spriteFactory.createSpriteAnimation(sprite, 0, 0, 0))
      sprite
    case EntityId.ArmorItem =>
      val sprite = spriteFactory.createEntitySprite(ApplicationConstants.SPRITES_PACK_LOCATION, "items", 32,
        32, entity.getSize._1, entity.getSize._2, 2)
      sprite.addAnimation(State.Standing,
        spriteFactory.createSpriteAnimation(sprite, 0, 0, 0, 0.20f))
      sprite
    case EntityId.Enemy | EntityId.Immobile | EntityId.Mobile => null
    case EntityId.EnemySkeleton =>
      val e:EntitySprite = spriteFactory.createEntitySprite("assets/sprites/skeleton.pack",
        "skeleton", 150, 150, 19.PPM, 23.PPM, 300)
      spriteFactory.defineEnemySkeletonAnimation(e)
      e
    case EntityId.EnemySlime =>
      val e:EntitySprite = spriteFactory.createEntitySprite("assets/sprites/slime.pack",
        "slime", 32, 25, 13.PPM, 13.PPM, 100)
      spriteFactory.defineEnemySlimeAnimation(e)
      e
    case EntityId.EnemyWorm =>
      val e:EntitySprite = spriteFactory.createEntitySprite("assets/sprites/worm.pack",
        "worm", 90, 90, 15.PPM, 15.PPM, 200)
      spriteFactory.defineEnemyWormAnimation(e)
      e
    case EntityId.AttackFireBall =>
      val e:EntitySprite = spriteFactory.createEntitySprite("assets/sprites/fireball.pack",
        "fireball", 46, 46, 5.PPM, 5.PPM, 200)
      spriteFactory.defineAttackFireballAnimation(e)
      e
    case _ => null
  }
}

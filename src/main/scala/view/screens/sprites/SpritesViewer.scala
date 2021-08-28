package view.screens.sprites

import com.badlogic.gdx.graphics.g2d.Batch
import model.collisions.EntityType
import model.collisions.ImplicitConversions.RichInt
import model.entities.{Enemy, EnemyType, Entity}
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
    println("added sprite for " + entity.toString + " type: " + entity.getType)
    sprites += (entity -> getSprite(entity))
  }

  private def getSprite(entity: Entity): EntitySprite = entity.getType match {
    case EntityType.Hero => spriteFactory.createHeroSprite(ApplicationConstants.SPRITES_PACK_LOCATION,
      "hero", 50, 37)
    case EntityType.Enemy => getEnemySprite(entity)
    case EntityType.Mobile => null
    case EntityType.Sword => null
    case EntityType.Immobile => null
    case EntityType.Item => null
    case _ => null
  }

  private def getEnemySprite(entity: Entity): EntitySprite = entity.asInstanceOf[Enemy].getEnemyType match {
    case EnemyType.Skeleton =>
      val e:EntitySprite = spriteFactory.createEntitySprite("assets/sprites/skeleton.pack",
        "skeleton", 150, 150, 10.PPM, 17.PPM, 300)
      spriteFactory.defineEnemySkeletonAnimation(e)
      e
    case EnemyType.Slime =>
      val e:EntitySprite = spriteFactory.createEntitySprite("assets/sprites/enemies_sprites.pack",
        "slime", 31, 20, 13.PPM, 13.PPM, 100)
      spriteFactory.defineEnemySlimeAnimation(e)
      e
    case EnemyType.Worm =>
      val e:EntitySprite = spriteFactory.createEntitySprite("assets/sprites/worm.pack",
        "worm", 90, 90, 15.PPM, 15.PPM, 200)
      spriteFactory.defineEnemyWormAnimation(e)
      e
    case _ => null
  }
}
package view.screens.sprites

import com.badlogic.gdx.graphics.g2d.Batch
import model.collisions.EntityType
import model.entities.{Entity, State}

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
    case EntityType.Hero => spriteFactory.createHeroSprite("hero", 50, 37)
    case EntityType.Enemy => null
    case EntityType.Mobile => null
    case EntityType.Sword => null
    case EntityType.Immobile => null
    case EntityType.Item => null
    case EntityType.Arrow =>
      val sprite = spriteFactory.createEntitySprite("arrow", 40, 5, 10, 1, 2)
      sprite.addAnimation(State.Standing, spriteFactory.createSpriteAnimation(sprite, 0, 0, 0))
      sprite
    case EntityType.ArmorItem =>
      val sprite = spriteFactory.createEntitySprite("items", 32,
        32, entity.getSize._1, entity.getSize._2, 2)
      sprite.addAnimation(State.Standing,
        spriteFactory.createSpriteAnimation(sprite, 0, 0, 0, 0.20f))
      sprite
    case _ => null
  }
}

package model.helpers

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d._
import model.collisions.{CollisionStrategyImpl, ItemCollisionStrategy}
import model.entities.ItemPools.ItemPools
import model.entities._

trait EntitiesFactory {

  def createMobileEntity(): Entity
  def createHeroEntity(): HeroImpl
  def createItem(PoolName: ItemPools): ItemImpl
}

class EntitiesFactoryImpl(private val world: World) extends EntitiesFactory {

  private val itemPool: ItemPool = new ItemPoolImpl()

  override def createMobileEntity(): Entity = {
    val position: (Float, Float) = (1, 1)
    val size: Float = 1f
    val body: Body = defineEntityBody(size, position, BodyDef.BodyType.DynamicBody)
    new MobileEntityImpl(body, (size,size))
  }

  override def createHeroEntity(): HeroImpl = {
    val position: (Float, Float) = (1, 1)
    val size: Float = 1f
    val body: Body = defineEntityBody(size, position, BodyDef.BodyType.DynamicBody)
    val hero: HeroImpl = new HeroImpl(body, (size,size))
    hero.setCollisionStrategy(new CollisionStrategyImpl())
    hero
  }

  override def createItem(PoolName: ItemPools): ItemImpl = {
    val position: (Float, Float) = (4, 2)
    val size: (Float, Float) = (0.5f, 0.5f)
    val body: Body = defineEntityBody(size._1, position, BodyDef.BodyType.StaticBody)
    val item: ItemImpl = itemPool.getItem(body, size, PoolName)
    item.setCollisionStrategy(new ItemCollisionStrategy())
    item
  }

  private def defineEntityBody(size: Float, position: (Float, Float), bodyType: BodyType): Body = {
    val bodyDef: BodyDef = new BodyDef()
    bodyDef.position.set(position._1, position._2)
    bodyDef.`type` = bodyType

    val body: Body = world.createBody(bodyDef)

    val fixtureDef: FixtureDef = new FixtureDef()

    /*fixtureDef.filter.categoryBits = 1
    fixtureDef.filter.maskBits = 2*/

    val shape: CircleShape = new CircleShape()
    shape.setRadius(size)

    fixtureDef.shape = shape

    body.createFixture(fixtureDef)
    body
  }

}

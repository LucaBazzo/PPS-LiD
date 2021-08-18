package model.world

import com.badlogic.gdx.physics.box2d._
import model.Level
import model.collisions.CollisionStrategyImpl
import model.entities.{CakeItem, Entity, ImmobileEntity, Item}

class WorldCreator(private val level: Level, private val world: World) {

  private val rectangle: Entity = createImmobileEntity()
  private val item: Entity = createItem()
  level.addEntity(rectangle)
  level.addEntity(item)

  def createImmobileEntity(): Entity = {
    val position: (Float, Float) = (0, -2)
    val size: (Float,Float) = (5, 0.5f)
    val body: Body = defineRectangleBody(size, position)
    val immobileEntity: Entity = ImmobileEntity(body, size)
    immobileEntity.setCollisionStrategy(new CollisionStrategyImpl())
    immobileEntity
  }

  def createItem(): Entity = {
    val position: (Float, Float) = (2, 2)
    val size: (Float, Float) = (0.5f, 0.5f)
    val body: Body = defineCircleBody(size._1, position)
    val item: Entity = new CakeItem(body, size)
    item.setCollisionStrategy(new CollisionStrategyImpl())
    item
  }

  private def defineRectangleBody(size: (Float,Float), position: (Float, Float)): Body = {
    val bodyDef: BodyDef = new BodyDef()
    val shape: PolygonShape = new PolygonShape()
    val fixtureDef: FixtureDef = new FixtureDef()

    bodyDef.`type` = BodyDef.BodyType.StaticBody
    bodyDef.position.set(position._1, position._2)

    val body: Body = world.createBody(bodyDef)

    /*fixtureDef.filter.categoryBits = 2
    fixtureDef.filter.maskBits = 1*/

    shape.setAsBox(size._1, size._2)
    fixtureDef.shape = shape
    body.createFixture(fixtureDef)

    body
  }

  private def defineCircleBody(size: Float, position: (Float, Float)): Body = {
    val bodyDef: BodyDef = new BodyDef()
    val shape: CircleShape = new CircleShape()
    val fixtureDef: FixtureDef = new FixtureDef()

    bodyDef.`type` = BodyDef.BodyType.StaticBody
    bodyDef.position.set(position._1, position._2)

    val body: Body = world.createBody(bodyDef)

    /*fixtureDef.filter.categoryBits = 2
    fixtureDef.filter.maskBits = 1*/

    shape.setRadius(size)
    fixtureDef.shape = shape
    body.createFixture(fixtureDef)

    body
  }

}

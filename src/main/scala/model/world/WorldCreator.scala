package model.world

import com.badlogic.gdx.physics.box2d._
import model.Level
import model.collisions.CollisionStrategyImpl
import model.entities.{Entity, ImmobileEntity}
import model.helpers.EntitiesBits

class WorldCreator(private val level: Level, private val world: World) {

  private val rectangle: Entity = createImmobileEntity()
  level.addEntity(rectangle)
//  level.addEntity(createLeftWall())

  def createImmobileEntity(): Entity = {
    val position: (Float, Float) = (0, -3)
    val size: (Float,Float) = (10, 0.5f)
    val body: Body = defineRectangleBody(size, position)
    val immobileEntity: Entity = ImmobileEntity(body, size)
    immobileEntity.setCollisionStrategy(new CollisionStrategyImpl())
    immobileEntity
  }

  def createLeftWall(): Entity = {
    val position: (Float, Float) = (-20, 0)
    val size: (Float,Float) = (0.5f, 5f)
    val body: Body = defineRectangleBody(size, position)
    val immobileEntity: Entity = ImmobileEntity(body, size)
    immobileEntity.setCollisionStrategy(new CollisionStrategyImpl())
    immobileEntity
  }

  private def defineRectangleBody(size: (Float,Float), position: (Float, Float)): Body = {
    val bodyDef: BodyDef = new BodyDef()
    val shape: PolygonShape = new PolygonShape()
    val fixtureDef: FixtureDef = new FixtureDef()

    bodyDef.`type` = BodyDef.BodyType.StaticBody
    bodyDef.position.set(position._1, position._2)

    val body: Body = world.createBody(bodyDef)

    fixtureDef.filter.categoryBits = EntitiesBits.WORLD_CATEGORY_BIT
    fixtureDef.filter.maskBits = EntitiesBits.WORLD_COLLISIONS_MASK

    shape.setAsBox(size._1, size._2)
    fixtureDef.shape = shape
    body.createFixture(fixtureDef)

    body
  }

}

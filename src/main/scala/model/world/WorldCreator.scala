package model.world

import com.badlogic.gdx.physics.box2d._
import model.Level
import model.collisions.CollisionStrategyImpl
import model.entities.{Entity, ImmobileEntity}

class WorldCreator(private val level: Level, private val world: World) {

  private val rectangle: Entity = createImmobileEntity((0, -2), (8, 0.5f))
  level.addEntity(rectangle)

  def createImmobileEntity(position: (Float, Float), size: (Float,Float)): Entity = {
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

    fixtureDef.filter.categoryBits = 2
    fixtureDef.filter.maskBits = 1

    shape.setAsBox(size._1, size._2)
    fixtureDef.shape = shape
    body.createFixture(fixtureDef)

    body
  }

}

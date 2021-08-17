package model.helpers

import com.badlogic.gdx.physics.box2d._
import model.HeroMovementStrategy
import model.collisions.CollisionStrategyImpl
import model.entities.{Entity, Hero, HeroImpl, MobileEntityImpl}

trait EntitiesFactory {

  def createMobileEntity(): Entity
  def createHeroEntity(): Hero
}

class EntitiesFactoryImpl(private val world: World) extends EntitiesFactory {

  override def createMobileEntity(): Entity = {
    val position: (Float, Float) = (1, 1)
    val size: Float = 1f
    val body: Body = defineEntityBody(size, position)
    new MobileEntityImpl(body, (size,size))
  }

  override def createHeroEntity(): Hero = {
    val position: (Float, Float) = (1, 1)
    val size: Float = 1f
    val body: Body = defineEntityBody(size, position)
    val hero: Hero = new HeroImpl(body, (size,size))
    hero.setCollisionStrategy(new CollisionStrategyImpl())
    hero.setMovementStrategy(new HeroMovementStrategy(hero))
    hero
  }

  private def defineEntityBody(size: Float, position: (Float, Float)): Body = {
    val bodyDef: BodyDef = new BodyDef()
    bodyDef.position.set(position._1, position._2)
    bodyDef.`type` = BodyDef.BodyType.DynamicBody

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

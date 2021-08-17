package model.helpers

import com.badlogic.gdx.physics.box2d._
import model.collisions.{ApplyDamageToHero, CollisionStrategyImpl}
import model.entities.{EnemyImpl, Entity, HeroImpl, MobileEntityImpl}

trait EntitiesFactory {

  def createMobileEntity(): Entity

  def createHeroEntity(): HeroImpl

  def createEnemyEntity(): EnemyImpl
}

class EntitiesFactoryImpl(private val world: World) extends EntitiesFactory {

  override def createEnemyEntity: EnemyImpl = {
    // TODO: valutare se aggregare con createMobileEntity
    val position: (Float, Float) = (4, 20)
    val size: Float = 0.5f
    val body: Body = defineEntityBody(size, position)
    val enemy:EnemyImpl = new EnemyImpl(body, (size,size), 0)

    enemy.setCollisionStrategy(new ApplyDamageToHero())
//    enemy.setAttackStrategy()
//    enemy.setMovementStrategy()
    enemy
  }

  override def createMobileEntity(): Entity = {
    val position: (Float, Float) = (1, 1)
    val size: Float = 1f
    val body: Body = defineEntityBody(size, position)
    new MobileEntityImpl(body, (size,size))
  }

  override def createHeroEntity(): HeroImpl = {
    val position: (Float, Float) = (1, 1)
    val size: Float = 1f
    val body: Body = defineEntityBody(size, position)
    val hero: HeroImpl = new HeroImpl(body, (size,size))
    hero.setCollisionStrategy(new CollisionStrategyImpl())
    hero
  }

  private def defineEntityBody(size: Float, position: (Float, Float)): Body = {
    val bodyDef: BodyDef = new BodyDef()
    bodyDef.position.set(position._1, position._2)
    bodyDef.`type` = BodyDef.BodyType.DynamicBody

//    BodyDef.BodyType.KinematicBody

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

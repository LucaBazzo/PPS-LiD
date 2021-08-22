package model.helpers

import com.badlogic.gdx.physics.box2d._
import model.Level
import model.attack.RangedArrowAttack
import model.collisions.{CollisionStrategyImpl, DoNothingOnCollision}
import model.entities.{EnemyImpl, HeroImpl}
import model.movement.PatrolAndStopIfFacingHero

trait EntitiesFactory {

  def createHeroEntity(): HeroImpl

  def createEnemyEntity(position: (Float, Float), size:Float): EnemyImpl
}

class EntitiesFactoryImpl(private val world: World, private val level: Level) extends EntitiesFactory {

  override def createEnemyEntity(position: (Float, Float), size:Float): EnemyImpl = {

    val bodyDef: BodyDef = new BodyDef()
    bodyDef.position.set(position._1, position._2)
    bodyDef.`type` = BodyDef.BodyType.DynamicBody

    val body: Body = world.createBody(bodyDef)

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: CircleShape = new CircleShape()
    shape.setRadius(size)
    fixtureDef.shape = shape
    fixtureDef.filter.categoryBits = EntitiesBits.ENEMY_CATEGORY_BIT
    fixtureDef.filter.maskBits = EntitiesBits.ENEMY_COLLISIONS_MASK

    body.createFixture(fixtureDef)

    val enemy:EnemyImpl = EnemyImpl(body, (size,size))

    enemy.setCollisionStrategy(new DoNothingOnCollision())

//    enemy.setAttackStrategy(new DoNotAttack())
//    enemy.setAttackStrategy(new ContactAttackStrategy(enemy, level.getEntity(e => e.isInstanceOf[HeroImpl]), world, level))
//    enemy.setAttackStrategy(new MeleeAttackStrategy(enemy, level.getEntity(e => e.isInstanceOf[HeroImpl]), world, level))
    enemy.setAttackStrategy(new RangedArrowAttack(enemy, level.getEntity(e => e.isInstanceOf[HeroImpl]), world, level))

    enemy.setMovementStrategy(new PatrolAndStopIfFacingHero(enemy, world, level.getEntity(e => e.isInstanceOf[HeroImpl]) ))

    enemy
  }

  override def createHeroEntity(): HeroImpl = {
    val position: (Float, Float) = (-1, 1)
    val size: Float = 1f
    val body: Body = defineEntityBody(size, position)

    val collisionFilter: Filter = new Filter()
    collisionFilter.categoryBits = EntitiesBits.HERO_CATEGORY_BIT
    collisionFilter.maskBits = EntitiesBits.HERO_COLLISIONS_MASK
    body.getFixtureList.toArray().head.setFilterData(collisionFilter)

    val hero: HeroImpl = new HeroImpl(body, (size,size))
    hero.setCollisionStrategy(new CollisionStrategyImpl())
    hero
  }

  private def defineEntityBody(size: Float, position: (Float, Float)): Body = {
    val bodyDef: BodyDef = new BodyDef()
    bodyDef.position.set(position._1, position._2)
    bodyDef.`type` = BodyDef.BodyType.DynamicBody

    val body: Body = world.createBody(bodyDef)

    val fixtureDef: FixtureDef = new FixtureDef()

    val shape: CircleShape = new CircleShape()
    shape.setRadius(size)

    fixtureDef.shape = shape

    body.createFixture(fixtureDef)
    body
  }

}

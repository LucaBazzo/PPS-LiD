package model.helpers

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import model.Level
import model.attack.{DoNotAttack, MeleeSwordAttack, RangedArrowAttack}
import model.collisions.{ApplyDamageToHero, CollisionStrategyImpl, DoNothingOnCollision}
import model.entities.{Attack, EnemyImpl, Entity, HeroImpl, MobileEntityImpl}
import model.movement.{ChaseHero, DoNotMove, PatrolAndStopIfFacingHero, PatrolAndStopIfSeeingHero, PatrolPlatform, PatrolPlatformRandomly, ProjectileTrajectory, WeightlessProjectileTrajectory}

trait EntitiesFactory {
  def createArrowProjectile(startingPoint: Vector2, targetPoint: Vector2, owner:Entity): Attack

  def createHeroEntity(): HeroImpl

  def createEnemyEntity(): EnemyImpl
}

class EntitiesFactoryImpl(private val world: World, private val level: Level) extends EntitiesFactory {


  override def createArrowProjectile(startingPoint: Vector2, targetPoint: Vector2, owner:Entity): Attack = {
    val position: (Float, Float) = (startingPoint.x+2, startingPoint.y+2)
    val size: Float = 0.5f
    val body: Body = defineEntityBody(size, position)
    val attack:Attack = Attack(body, (size,size))
    attack.setMovementStrategy(new ProjectileTrajectory(attack, targetPoint))
    attack.setCollisionStrategy(new ApplyDamageToHero(owner))
    attack
  }

  override def createEnemyEntity: EnemyImpl = {
    val position: (Float, Float) = (4, 20)
    val size: Float = 1f

    val body: Body = defineEntityBody(size, position)

    val collisionFilter: Filter = new Filter()
    collisionFilter.categoryBits = EntitiesBits.ENEMY_CATEGORY_BIT
    collisionFilter.maskBits = EntitiesBits.ENEMY_COLLISIONS_MASK
    body.getFixtureList.toArray().head.setFilterData(collisionFilter)

    val enemy:EnemyImpl = EnemyImpl(body, (size,size))
    enemy.setCollisionStrategy(new DoNothingOnCollision())
//    enemy.setAttackStrategy(new RangedArrowAttack(enemy, level.getEntity(e => e.isInstanceOf[HeroImpl]), world, level, this))
    enemy.setAttackStrategy(new DoNotAttack())
//    enemy.setMovementStrategy(new PatrolAndStopIfSeeingHero(enemy, world, level.getEntity(e => e.isInstanceOf[HeroImpl]), level))
    enemy.setMovementStrategy(new DoNotMove())
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

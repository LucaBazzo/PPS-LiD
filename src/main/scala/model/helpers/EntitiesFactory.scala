package model.helpers

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import model.{HeroAttackStrategy, HeroMovementStrategy}
import model.collisions.CollisionStrategyImpl
import model.entities.{Entity, Hero, HeroImpl, MobileEntityImpl}

trait EntitiesFactory {

  def createMobileEntity(): Entity
  def createHeroEntity(): Hero

  def setWorld(world: World)
  def defineSlidingHero(hero: Hero)
  def defineNormalHero(hero: Hero)
}

object EntitiesFactoryImpl extends EntitiesFactory {

  private var world: World = _

  override def setWorld(world: World): Unit = this.world = world

  override def createMobileEntity(): Entity = {
    val position: (Float, Float) = (1, 1)
    val size: (Float, Float) = (1, 1)
    val body: Body = defineEntityBody(size, position)
    new MobileEntityImpl(body, size)
  }

  override def createHeroEntity(): Hero = {
    val position: (Float, Float) = (1, 1)
    val size: (Float, Float) = (0.85f, 1.4f)
    val body: Body = defineEntityBody(size, position)
    //TODO mettere a posto Impl una volta aggiunte le collisioni
    val hero: HeroImpl = new HeroImpl(body, size)
    hero.setCollisionStrategy(new CollisionStrategyImpl())
    hero.setMovementStrategy(new HeroMovementStrategy(hero))
    hero.setAttackStrategy(new HeroAttackStrategy(hero))
    hero
  }

  private def defineEntityBody(size: (Float, Float), position: (Float, Float)): Body = {
    val bodyDef: BodyDef = new BodyDef()
    val shape: PolygonShape = new PolygonShape()
    val fixtureDef: FixtureDef = new FixtureDef()

    bodyDef.position.set(position._1, position._2)
    bodyDef.`type` = BodyDef.BodyType.DynamicBody

    val body: Body = world.createBody(bodyDef)

    /*fixtureDef.filter.categoryBits = 1
    fixtureDef.filter.maskBits = 2*/

    shape.setAsBox(size._1, size._2)
    fixtureDef.shape = shape
    body.createFixture(fixtureDef)
    body
  }

  override def defineSlidingHero(hero: Hero): Unit ={
    val position: Vector2 = hero.getBody.getPosition

    //this.world.destroyBody(this.hero.getBody)
    //this.hero.setBody(defineEntityBody((0.85f, 0.1f), position))
    defineEntityBody(hero, (0.85f, 0.9f), position)
  }

  private def defineEntityBody(hero: Hero, size: (Float, Float), position: Vector2){
    val shape: PolygonShape = new PolygonShape()
    val fixtureDef: FixtureDef = new FixtureDef()

    hero.getBody.destroyFixture(hero.getBody.getFixtureList.first())

    /*fixtureDef.filter.categoryBits = 1
    fixtureDef.filter.maskBits = 2*/

    shape.setAsBox(size._1, size._2)
    fixtureDef.shape = shape
    hero.getBody.createFixture(fixtureDef)
    hero.getBody.setTransform(position.add(0,-0.5f), 0)
  }

  override def defineNormalHero(hero: Hero): Unit ={
    val position: Vector2 = hero.getBody.getPosition

    //this.world.destroyBody(this.hero.getBody)
    //this.hero.setBody(defineEntityBody((0.85f, 0.1f), position))
    defineNormalEntityBody(hero, (0.85f, 1.4f), position)
  }

  private def defineNormalEntityBody(hero: Hero, size: (Float, Float), position: Vector2){
    val shape: PolygonShape = new PolygonShape()
    val fixtureDef: FixtureDef = new FixtureDef()

    hero.getBody.destroyFixture(hero.getBody.getFixtureList.first())

    /*fixtureDef.filter.categoryBits = 1
    fixtureDef.filter.maskBits = 2*/

    shape.setAsBox(size._1, size._2)
    fixtureDef.shape = shape
    hero.getBody.createFixture(fixtureDef)
    hero.getBody.setTransform(position.add(0,+0.5f), 0)
  }
}

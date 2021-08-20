package model.helpers

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef
import model.collisions.CollisionStrategyImpl
import model.entities._
import model.{CircularMovementStrategy, HeroAttackStrategy, HeroMovementStrategy, Level}


trait EntitiesFactory {

  def createMobileEntity(): Entity
  def createHeroEntity(): Hero

  def setLevel(level: Level)
  def defineSlidingHero(hero: Hero)
  def defineNormalHero(hero: Hero)
  def defineSword(size: (Float, Float), position: (Float, Float), angle: Float): Body
  def defineStaticBody(size: (Float, Float), position: (Float, Float)): Body

  def createAttackPattern(rotatingBodySize: (Float, Float),
                          pivotPoint: (Float, Float),
                          rotatingBodyDistance: (Float, Float),
                          angularVelocity: Float,
                          startingAngle: Float): MobileEntity

  def revoluteJoint(pivotBody: Body, rotatingBody: Body): Joint

  def removeEntity(entity: Entity)

  def destroyBody(body: Body)
  def destroyJoint(joint: Joint)
}

object EntitiesFactoryImpl extends EntitiesFactory {

  private var level: Level = _

  override def setLevel(level: Level): Unit = this.level = level

  override def createMobileEntity(): Entity = {
    val position: (Float, Float) = (1, 1)
    val size: (Float, Float) = (1, 1)
    val body: Body = defineEntityBody(size, position)
    val mobileEntity: MobileEntity = new MobileEntityImpl(body, size)
    this.level.addEntity(mobileEntity)
    mobileEntity
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

    this.level.addEntity(hero)
    hero
  }

  private def defineEntityBody(size: (Float, Float), position: (Float, Float)): Body = {
    val bodyDef: BodyDef = new BodyDef()
    val shape: PolygonShape = new PolygonShape()
    val fixtureDef: FixtureDef = new FixtureDef()

    bodyDef.position.set(position._1, position._2)
    bodyDef.`type` = BodyDef.BodyType.DynamicBody

    val body: Body = this.level.getWorld.createBody(bodyDef)

    fixtureDef.filter.categoryBits = 1
    fixtureDef.filter.maskBits = (2 | 8).asInstanceOf[Short]

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

    fixtureDef.filter.categoryBits = 1
    fixtureDef.filter.maskBits = (2 | 8).asInstanceOf[Short]

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

    fixtureDef.filter.categoryBits = 1
    fixtureDef.filter.maskBits = (2 | 8).asInstanceOf[Short]

    shape.setAsBox(size._1, size._2)
    fixtureDef.shape = shape
    hero.getBody.createFixture(fixtureDef)
    hero.getBody.setTransform(position.add(0,+0.5f), 0)
  }

  override def defineSword(size: (Float, Float), position: (Float, Float), angle: Float = 0): Body = {
    val bodyDef: BodyDef = new BodyDef()
    val shape: PolygonShape = new PolygonShape()
    val fixtureDef: FixtureDef = new FixtureDef()

    bodyDef.position.set(position._1, position._2)
    bodyDef.`type` = BodyDef.BodyType.DynamicBody
    bodyDef.gravityScale = 0
    bodyDef.angle = angle

    val body: Body = this.level.getWorld.createBody(bodyDef)

    fixtureDef.filter.categoryBits = 4
    fixtureDef.filter.maskBits = 8

    shape.setAsBox(size._1, size._2)
    fixtureDef.shape = shape
    fixtureDef.density = 1
    fixtureDef.friction = 0.3f
    fixtureDef.restitution = 0.5f

    body.createFixture(fixtureDef)
    body
  }

  override def defineStaticBody(size: (Float, Float), position: (Float, Float)): Body = {
    val bodyDef: BodyDef = new BodyDef()
    val shape: PolygonShape = new PolygonShape()
    val fixtureDef: FixtureDef = new FixtureDef()

    bodyDef.position.set(position._1, position._2)
    bodyDef.`type` = BodyDef.BodyType.StaticBody

    val body: Body = this.level.getWorld.createBody(bodyDef)

    fixtureDef.filter.categoryBits = 4

    shape.setAsBox(size._1, size._2)
    fixtureDef.shape = shape

    body.createFixture(fixtureDef)
    body
  }

  override def createAttackPattern(rotatingBodySize: (Float, Float),
                                   pivotPoint: (Float, Float),
                                   rotatingBodyDistance: (Float, Float),
                                   angularVelocity: Float,
                                   startingAngle: Float = 0): MobileEntity = {

    val pivotBody: Body = this.defineStaticBody((0.2f, 0.2f), (pivotPoint._1, pivotPoint._2))

    val rotatingBodyPosition = (pivotPoint._1 + rotatingBodyDistance._1, pivotPoint._2 + rotatingBodyDistance._2)
    val rotatingBody: Body = this.defineSword(rotatingBodySize, rotatingBodyPosition, startingAngle)

    //new AttackPatternImpl(this.world, pivotBody,rotatingBody, angularVelocity)
    val circularMobileEntity = new CircularMobileEntity(rotatingBody, rotatingBodySize, pivotBody)
    circularMobileEntity.setMovementStrategy(new CircularMovementStrategy(circularMobileEntity, angularVelocity))
    circularMobileEntity.setCollisionStrategy(new CollisionStrategyImpl)

    this.level.addEntity(circularMobileEntity)
    circularMobileEntity
  }

  override def revoluteJoint(pivotBody: Body, rotatingBody: Body): Joint = {
    val rjd: RevoluteJointDef = new RevoluteJointDef()

    rjd.initialize(pivotBody, rotatingBody, pivotBody.getWorldCenter)
    /*rjd.motorSpeed = 3.14f * 2    //how fast
    rjd.maxMotorTorque = 1000.0f  //how powerful
    rjd.enableMotor = false*/

    this.level.getWorld.createJoint(rjd)
  }

  override def removeEntity(entity: Entity): Unit = this.level.removeEntity(entity)

  override def destroyBody(body: Body): Unit = this.level.getWorld.destroyBody(body)

  override def destroyJoint(joint: Joint): Unit = this.level.getWorld.destroyJoint(joint)
}

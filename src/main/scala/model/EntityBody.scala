package model

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.{Body, BodyDef, FixtureDef, Shape}
import model.helpers.ImplicitConversions._
import model.helpers.EntitiesFactoryImpl

/**
 *
 */
trait EntityBody {

  def getBody: Body

  def createBody(bodyType: BodyType = BodyType.StaticBody,
                 position: (Float, Float) = (0,0),
                 angle: Float = 0, gravityScale: Float = 1.0f): EntityBody

  def setEntityCollisionBit(entityCollisionBit: Short): EntityBody
  def getEntityCollisionBit(): Short
  def setShape(shape: Shape): EntityBody
  def setCollisions(entitiesTypes: Short): EntityBody
  def getEntityCollisions(): Short
  def setFixtureValues(density: Float = 0, friction: Float = 2, restitution: Float = 0, isSensor: Boolean = false): EntityBody
  def createFixture(): Unit

  def setPosition(position: (Float, Float), angle: Float = 0): Unit
  def addCoordinates(x: Float, y: Float): Unit

  def setGravityScale(gravityScale: Float = 1.0f): Unit
}

class EntityBodyImpl extends EntityBody {

  private var body: Body = _
  private val fixtureDef: FixtureDef = new FixtureDef()
  private var shape: Shape = _

  override def getBody: Body = this.body

  override def setEntityCollisionBit(entityCollisionBit: Short): EntityBody = {
    this.fixtureDef.filter.categoryBits = entityCollisionBit
    this
  }

  override def getEntityCollisionBit(): Short = this.fixtureDef.filter.categoryBits

  override def getEntityCollisions(): Short = this.fixtureDef.filter.maskBits

  override def setCollisions(entitiesTypes: Short): EntityBody = {
    this.fixtureDef.filter.maskBits = entitiesTypes
    this
  }

  override def setShape(shape: Shape): EntityBody = {
    this.shape = shape
    this.fixtureDef.shape = shape
    this
  }

  override def setFixtureValues(density: Float = 0, friction: Float = 2, restitution: Float = 0, isSensor: Boolean = false): EntityBody = {
    this.fixtureDef.density = density
    this.fixtureDef.friction = friction
    this.fixtureDef.restitution = restitution
    this.fixtureDef.isSensor = isSensor
    this
  }

  override def createFixture(): Unit = {
    if(!this.body.getFixtureList.isEmpty) {
      this.body.destroyFixture(this.body.getFixtureList.first())
    }
    this.body.createFixture(this.fixtureDef)
  }

  override def createBody(bodyType: BodyType = BodyType.StaticBody,
                          position: (Float, Float) = (0,0),
                          angle: Float = 0, gravityScale: Float = 1.0f): EntityBody = {
    val bodyDef: BodyDef = new BodyDef()

    bodyDef.position.set(position)
    bodyDef.`type` = bodyType
    bodyDef.angle = angle
    bodyDef.gravityScale = gravityScale

    if(this.body != null) EntitiesFactoryImpl.pendingDestroyBody(this.body)
    this.body = EntitiesFactoryImpl.createBody(bodyDef)
    this
  }

  override def setPosition(position: (Float, Float), angle: Float = 0): Unit =
    this.body.setTransform(position, angle)

  override def addCoordinates(x: Float, y: Float): Unit =
    this.body.setTransform(this.body.getPosition.add(x, y), 0)

  override def setGravityScale(gravityScale: Float): Unit = this.body.setGravityScale(gravityScale)
}

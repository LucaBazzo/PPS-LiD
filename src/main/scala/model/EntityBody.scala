package model

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.{Body, BodyDef, FixtureDef, Shape}
import model.collisions.ImplicitConversions._
import model.helpers.EntitiesFactoryImpl

trait EntityBody {

  def getBody: Body

  def createBody(bodyType: BodyType = BodyType.StaticBody,
                 size: (Float, Float) = (1,1),
                 position: (Float, Float) = (0,0),
                 angle: Float = 0, gravity: Boolean = true): EntityBody

  def setEntityType(entityType: Short): EntityBody
  def setShape(shape: Shape): EntityBody
  def setCollisions(entitiesTypes: Short): EntityBody
  def setFixtureValues(density: Float = 0, friction: Float = 2, restitution: Float = 0): EntityBody
  def createFixture()

  def setPosition(position: (Float, Float), angle: Float = 0)
  def addCoordinates(x: Float, y: Float)
}

class EntityBodyImpl extends EntityBody {

  private var body: Body = _
  private val fixtureDef: FixtureDef = new FixtureDef()
  private var shape: Shape = _

  override def getBody: Body = this.body

  override def setEntityType(entityType: Short): EntityBody = {
    this.fixtureDef.filter.categoryBits = entityType
    this
  }

  override def setCollisions(entitiesTypes: Short): EntityBody = {
    /*def setCollisions_(entitiesTypes: List[Short]): Short = entitiesTypes match {
      case Nil => 0
      case h :: t =>  h | setCollisions(t)
    }
    this.fixtureDef.filter.maskBits = setCollisions_(entitiesTypes)*/
    this.fixtureDef.filter.maskBits = entitiesTypes
    this
  }

  override def setShape(shape: Shape): EntityBody = {
    this.shape = shape
    this.fixtureDef.shape = shape
    this
  }

  override def setFixtureValues(density: Float = 0, friction: Float = 2, restitution: Float = 0): EntityBody = {
    this.fixtureDef.density = density
    this.fixtureDef.friction = friction
    this.fixtureDef.restitution = restitution
    this
  }

  override def createFixture(): Unit = {
    if(!this.body.getFixtureList.isEmpty) {
      this.body.destroyFixture(this.body.getFixtureList.first())
    }
    this.body.createFixture(this.fixtureDef)
  }

  override def createBody(bodyType: BodyType = BodyType.StaticBody,
                          size: (Float, Float) = (1,1),
                          position: (Float, Float) = (0,0),
                          angle: Float = 0, gravity: Boolean = true): EntityBody = {
    val bodyDef: BodyDef = new BodyDef()

    bodyDef.position.set(position)
    bodyDef.`type` = bodyType
    bodyDef.angle = angle

    if(!gravity) bodyDef.gravityScale = 0

    if(this.body != null) EntitiesFactoryImpl.destroyBody(this.body)
    this.body = EntitiesFactoryImpl.createBody(bodyDef)
    this
  }

  override def setPosition(position: (Float, Float), angle: Float = 0): Unit = this.body.setTransform(position, angle)

  override def addCoordinates(x: Float, y: Float): Unit = this.body.setTransform(this.body.getPosition.add(x,y), 0)
}

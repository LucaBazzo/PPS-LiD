package view.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.{Body, BodyDef, CircleShape, FixtureDef, World}

class Hero(w: World) extends Sprite {

  private val world: World = w

  private val radius:Float = 1f

  private var body: Body = _

  this.defineHero()

  private def defineHero(): Unit = {
    val bodyDef: BodyDef = new BodyDef()
    bodyDef.position.set(1, 1)
    bodyDef.`type` = BodyDef.BodyType.DynamicBody

    this.body = world.createBody(bodyDef)

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: CircleShape = new CircleShape()
    shape.setRadius(this.radius)

    fixtureDef.shape = shape

    body.createFixture(fixtureDef)
  }

  def getBody: Body = this.body

  def jump(): Unit = {
    this.body.applyLinearImpulse(vectorScalar(new Vector2(0, 400f)), this.body.getWorldCenter, true)
  }

  def moveRight(): Unit = {
    if (this.body.getLinearVelocity.x <= 2) {
      this.body.applyLinearImpulse(vectorScalar(new Vector2(60f, 0)), this.body.getWorldCenter, true)
    }
  }

  def moveLeft(): Unit = {
    if (this.body.getLinearVelocity.x >= -2) {
      this.body.applyLinearImpulse(vectorScalar(new Vector2(-60f, 0)), this.body.getWorldCenter, true)
    }
  }

  private def vectorScalar(vector: Vector2, scalar: Float = Gdx.graphics.getDeltaTime()) = new Vector2(vector.x * scalar, vector.y * scalar)


  override def getWidth: Float = this.radius

  override def getHeight: Float = this.radius
}

package view.entities

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.{Body, BodyDef, CircleShape, FixtureDef, World}
import utils.GameConstants.PIXEL_PER_METER

class Hero(w: World) extends Sprite {

  private val world: World = w

  private var body: Body = _

  this.defineHero()

  private def defineHero(): Unit = {
    val bodyDef: BodyDef = new BodyDef()
    bodyDef.position.set(1 / PIXEL_PER_METER, 1 / PIXEL_PER_METER)
    bodyDef.`type` = BodyDef.BodyType.DynamicBody

    this.body = world.createBody(bodyDef)

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: CircleShape = new CircleShape()
    shape.setRadius(1 / PIXEL_PER_METER)

    fixtureDef.shape = shape

    body.createFixture(fixtureDef)
  }

  def getBody: Body = this.body

  def jump(): Unit = {
    this.body.applyLinearImpulse(new Vector2(0, 4f), this.body.getWorldCenter, true)
  }

  def moveRight(): Unit = {
    if (this.body.getLinearVelocity.x <= 2) {
      this.body.applyLinearImpulse(new Vector2(0.1f, 0), this.body.getWorldCenter, true)
    }
  }

  def moveLeft(): Unit = {
    if (this.body.getLinearVelocity.x >= -2) {
      this.body.applyLinearImpulse(new Vector2(-0.1f, 0), this.body.getWorldCenter, true)
    }
  }

}

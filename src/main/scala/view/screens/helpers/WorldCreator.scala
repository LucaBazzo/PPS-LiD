package view.screens.helpers

import _root_.utils.GameConstants.PIXEL_PER_METER
import com.badlogic.gdx.physics.box2d._

class WorldCreator(w: World) {

  private val world: World = w

  this.rectangleDefinition()

  private def rectangleDefinition() = {
    val bodyDef: BodyDef = new BodyDef()
    val shape: PolygonShape = new PolygonShape()
    val fixtureDef: FixtureDef = new FixtureDef()

    bodyDef.`type` = BodyDef.BodyType.StaticBody
    bodyDef.position.set(0, -2 / PIXEL_PER_METER)

    val body: Body = world.createBody(bodyDef)
    shape.setAsBox(5 / PIXEL_PER_METER,0.5f / PIXEL_PER_METER)
    fixtureDef.shape = shape
    body.createFixture(fixtureDef)
  }

}

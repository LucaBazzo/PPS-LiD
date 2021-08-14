package view.screens.helpers

//import _root_.utils.GameConstants.PIXEL_PER_METER
import com.badlogic.gdx.physics.box2d._

class WorldCreator(w: World) {

  private val world: World = w

  this.rectangleDefinition()

  private def rectangleDefinition() = {
    val bodyDef: BodyDef = new BodyDef()
    val shape: PolygonShape = new PolygonShape()
    val fixtureDef: FixtureDef = new FixtureDef()

    /*fixtureDef.filter.categoryBits = 2
    fixtureDef.filter.maskBits = 1*/

    bodyDef.`type` = BodyDef.BodyType.StaticBody
    bodyDef.position.set(0, -2)

    val body: Body = world.createBody(bodyDef)
    shape.setAsBox(5 ,0.5f)
    fixtureDef.shape = shape
    body.createFixture(fixtureDef)
  }

}

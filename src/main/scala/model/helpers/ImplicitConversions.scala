package model.helpers

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import model.entities.Entity
import utils.ApplicationConstants.PIXELS_PER_METER

object ImplicitConversions {

  implicit def intToShort(value: Int): Short = value.toShort

  implicit def tupleToVector2(tuple: (Float, Float)): Vector2 =
    new Vector2(tuple._1, tuple._2)

  implicit def vectorToTuple(vector: Vector2): (Float, Float) =
    (vector.x, vector.y)

  implicit def entityToBody(entity: Entity): Body = entity.getBody

  implicit class RichFloat(base: Float) {
    def PPM: Float = base / PIXELS_PER_METER

    def MPP: Float = base * PIXELS_PER_METER
  }

  implicit class RichInt(base: Int) {
    def PPM: Float = base / PIXELS_PER_METER

    def MPP: Float = base * PIXELS_PER_METER
  }

  implicit class RichTuple2(base: (Float, Float)) {
    def PPM: (Float, Float) = base / PIXELS_PER_METER

    def MPP: (Float, Float) = base * PIXELS_PER_METER

    def INV: (Float, Float) = (-base._1, -base._2)

    def /(div: Float): (Float, Float) = (base._1 / div, base._2 / div)

    def *(mul: Float): (Float, Float) = (base._1 * mul, base._2 * mul)

    def *(tuple: (Float, Float)): (Float, Float) = (base._1 * tuple._1, base._2 * tuple._2)

    def +(tuple: (Float, Float)): (Float, Float) = (base._1 + tuple._1, base._2 + tuple._2)

    def -(tuple: (Float, Float)): (Float, Float) = (base._1 - tuple._1, base._2 - tuple._2)
  }

}

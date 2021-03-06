package model.helpers

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.{Body, Fixture, World}
import model.entity.Entity
import utils.ApplicationConstants.PIXELS_PER_METER

import scala.collection.immutable.ListMap

/** Implicit conversions object
 *
 */
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

  implicit class RichWorld(world:World) {

    val DEFAULT_VISION_ANGLE: Int = 90
    val MATH_PI = 180

    private def worldQuery(x1: Float, y1: Float, x2: Float, y2: Float, f: Fixture => Boolean): Boolean = {
      var output: Boolean = false
      world.QueryAABB((fixture: Fixture) => {
        output = f.apply(fixture)
        !output
      }, x1, y1, x2, y2)
      output
    }

    def checkCollisionWithBody(x1: Float, y1: Float, x2: Float, y2: Float, targetBody: Body): Boolean =
      worldQuery(x1, y1, x2, y2, (fixture:Fixture) => targetBody equals fixture.getBody)

    def checkCollision(x1: Float, y1: Float, x2: Float, y2: Float, sourceBody: Body): Boolean =
      worldQuery(x1, y1, x2, y2, (fixture:Fixture) =>
        canFixturesCollide(sourceBody.getFixtureList.toArray().head, fixture))

    def checkCollision(x1: Float, y1: Float, x2: Float, y2: Float, entityBit: Short): Boolean =
      worldQuery(x1, y1, x2, y2, (fixture:Fixture) => fixture.getFilterData.categoryBits == entityBit)

    def checkCollision(x1: Float, y1: Float, x2: Float, y2: Float): Boolean =
      worldQuery(x1, y1, x2, y2, _ => true)

    def isBodyVisible(sourceBody: Body, targetBody: Body, maxHorizontalAngle: Int = DEFAULT_VISION_ANGLE): Boolean = {
      // Get the list of ordered fixtures (bodies) between source and target bodies
      var fixList:Map[Fixture, Float] = Map.empty
      world.rayCast((fixture:Fixture, point:Vector2, _, _) => {
        fixList = fixList + (fixture -> GeometricUtilities.getPointsDistance(sourceBody.getPosition, point))
        1
      }, sourceBody.getPosition, targetBody.getPosition)

      // Check if source and target bodies are obstructed by other colliding entities
      // No fixtures between target and source (fixList is empty) means that they are overlapping
      var isTargetVisible = if (fixList.nonEmpty) false else true
      var preemptiveStop = false
      for (fixture <- ListMap(fixList.toSeq.sortBy(_._2):_*).keys if !preemptiveStop && !isTargetVisible) {
        isTargetVisible = fixture.getBody.equals(targetBody)

        // Check horizontal axis angle of source-target bodies
        if (isTargetVisible) {
          val angle =
            new Vector2(sourceBody.getPosition.sub(targetBody.getPosition)).angleDeg()
          isTargetVisible = ((angle >= MATH_PI * 2 - maxHorizontalAngle)
            || (MATH_PI + maxHorizontalAngle >= angle))
          preemptiveStop = true
        }

        // an entity who can collides with the source is obstructing the visual
        if ((sourceBody.getFixtureList.toArray().head.getFilterData.maskBits &
          fixture.getFilterData.categoryBits) != 0
          && !fixture.isSensor)
          preemptiveStop = true
      }
      isTargetVisible
    }

    def canBodiesCollide(body1:Body, body2:Body):Boolean =
      canFixturesCollide(body1.getFixtureList.toArray().head,
        body2.getFixtureList.toArray().head)

    def canFixturesCollide(fixture1:Fixture, fixture2:Fixture): Boolean =
      (fixture1.getFilterData.maskBits & fixture2.getFilterData.categoryBits) != 0

    def checkSideCollision(rightSide: Boolean, entity: Entity, entitiesBit: Short*): Boolean = {
      val sideX = getEntitySideX(entity.getPosition, entity.getSize, rightSide)
      val sideY = getEntitySideY(entity.getPosition, entity.getSize)

      entitiesBit.exists(entityBit => checkCollision(sideX._1, sideY._1, sideX._2, sideY._2, entityBit))
    }

    def computeDirectionToTarget(position: (Float, Float), target:(Float, Float), scale: Float): (Float, Float) = {
      target.sub(position).nor().scl(scale)
    }

    private def getEntitySideX(position: (Float, Float), size: (Float, Float), rightSide: Boolean = true): (Float, Float) = {
      var x1: Float = size._1
      if(!rightSide) x1 = -x1
      (position._1 + x1, position._1 + x1 + x1)
    }

    private def getEntitySideY(position: (Float, Float), size: (Float, Float)): (Float, Float) =
      (position._2 - size._2, position._2 + size._2)
  }
}

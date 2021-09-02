package model.helpers

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.{CircleShape, PolygonShape, _}
import model.collisions.EntityCollisionBit
import model.collisions.ImplicitConversions.RichFloat

import scala.collection.immutable.ListMap

trait WorldUtilities {

  def setWorld(world: World): Unit

  def isBodyVisible(sourceBody: Body, targetBody: Body, angle: Float = 90): Boolean

  def checkCollision(x1: Float, y1: Float, x2: Float, y2: Float, entityBit: Short): Boolean

  def checkCollision(x1: Float, y1: Float, x2: Float, y2: Float, body: Body): Boolean

  def checkCollision(x1: Float, y1: Float, x2: Float, y2: Float): Boolean

  def bodiesCanCollide(body1: Body, body2: Body): Boolean

  def isFloorPresentOnTheLeft(body: Body, offset: Float = 5f.PPM): Boolean

  def isFloorPresentOnTheRight(body: Body, offset: Float = 5f.PPM): Boolean

  def isPathClearOnTheLeft(body: Body, offset: Float = 5f.PPM): Boolean

  def isPathClearOnTheRight(body: Body, offset: Float = 5f.PPM): Boolean

  def getBodySizes(body: Body): (Float, Float)

  def checkCollision(point1: Vector2, point2: Vector2, targetBody: Body): Boolean =
    checkCollision(point1.x, point1.y, point2.x, point2.y, targetBody)

  def checkCollision(point1: Vector2, point2: Vector2, entityBit: Short): Boolean =
    checkCollision(point1.x, point1.y, point2.x, point2.y, entityBit)

  def checkCollision(point1: Vector2, point2: Vector2): Boolean =
    checkCollision(point1.x, point1.y, point2.x, point2.y)

  def checkCollision(x: Float, y: Float, targetBody: Body): Boolean =
    checkCollision(x, y, x, y, targetBody)

  def checkCollision(x: Float, y: Float, entityBit: Short): Boolean =
    checkCollision(x, y, x, y, entityBit)

  def checkCollision(x: Float, y: Float): Boolean =
    checkCollision(x, y, x, y)

  def checkCollision(point: Vector2, targetBody: Body): Boolean =
    checkCollision(point.x, point.y, point.x, point.y, targetBody)

  def checkCollision(point: Vector2, entityBit: Short): Boolean =
    checkCollision(point.x, point.y, point.x, point.y, entityBit)

  def checkCollision(point: Vector2): Boolean =
    checkCollision(point.x, point.y, point.x, point.y)

  def getBodyWidth(body: Body): Float = getBodySizes(body)._1

  def getBodyHeight(body: Body): Float = getBodySizes(body)._2

}

object WorldUtilities extends WorldUtilities {

  // TODO: fattorizzare world.QueryAABB

  private var world:World = _

  def setWorld(world:World): Unit = this.world = world

  override def checkCollision(x1: Float, y1: Float, x2: Float, y2: Float, targetBody: Body): Boolean = {
    var output: Boolean = false
    world.QueryAABB((fixture: Fixture) => {
      output = targetBody equals fixture.getBody
      !output // automatically stop consecutive queries if a match has been found
    }, x1, y1, x2, y2)
    output
  }

  override def checkCollision(x1: Float, y1: Float, x2: Float, y2: Float, entityBit: Short): Boolean = {
    var output: Boolean = false
    world.QueryAABB((fixture: Fixture) => {
      output = fixture.getFilterData.categoryBits == entityBit
      !output // automatically stop consecutive queries if a match has been found
    },x1, y1, x2, y2)
    output
  }

  override def checkCollision(x1: Float, y1: Float, x2: Float, y2: Float): Boolean = {
    var output: Boolean = false
    world.QueryAABB(_ => {
      output = true
      !output // automatically stop consecutive queries if a match has been found
    },x1, y1, x2, y2)
    output
  }

  override def isBodyVisible(sourceBody: Body, targetBody: Body, maxHorizontalAngle: Float = 90): Boolean = {
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
        val angle = new Vector2(sourceBody.getPosition.sub(targetBody.getPosition)).angleDeg()
        isTargetVisible = ((angle <= maxHorizontalAngle || angle >= 360-maxHorizontalAngle)
          || (180-maxHorizontalAngle <= angle && 180+maxHorizontalAngle >= angle))
        preemptiveStop = true
      }

      // an entity who can collides with the source is obstructing the visual
      if ((sourceBody.getFixtureList.toArray().head.getFilterData.maskBits & fixture.getFilterData.categoryBits) != 0
        && !fixture.isSensor)
        preemptiveStop = true
    }
    isTargetVisible
  }

  override def bodiesCanCollide(body1:Body, body2:Body):Boolean =
    (body1.getFixtureList.toArray().head.getFilterData.maskBits &
      body2.getFixtureList.toArray().head.getFilterData.categoryBits) != 0

  override def isFloorPresentOnTheLeft(body: Body, offset:Float = 5f.PPM): Boolean = {
    val position = body.getWorldCenter
    val bodyWidth = getBodyWidth(body)
    val bodyHeight = getBodyHeight(body)

    checkCollision(
      position.x - -bodyWidth / 2 - offset, position.y - bodyHeight / 2,
      EntityCollisionBit.Immobile)
  }

  override def isFloorPresentOnTheRight(body: Body, offset:Float = 5f.PPM): Boolean = {
    val position = body.getWorldCenter
    val bodyWidth = getBodyWidth(body)
    val bodyHeight = getBodyHeight(body)

    checkCollision(
      position.x - +bodyWidth / 2 + offset, position.y - bodyHeight / 2,
      EntityCollisionBit.Immobile)
  }

  override def isPathClearOnTheLeft(body: Body, offset:Float = 5f.PPM): Boolean = {
    val position = body.getWorldCenter
    val bodyWidth = getBodyWidth(body)
    val bodyHeight = getBodyHeight(body)

    checkCollision(
      position.x + -bodyWidth / 2 - offset, position.y - bodyHeight / 2,
      position.x + -bodyWidth / 2 - offset, position.y + bodyHeight / 2,
      EntityCollisionBit.Immobile)
  }

  override def isPathClearOnTheRight(body: Body, offset:Float = 5f.PPM): Boolean = {
    val position = body.getWorldCenter
    val bodyWidth = getBodyWidth(body)
    val bodyHeight = getBodyHeight(body)

    checkCollision(
      position.x + bodyWidth / 2 + offset, position.y - bodyHeight / 2,
      position.x + bodyWidth / 2 + offset, position.y + bodyHeight / 2,
      EntityCollisionBit.Immobile)
  }

  override def getBodySizes(body:Body): (Float, Float) = {
    // TODO: considerare tutte le fixture del body invece che solo la prima
    val fixture = body.getFixtureList.toArray().head

    fixture.getShape match {
      case shape: CircleShape =>
        val diameter = shape.getRadius * 2
        (diameter, diameter)
      case shape: PolygonShape => (getPolygonWidth(shape), getPolygonHeight(shape))
    }
  }

  private def getPolygonWidth(shape:PolygonShape): Float  = {
    var vertex = new Vector2()
    shape.getVertex(0, vertex)

    var lowerX:Float = vertex.x
    var higherX:Float = vertex.x

    for (i <- 1 until shape.getVertexCount) {
      shape.getVertex(i, vertex)

      if (vertex.x < lowerX)
        lowerX = vertex.x
      if (vertex.x > higherX)
        higherX = vertex.x
    }
    higherX - lowerX
  }

  private def getPolygonHeight(shape:PolygonShape): Float  = {
    var vertex = new Vector2()
    shape.getVertex(0, vertex)

    var lowerY:Float = vertex.y
    var higherY:Float = vertex.y

    for (i <- 1 until shape.getVertexCount) {
      shape.getVertex(i, vertex)

      if (vertex.y < lowerY) lowerY = vertex.y
      if (vertex.y > higherY) higherY = vertex.y
    }
    higherY - lowerY
  }
}

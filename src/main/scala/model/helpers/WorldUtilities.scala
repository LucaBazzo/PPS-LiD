package model.helpers

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import model.entities.Entity
import model.helpers.WorldUtilities.{getBodyHeight, getBodyWidth}

import scala.collection.immutable.ListMap

object WorldUtilities {
  def computePointsDistance(sourcePoint: Vector2, targetPoint: Vector2): Float =
    Math.sqrt(Math.pow(sourcePoint.x - targetPoint.x, 2) +
      Math.pow(sourcePoint.y - targetPoint.y, 2)).toFloat

  def checkAABBCollision(world:World, x1:Float, y1:Float, x2:Float, y2:Float, entity:Entity): Boolean = {
    var output: Boolean = false
    world.QueryAABB((fixture: Fixture) => {
      if (entity.getBody equals fixture.getBody)
        output = true
      !output // automatically stop consecutive queries if a match has been found
    }, x1, y1, x2, y2)
    output
  }

  def checkAABBCollision(world:World, x1:Float, y1:Float, x2:Float, y2:Float, entityBit:Short): Boolean = {
    var output: Boolean = false
    world.QueryAABB((fixture: Fixture) => {
      // a collision with a specific entity type has occurred
      output = fixture.getFilterData.categoryBits == entityBit

      // automatically stop consecutive queries if a match has been found
      !output
    },x1, y1, x2, y2)
    output
  }

  def checkAABBCollision(world:World, x1:Float, y1:Float, x2:Float, y2:Float): Boolean = {
    var output: Boolean = false
    world.QueryAABB((_) => {
      output = true
      false // automatically stop consecutive queries
    },x1, y1, x2, y2)
    output
  }

  def checkPointCollision(world:World, x:Float, y:Float): Boolean = {
    checkAABBCollision(world:World, x, y, x, y)
  }

  def checkPointCollision(world:World, x:Float, y:Float, entity:Entity): Boolean = {
    checkAABBCollision(world:World, x, y, x, y, entity)
  }

  def checkPointCollision(world:World, x:Float, y:Float, entityBit:Short): Boolean = {
    checkAABBCollision(world:World, x, y, x, y, entityBit)
  }

  def checkBodyIsVisible(world: World, sourceBody:Body, targetBody:Body, maxHorizontalAngle:Float=90): Boolean = {
    // Get the list of ordered fixtures (bodies) between source and target bodies
    var fixList:Map[Fixture, Float] = Map.empty
    world.rayCast((fixture:Fixture, point:Vector2, _, _) => {
      fixList = fixList + (fixture -> WorldUtilities.computePointsDistance(sourceBody.getPosition, point))
      1
    }, sourceBody.getPosition, targetBody.getPosition)

    // Check if source and target bodies are obstructed by other colliding entities
    // No fixtures between target and source means that they are overlapping
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
      if ((sourceBody.getFixtureList.toArray().head.getFilterData.maskBits & fixture.getFilterData.categoryBits) != 0)
        preemptiveStop = true
    }
    isTargetVisible
  }

  def getBodiesDistance(body1:Body, body2:Body): Float = {
    // TODO : convertire distanza tra centri (attuale) in minima distanza tra le superfici dei body
    body1.getWorldCenter.dst(body2.getWorldCenter)
  }

  def isTargetOnTheRight(sourceBody: Body, targetBody: Body): Boolean = {
    sourceBody.getPosition.x - targetBody.getPosition.x < 0
  }

  def getBodyWidth(body: Body): Float = {
    // TODO: instead of looking only the first Fixture, consider every non-sensor fixture
    val fixture:Fixture = body.getFixtureList.toArray().head
    fixture.getType match {
      case Shape.Type.Circle => fixture.getShape.asInstanceOf[CircleShape].getRadius*2
    }
  }

  def getBodyHeight(body: Body): Float = {
    // TODO: instead of looking only the first Fixture, consider every non-sensor fixture
    val fixture:Fixture = body.getFixtureList.toArray().head
    fixture.getType match {
      case Shape.Type.Circle => fixture.getShape.asInstanceOf[CircleShape].getRadius*2
    }
  }

}

object SensorsUtility {
  val fixtureDef:FixtureDef = new FixtureDef()
  val shape:PolygonShape = new PolygonShape

  fixtureDef.isSensor = true

  def createLowerLeftSensor(body:Body): Fixture = {
    shape.setAsBox(0.1f, 0.1f, new Vector2(
      -getBodyWidth(body)/2 - 0.1f,
      -getBodyHeight(body)/2 - 0.1f), 0)
    fixtureDef.shape = shape
    body.createFixture(fixtureDef)
  }

  def createLowerRightSensor(body:Body): Fixture = {
    shape.setAsBox(0.1f, 0.1f, new Vector2(
      +getBodyWidth(body)/2 + 0.1f,
      -getBodyHeight(body)/2 - 0.1f), 0)
    fixtureDef.shape = shape
    body.createFixture(fixtureDef)
  }

  def sensorIsIntersectingWith(sensor:Fixture, categoryBit: Short, world:World): Boolean = {
    var output: Boolean = true
    for (contact <- world.getContactList.toArray()) {
      output = contact.getFixtureA.equals(sensor) && contact.getFixtureB.getFilterData.categoryBits.equals(categoryBit)
    }
    output
  }
}

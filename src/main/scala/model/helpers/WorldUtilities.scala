package model.helpers

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import model.collisions.ImplicitConversions.RichFloat
import model.entities.Entity

import scala.collection.immutable.ListMap

trait WorldUtilities {

  def setWorld(world: World): Unit

  def isBodyVisible(sourceBody: Body, targetBody: Body, angle: Float = 90): Boolean

  def checkCollisionWithBody(x1: Float, y1: Float, x2: Float, y2: Float, body: Body): Boolean

  def checkCollision(x1: Float, y1: Float, x2: Float, y2: Float, entityBit: Short): Boolean

  def checkCollision(x1: Float, y1: Float, x2: Float, y2: Float, body: Body): Boolean

  def checkCollision(x1: Float, y1: Float, x2: Float, y2: Float): Boolean

  def canBodiesCollide(body1: Body, body2: Body): Boolean

  def canFixturesCollide(fixture1: Fixture, fixture2: Fixture): Boolean

  def isFloorPresentOnTheRight(body: Body, size: (Float, Float), hOffset: Float, vOffset: Float): Boolean

  def isFloorPresentOnTheLeft(body: Body, size: (Float, Float), hOffset: Float, vOffset: Float): Boolean

  def isPathObstructedOnTheLeft(body: Body, size: (Float, Float), hOffset: Float, vOffset: Float): Boolean

  def isPathObstructedOnTheRight(body: Body, size: (Float, Float), hOffset: Float, vOffset: Float): Boolean

  def checkCollision(x: Float, y: Float, sourceBody: Body): Boolean = checkCollision(x, y, x, y, sourceBody)

  def checkCollisionWithBody(x: Float, y: Float, targetBody: Body): Boolean = checkCollisionWithBody(x, y, x, y, targetBody)

  def checkCollision(x: Float, y: Float, entityBit: Short): Boolean = checkCollision(x, y, x, y, entityBit)

  def checkCollision(x: Float, y: Float): Boolean = checkCollision(x, y, x, y)

  def checkSideCollision(rightSide: Boolean, entity: Entity, entitiesBit: Short*): Boolean
}

object WorldUtilities extends WorldUtilities {

  private var world:World = _

  override def setWorld(world:World): Unit = this.world = world

  override def checkCollisionWithBody(x1: Float, y1: Float, x2: Float, y2: Float, targetBody: Body): Boolean = {
    var output: Boolean = false
    world.QueryAABB((fixture: Fixture) => {
      output = targetBody equals fixture.getBody
      !output // automatically stop consecutive queries if a match has been found
    }, x1, y1, x2, y2)
    output
  }

  override def checkCollision(x1: Float, y1: Float, x2: Float, y2: Float, sourceBody: Body): Boolean = {
    var output: Boolean = false
    val sourceFixture = sourceBody.getFixtureList.toArray().head
    world.QueryAABB((fixture: Fixture) => {
      output = canFixturesCollide(sourceFixture, fixture)
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
    }, x1, y1, x2, y2)
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
        //        isTargetVisible = ((angle <= maxHorizontalAngle || angle >= 360-maxHorizontalAngle)
        //          || (180-maxHorizontalAngle <= angle && 180+maxHorizontalAngle >= angle))
        isTargetVisible = ((angle >= 360-maxHorizontalAngle)
          || (180+maxHorizontalAngle >= angle))
        preemptiveStop = true
      }

      // an entity who can collides with the source is obstructing the visual
      if ((sourceBody.getFixtureList.toArray().head.getFilterData.maskBits & fixture.getFilterData.categoryBits) != 0
        && !fixture.isSensor)
        preemptiveStop = true
    }
    isTargetVisible
  }

  override def canBodiesCollide(body1:Body, body2:Body):Boolean =
    canFixturesCollide(body1.getFixtureList.toArray().head, body2.getFixtureList.toArray().head)

  override def canFixturesCollide(fixture1:Fixture, fixture2:Fixture): Boolean =
    (fixture1.getFilterData.maskBits & fixture2.getFilterData.categoryBits) != 0

  override def isFloorPresentOnTheLeft(body: Body, size:(Float, Float), hOffset:Float = 5f.PPM, vOffset:Float = 8f.PPM): Boolean = {
    val position = body.getPosition

    checkCollision(
      position.x - size._1 - hOffset, position.y - size._2 + vOffset,
      body)
  }

  override def isFloorPresentOnTheRight(body: Body, size:(Float, Float), hOffset:Float = 5f.PPM, vOffset:Float = 8f.PPM): Boolean = {
    val position = body.getWorldCenter

    checkCollision(
      position.x + size._1 + hOffset, position.y - size._2 + vOffset,
      body)
  }

  override def isPathObstructedOnTheLeft(body: Body, size:(Float, Float), hOffset:Float = 1f.PPM,
                                         vOffset:Float = 0f.PPM): Boolean = {
    val position = body.getWorldCenter

    checkCollision(
      position.x - size._1 - hOffset, position.y - size._2 / 2 + vOffset,
      position.x - size._1 - hOffset, position.y + size._2 / 2 + vOffset,
      body)
  }

  override def isPathObstructedOnTheRight(body: Body, size:(Float, Float), hOffset:Float = 1f.PPM,
                                          vOffset:Float = 0f.PPM): Boolean = {
    val position = body.getWorldCenter

    checkCollision(
      position.x + size._1 + hOffset, position.y - size._2 / 2 + vOffset,
      position.x + size._1 + hOffset, position.y + size._2 / 2 + vOffset,
      body)
  }

  override def checkSideCollision(rightSide: Boolean, entity: Entity, entitiesBit: Short*): Boolean = {
    val sideX = getEntitySideX(entity.getPosition, entity.getSize, rightSide)
    val sideY = getEntitySideY(entity.getPosition, entity.getSize)

    entitiesBit.exists(entityBit => checkCollision(sideX._1, sideY._1, sideX._2, sideY._2, entityBit))
  }

  private def getEntitySideX(position: (Float, Float), size: (Float, Float), rightSide: Boolean = true): (Float, Float) = {
    var x1: Float = size._1
    if(!rightSide) x1 = -x1
    (position._1 + x1, position._1 + x1 + x1)
  }

  private def getEntitySideY(position: (Float, Float), size: (Float, Float)): (Float, Float) =
    (position._2 - size._2, position._2 + size._2)
}

package model.helpers

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import model.entities.Entity
import model.helpers.ImplicitConversions.{tupleToVector2, vectorToTuple}

import scala.collection.immutable.ListMap

object WorldUtilities {

  private var world:World = _

  def setWorld(world:World): Unit = this.world = world

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

  def isBodyVisible(sourceBody: Body, targetBody: Body, maxHorizontalAngle: Int = 90): Boolean = {
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
        isTargetVisible = ((angle >= 360-maxHorizontalAngle)
          || (180+maxHorizontalAngle >= angle))
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

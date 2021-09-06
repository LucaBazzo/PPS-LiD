package model.helpers

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body


trait GeometricUtilities {

  def getPointsDistance(sourcePoint: Vector2, targetPoint: Vector2): Float

  def isTargetPointOnTheLeft(sourcePoint: Vector2, targetPoint: Vector2): Boolean

  def isTargetPointOnTheRight(sourcePoint: Vector2, targetPoint: Vector2): Boolean

  def isTargetPointAbove(sourcePoint: Vector2, targetPoint: Vector2): Boolean

  def isTargetPointBelow(sourcePoint: Vector2, targetPoint: Vector2): Boolean

  def getPointsAngle(sourcePoint: Vector2, targetPoint: Vector2): Float

  def isBodyBelow(sourceBody: Body, targetBody: Body): Boolean

  def isBodyAbove(sourceBody: Body, targetBody: Body): Boolean

  def isBodyOnTheLeft(sourceBody: Body, targetBody: Body): Boolean

  def isBodyOnTheRight(sourceBody: Body, targetBody: Body): Boolean

  def getBodiesDistance(sourceBody: Body, targetBody: Body): Float

  def isBodyMovingToTheLeft(body: Body): Boolean

  def isBodyMovingToTheRight(body: Body): Boolean

  def isBodyMovingHorizontally(body: Body): Boolean

  def isBodyMovingVertically(body: Body): Boolean

  def isBodyMoving(body: Body): Boolean
}

object GeometricUtilities extends GeometricUtilities {

  override def getPointsDistance(sourcePoint: Vector2, targetPoint: Vector2): Float =
    sourcePoint.dst(targetPoint)

  override def isTargetPointOnTheLeft(sourcePoint: Vector2, targetPoint: Vector2): Boolean =
    sourcePoint.sub(targetPoint).x >= 0

  override def isTargetPointOnTheRight(sourcePoint: Vector2, targetPoint: Vector2): Boolean =
    sourcePoint.sub(targetPoint).x <= 0

  override def isTargetPointAbove(sourcePoint: Vector2, targetPoint: Vector2): Boolean =
    sourcePoint.sub(targetPoint).y < 0

  override def isTargetPointBelow(sourcePoint: Vector2, targetPoint: Vector2): Boolean =
    sourcePoint.sub(targetPoint).y > 0

  override def getPointsAngle(sourcePoint: Vector2, targetPoint: Vector2): Float =
    sourcePoint.angleDeg(targetPoint)

  override def isBodyBelow(sourceBody:Body, targetBody:Body): Boolean =
    isTargetPointBelow(sourceBody.getWorldCenter, targetBody.getWorldCenter)

  override def isBodyAbove(sourceBody:Body, targetBody:Body): Boolean =
    isTargetPointAbove(sourceBody.getWorldCenter, targetBody.getWorldCenter)

  override def isBodyOnTheLeft(sourceBody:Body, targetBody:Body): Boolean =
    isTargetPointOnTheLeft(sourceBody.getWorldCenter, targetBody.getWorldCenter)

  override def isBodyOnTheRight(sourceBody:Body, targetBody:Body): Boolean =
    isTargetPointOnTheRight(sourceBody.getWorldCenter, targetBody.getWorldCenter)

  override def getBodiesDistance(sourceBody:Body, targetBody:Body): Float =
    getPointsDistance(sourceBody.getWorldCenter, targetBody.getWorldCenter)

  override def isBodyMovingToTheLeft(body: Body): Boolean = body.getLinearVelocity.x < 0

  override def isBodyMovingToTheRight(body: Body): Boolean = body.getLinearVelocity.x > 0

  override def isBodyMovingHorizontally(body: Body): Boolean = body.getLinearVelocity.x == 0

  override def isBodyMovingVertically(body: Body): Boolean = body.getLinearVelocity.y == 0

  override def isBodyMoving(body: Body): Boolean = body.getLinearVelocity.len() == 0
}

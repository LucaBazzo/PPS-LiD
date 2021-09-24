package model.helpers

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body

object GeometricUtilities {

  def getPointsDistance(p1: Vector2, p2: Vector2): Float = p1.dst(p2)

  def isTargetPointOnTheLeft(point: Vector2, targetPoint: Vector2): Boolean = point.x - targetPoint.x >= 0

  def isTargetPointOnTheRight(point: Vector2, targetPoint: Vector2): Boolean = point.x - targetPoint.x <= 0

  def isTargetPointAbove(point: Vector2, targetPoint: Vector2): Boolean = point.y - targetPoint.y <= 0

  def isTargetPointBelow(point: Vector2, targetPoint: Vector2): Boolean = point.y - targetPoint.y >= 0

  def getPointsAngle(point: Vector2, targetPoint: Vector2): Float = point.angleDeg(targetPoint)

  def isBodyBelow(body:Body, targetBody:Body): Boolean = isTargetPointBelow(body.getWorldCenter, targetBody.getWorldCenter)

  def isBodyAbove(body:Body, targetBody:Body): Boolean = isTargetPointAbove(body.getWorldCenter, targetBody.getWorldCenter)

  def isBodyOnTheLeft(body:Body, targetBody:Body): Boolean = isTargetPointOnTheLeft(body.getWorldCenter, targetBody.getWorldCenter)

  def isBodyOnTheRight(body:Body, targetBody:Body): Boolean = isTargetPointOnTheRight(body.getWorldCenter, targetBody.getWorldCenter)

  def getBodiesDistance(body:Body, targetBody:Body): Float = getPointsDistance(body.getWorldCenter, targetBody.getWorldCenter)

  def isBodyMovingToTheLeft(body:Body): Boolean = body.getLinearVelocity.x < 0

  def isBodyMovingToTheRight(body:Body): Boolean = body.getLinearVelocity.x > 0

  def isBodyMovingHorizontally(body:Body): Boolean = body.getLinearVelocity.x == 0

  def isBodyMovingVertically(body:Body): Boolean = body.getLinearVelocity.y == 0

  def isBodyMoving(body:Body): Boolean = body.getLinearVelocity.len() == 0
}

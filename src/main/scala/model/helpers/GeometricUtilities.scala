package model.helpers

import com.badlogic.gdx.math.Vector2


trait GeometricUtilities {

  def getPointsDistance(sourcePoint: Vector2, targetPoint: Vector2): Float

  def getPointsDistance(sourcePoint: (Float, Float), targetPoint: (Float, Float)): Float

  def isTargetPointOnTheLeft(sourcePoint: Vector2, targetPoint: Vector2): Boolean

  def isTargetPointOnTheLeft(sourcePoint: (Float, Float), targetPoint: (Float, Float)): Boolean

  def isTargetPointOnTheRight(sourcePoint: Vector2, targetPoint: Vector2): Boolean

  def isTargetPointOnTheRight(sourcePoint: (Float, Float), targetPoint: (Float, Float)): Boolean

  def isTargetPointAbove(sourcePoint: Vector2, targetPoint: Vector2): Boolean

  def isTargetPointAbove(sourcePoint: (Float, Float), targetPoint: (Float, Float)): Boolean

  def isTargetPointBelow(sourcePoint: Vector2, targetPoint: Vector2): Boolean

  def isTargetPointBelow(sourcePoint: (Float, Float), targetPoint: (Float, Float)): Boolean

  def getPointsAngle(sourcePoint: Vector2, targetPoint: Vector2): Float
}

object GeometricUtilities extends GeometricUtilities {

  override def getPointsDistance(sourcePoint: Vector2, targetPoint: Vector2): Float = {
    sourcePoint.dst(targetPoint)
  }

  override def getPointsDistance(sourcePoint: (Float, Float), targetPoint: (Float, Float)): Float =
    getPointsDistance(new Vector2(sourcePoint._1, sourcePoint._2), new Vector2(targetPoint._1, targetPoint._2))

  override def isTargetPointOnTheLeft(sourcePoint: Vector2, targetPoint: Vector2): Boolean = {
    sourcePoint.sub(targetPoint).x < 0
  }

  override def isTargetPointOnTheLeft(sourcePoint: (Float, Float), targetPoint: (Float, Float)): Boolean = {
    sourcePoint._1 - targetPoint._1 < 0
  }

  override def isTargetPointOnTheRight(sourcePoint: Vector2, targetPoint: Vector2): Boolean = {
    sourcePoint.sub(targetPoint).x > 0
  }

  override def isTargetPointOnTheRight(sourcePoint: (Float, Float), targetPoint: (Float, Float)): Boolean = {
    sourcePoint._1 - targetPoint._1 > 0
  }

  override def isTargetPointAbove(sourcePoint: Vector2, targetPoint: Vector2): Boolean = {
    sourcePoint.sub(targetPoint).y < 0
  }

  override def isTargetPointAbove(sourcePoint: (Float, Float), targetPoint: (Float, Float)): Boolean = {
    sourcePoint._2 - targetPoint._2 < 0
  }

  override def isTargetPointBelow(sourcePoint: Vector2, targetPoint: Vector2): Boolean = {
    sourcePoint.sub(targetPoint).y > 0
  }

  override def isTargetPointBelow(sourcePoint: (Float, Float), targetPoint: (Float, Float)): Boolean = {
    sourcePoint._2 - targetPoint._2 > 0
  }

  override def getPointsAngle(sourcePoint: Vector2, targetPoint: Vector2): Float = {
    sourcePoint.angleDeg(targetPoint)
  }
}

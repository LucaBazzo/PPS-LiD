package model.helpers

import ImplicitConversions.RichInt
import model.entity.Entity
import model.helpers.WorldUtilities.checkCollision

object EntitiesUtilities {
  def isPathObstructedOnTheLeft(entity: Entity, hOffset: Float=0.PPM, vOffset: Float=0.PPM): Boolean = {
    val body = entity.getBody
    val position = entity.getBody.getPosition
    val size = entity.getSize

    checkCollision(
      position.x - size._1 - hOffset, position.y - size._2 / 2 + vOffset,
      position.x - size._1 - hOffset, position.y + size._2 / 2 + vOffset,
      body)
  }

  def isPathObstructedOnTheRight(entity: Entity, hOffset: Float=0.PPM, vOffset: Float=0.PPM): Boolean = {
    val body = entity.getBody
    val position = entity.getBody.getPosition
    val size = entity.getSize

    checkCollision(
      position.x + size._1 + hOffset, position.y - size._2 / 2 + vOffset,
      position.x + size._1 + hOffset, position.y + size._2 / 2 + vOffset,
      body)
  }

  def isFloorPresentOnTheRight(entity: Entity, hOffset: Float=0.PPM, vOffset: Float=0.PPM): Boolean = {
    val body = entity.getBody
    val position = entity.getBody.getPosition
    val size = entity.getSize

    checkCollision(
      position.x + size._1 + hOffset, position.y - size._2 + vOffset,
      position.x + size._1 + hOffset, position.y - size._2 + vOffset,
      body)
  }

  def isFloorPresentOnTheLeft(entity: Entity, hOffset: Float=0.PPM, vOffset: Float=0.PPM): Boolean = {
    val body = entity.getBody
    val position = entity.getBody.getPosition
    val size = entity.getSize

    checkCollision(
      position.x - size._1 - hOffset, position.y - size._2 + vOffset,
      position.x - size._1 - hOffset, position.y - size._2 + vOffset,
      body)
  }
}

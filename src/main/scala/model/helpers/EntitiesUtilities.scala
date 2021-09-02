package model.helpers

import model.collisions.ImplicitConversions.RichFloat
import model.entities.Entity
import model.helpers.GeometricUtilities._
import model.helpers.WorldUtilities._

trait EntitiesUtilities {

  def isEntityVisible(sourceEntity: Entity, targetEntity: Entity, angle: Float = 90): Boolean

  def getEntitiesDistance(sourceEntity: Entity, targetEntity: Entity): Float

  def isEntityBelow(sourceEntity: Entity, targetEntity: Entity): Boolean

  def isEntityAbove(sourceEntity: Entity, targetEntity: Entity): Boolean

  def isEntityOnTheLeft(sourceEntity: Entity, targetEntity: Entity): Boolean

  def isEntityOnTheRight(sourceEntity: Entity, targetEntity: Entity): Boolean

  def entitiesCanCollide(entity1: Entity, entity2: Entity): Boolean

  def isPathClearOnTheLeft(entity:Entity, offset:Float = 5f.PPM): Boolean
  def isPathClearOnTheRight(entity:Entity, offset:Float = 5f.PPM): Boolean
  def isFloorPresentOnTheRight(entity:Entity, offset:Float = 5f.PPM): Boolean
  def isFloorPresentOnTheLeft(entity:Entity, offset:Float = 5f.PPM): Boolean

}

object EntitiesUtilities extends EntitiesUtilities {

  override def isEntityVisible(sourceEntity: Entity, targetEntity: Entity, angle:Float = 90): Boolean =
    isBodyVisible(sourceEntity.getBody, targetEntity.getBody, angle)

  override def getEntitiesDistance(sourceEntity:Entity, targetEntity:Entity): Float =
    getBodiesDistance(sourceEntity.getBody, targetEntity.getBody)

  override def isEntityBelow(sourceEntity:Entity, targetEntity:Entity): Boolean =
    isBodyBelow(sourceEntity.getBody, targetEntity.getBody)

  override def isEntityAbove(sourceEntity:Entity, targetEntity:Entity): Boolean =
    isBodyAbove(sourceEntity.getBody, targetEntity.getBody)

  override def isEntityOnTheLeft(sourceEntity:Entity, targetEntity:Entity): Boolean =
    isBodyOnTheLeft(sourceEntity.getBody, targetEntity.getBody)

  override def isEntityOnTheRight(sourceEntity:Entity, targetEntity:Entity): Boolean =
    isBodyOnTheRight(sourceEntity.getBody, targetEntity.getBody)

  override def entitiesCanCollide(entity1:Entity, entity2:Entity): Boolean =
    bodiesCanCollide(entity1.getBody, entity2.getBody)

  override def isPathClearOnTheLeft(entity: Entity, offset: Float = 5.PPM): Boolean =
    WorldUtilities.isPathClearOnTheLeft(entity.getBody, offset)

  override def isPathClearOnTheRight(entity: Entity, offset: Float = 5.PPM): Boolean =
    WorldUtilities.isPathClearOnTheRight(entity.getBody, offset)

  override def isFloorPresentOnTheRight(entity: Entity, offset: Float = 5.PPM): Boolean =
    WorldUtilities.isFloorPresentOnTheRight(entity.getBody, offset)

  override def isFloorPresentOnTheLeft(entity: Entity, offset: Float = 5.PPM): Boolean =
    WorldUtilities.isFloorPresentOnTheRight(entity.getBody, offset)

}

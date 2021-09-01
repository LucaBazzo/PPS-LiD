package model.helpers

import model.entities.Entity

import model.helpers.WorldUtilities._

trait EntitiesUtilities {

  def isEntityVisible(sourceEntity: Entity, targetEntity: Entity, angle: Float = 90): Boolean

  def getEntitiesDistance(sourceEntity: Entity, targetEntity: Entity): Float

  def isEntityBelow(sourceEntity: Entity, targetEntity: Entity): Boolean

  def isEntityAbove(sourceEntity: Entity, targetEntity: Entity): Boolean

  def isEntityOnTheLeft(sourceEntity: Entity, targetEntity: Entity): Boolean

  def isEntityOnTheRight(sourceEntity: Entity, targetEntity: Entity): Boolean

  def entitiesCanCollide(entity1: Entity, entity2: Entity): Boolean
}

object EntitiesUtilities extends EntitiesUtilities {

  override def isEntityVisible(sourceEntity: Entity, targetEntity: Entity, angle:Float = 90): Boolean =
    isBodyVisible(sourceEntity.getBody, targetEntity.getBody)

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

}

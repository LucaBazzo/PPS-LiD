package model.world

import model.Level
import model.collisions.EntityType
import model.collisions.ImplicitConversions._
import model.entities.{Entity, EntityId}
import model.helpers.EntitiesFactoryImpl

class WorldCreator(private val level: Level) {

  private val rectangle: Entity = EntitiesFactoryImpl.createImmobileEntity(EntityId.Immobile, (120, 6f), (0, -20), EntityType.Immobile, EntityType.Hero | EntityType.Enemy)
  level.addEntity(rectangle)

  private val enemy: Entity = EntitiesFactoryImpl.createImmobileEnemy(EntityId.Enemy, (5f, 50f), (80, 10), EntityType.Enemy, EntityType.Hero | EntityType.Sword | EntityType.Arrow)
  level.addEntity(enemy)

  private val square: Entity = EntitiesFactoryImpl.createImmobileEntity(EntityId.Immobile, (5f, 10), (-80, 15f), EntityType.Immobile,  EntityType.Hero | EntityType.Arrow)
  level.addEntity(square)
}

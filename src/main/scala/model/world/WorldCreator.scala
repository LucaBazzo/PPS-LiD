package model.world

import model.Level
import model.collisions.EntityType
import model.collisions.ImplicitConversions._
import model.entities.Entity
import model.helpers.EntitiesFactoryImpl

class WorldCreator(private val level: Level) {

  private val rectangle: Entity = EntitiesFactoryImpl.createImmobileEntity((8, 0.5f), (0, -2), EntityType.Hero)
  level.addEntity(rectangle)

  private val enemy: Entity = EntitiesFactoryImpl.createImmobileEnemy((0.5f, 0.5f), (5, 1), EntityType.Hero | EntityType.Sword)
  level.addEntity(enemy)

}

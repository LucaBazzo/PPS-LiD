package model.world

import model.Level
import model.collisions.EntityType
import model.collisions.ImplicitConversions._
import model.entities.Entity
import model.helpers.EntitiesFactoryImpl

class WorldCreator(private val level: Level) {

  private val rectangle: Entity = EntitiesFactoryImpl.createImmobileEntity((12, 0.5f), (0, -2), EntityType.Hero | EntityType.Enemy)
  level.addEntity(rectangle)
//  level.addEntity(createLeftWall())

  private val enemy: Entity = EntitiesFactoryImpl.createImmobileEnemy((0.5f, 0.5f), (8, 1), EntityType.Hero | EntityType.Sword)
  level.addEntity(enemy)

  private val square: Entity = EntitiesFactoryImpl.createImmobileEnemy((0.5f, 1), (-8, 1.5f), EntityType.Hero)
  level.addEntity(square)
}

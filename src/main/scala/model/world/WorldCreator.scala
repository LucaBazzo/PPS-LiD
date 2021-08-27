package model.world

import model.Level
import model.collisions.EntityType
import model.collisions.ImplicitConversions._
import model.entities.Entity
import model.helpers.EntitiesFactoryImpl

class WorldCreator(private val level: Level) {

  private val rectangle: Entity = EntitiesFactoryImpl.createImmobileEntity((120, 6f), (0, -20), EntityType.Hero | EntityType.Enemy)
  level.addEntity(rectangle)



}

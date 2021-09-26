package model.entities

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import model.EntityBody
import model.collisions.ImplicitConversions._
import model.collisions._
import model.entities.EntityType.EntityType
import model.helpers.EntitiesFactoryImpl.{createPolygonalShape, defineEntityBody}
import model.helpers.{EntitiesContainerMonitor, EntitiesFactoryImpl}
import utils.CollisionConstants._
import utils.EnvironmentConstants._

case class ImmobileEntity(private var entityType: EntityType,
                          private var entityBody: EntityBody,
                          private val size: (Float, Float))
  extends EntityImpl(entityType, entityBody, size) {
}

object Platform {

  def apply(position: (Float, Float),
    size: (Float, Float)): Entity = {

  val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Platform,
  PLATFORM_COLLISIONS, createPolygonalShape(size.PPM), position.PPM)

  val immobileEntity: ImmobileEntity = ImmobileEntity(EntityType.Platform, entityBody, size.PPM)
  immobileEntity.setCollisionStrategy(DoNothingCollisionStrategy())
  EntitiesFactoryImpl.getEntitiesContainerMonitor.addEntity(immobileEntity)

  createPlatformSensor(size, position, immobileEntity, sizeXOffset = PLATFORM_SENSOR_SIZE_X_OFFSET,
  sizeYOffset = -size._2, positionYOffset = size._2 + UPPER_PLATFORM_SENSOR_POSITION_Y_OFFSET, isTopSensor = true)
  createPlatformSensor(size, position, immobileEntity, sizeXOffset = PLATFORM_SENSOR_SIZE_X_OFFSET,
  sizeYOffset = -size._2, positionYOffset = - size._2 + LOWER_PLATFORM_SENSOR_POSITION_Y_OFFSET)
  createPlatformSensor(size, position, immobileEntity, sizeXOffset = -size._1,
  sizeYOffset = SIDE_PLATFORM_SENSOR_SIDE_Y_OFFSET, positionXOffset = +size._1 + SIDE_PLATFORM_SENSOR_POSITION_X_OFFSET, positionYOffset = SIDE_PLATFORM_SENSOR_POSITION_Y_OFFSET)
  createPlatformSensor(size, position, immobileEntity, sizeXOffset = -size._1,
  sizeYOffset = SIDE_PLATFORM_SENSOR_SIDE_Y_OFFSET, positionXOffset = -size._1 - SIDE_PLATFORM_SENSOR_POSITION_X_OFFSET, positionYOffset = SIDE_PLATFORM_SENSOR_POSITION_Y_OFFSET)

  immobileEntity
  }

  private def createPlatformSensor(size: (Float, Float),
                                   position: (Float, Float),
                                   mainPlatform: ImmobileEntity,
                                   sizeXOffset: Float = 0,
                                   sizeYOffset: Float = 0,
                                   positionXOffset: Float = 0,
                                   positionYOffset: Float = 0,
                                   isTopSensor: Boolean = false): Unit = {

  val realSize: (Float, Float) = size + (sizeXOffset, sizeYOffset)
  val realPosition: (Float, Float) = position + (positionXOffset, positionYOffset)

  val sensorBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.PlatformSensor,
  PLATFORM_SENSOR_COLLISIONS, createPolygonalShape(realSize.PPM),
  realPosition.PPM, isSensor = true)

  val sensorEntity: ImmobileEntity = ImmobileEntity(EntityType.PlatformSensor, sensorBody, realSize.PPM)

  val collisionStrategy: CollisionStrategy = if(isTopSensor)
  UpperPlatformCollisionStrategy(mainPlatform, EntitiesFactoryImpl.getCollisionMonitor)
  else
  LowerPlatformCollisionStrategy(mainPlatform, EntitiesFactoryImpl.getCollisionMonitor)

  sensorEntity.setCollisionStrategy(collisionStrategy)

  EntitiesFactoryImpl.getEntitiesContainerMonitor.addEntity(sensorEntity)
  }

}

object Ladder {

  def apply(position: (Float, Float),
            size: (Float, Float)): Entity = {
    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Ladder,
      LADDER_COLLISIONS, createPolygonalShape(size.PPM), position.PPM, isSensor = true)

    val immobileEntity: ImmobileEntity = ImmobileEntity(EntityType.Ladder, entityBody, size.PPM)
    immobileEntity.setCollisionStrategy(LadderCollisionStrategy(EntitiesFactoryImpl.getCollisionMonitor))
    EntitiesFactoryImpl.getEntitiesContainerMonitor.addEntity(immobileEntity)

    immobileEntity
  }

}

object Door {

  def apply(size: (Float, Float) = DEFAULT_DOOR_SIZE,
            position: (Float, Float) = DEFAULT_DOOR_POSITION,
            isBossDoor: Boolean = false,
            collisions: Short = 0): Entity = {

    val doors = createDoorWithSensors(size, position)
    val entitiesContainer: EntitiesContainerMonitor = EntitiesFactoryImpl.getEntitiesContainerMonitor

    val collisionStrategy: CollisionStrategy = if(isBossDoor)
      BossDoorCollisionStrategy(entitiesContainer, doors._1, doors._2, doors._3)
    else
      DoorCollisionStrategy(entitiesContainer, doors._1, doors._2, doors._3)

    doors._1.setCollisionStrategy(collisionStrategy)

    doors._2.setCollisionStrategy(collisionStrategy)

    doors._3.setCollisionStrategy(collisionStrategy)

    entitiesContainer.addEntity(doors._1)

    entitiesContainer.addEntity(doors._2)

    entitiesContainer.addEntity(doors._3)
    doors._1
  }

  private def createDoorWithSensors(size: (Float, Float),
                                    position: (Float, Float)): (ImmobileEntity, ImmobileEntity, ImmobileEntity) = {
    val sensorSize = (0f, size._2)
    val leftSensorPosition = (position._1 - size._1 - 10, position._2)

    val rightSensorPosition = (position._1 + size._1 + 10, position._2)

    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Door,
      DOOR_COLLISIONS, createPolygonalShape(size.PPM), position.PPM)

    val leftSensorBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Door,
      DOOR_COLLISIONS, createPolygonalShape(sensorSize.PPM), leftSensorPosition.PPM, isSensor = true)

    val rightSensorBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Door,
      DOOR_COLLISIONS, createPolygonalShape(sensorSize.PPM), rightSensorPosition.PPM, isSensor = true)

    val door: ImmobileEntity = ImmobileEntity(EntityType.Door, entityBody, size.PPM)

    val leftSensor: ImmobileEntity = ImmobileEntity(EntityType.Immobile, leftSensorBody, size.PPM)

    val rightSensor: ImmobileEntity = ImmobileEntity(EntityType.Immobile, rightSensorBody, size.PPM)

    (door, leftSensor, rightSensor)
  }
}

object Chest {

  def apply(size: (Float, Float) = DEFAULT_CHEST_SIZE,
            position: (Float, Float) = DEFAULT_CHEST_POSITION): Entity = {
  val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Door,
  CHEST_COLLISIONS, createPolygonalShape(size.PPM), position.PPM)

  val immobileEntity: ImmobileEntity = ImmobileEntity(EntityType.Chest, entityBody, size.PPM)
  immobileEntity.setCollisionStrategy(ChestCollisionStrategy(EntitiesFactoryImpl.getEntitiesContainerMonitor, immobileEntity))
    EntitiesFactoryImpl.getEntitiesContainerMonitor.addEntity(immobileEntity)
  immobileEntity
  }

}

object Portal {

  def apply(size: (Float, Float) = DEFAULT_PORTAL_SIZE,
            position: (Float, Float) = DEFAULT_PORTAL_POSITION): Entity = {

    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Portal,
      PORTAL_COLLISIONS, createPolygonalShape(size.PPM), position.PPM)

    val immobileEntity: ImmobileEntity = ImmobileEntity(EntityType.Portal, entityBody, size.PPM)
    immobileEntity.setCollisionStrategy(PortalCollisionStrategy(immobileEntity, EntitiesFactoryImpl.getLevel))
    immobileEntity.setState(State.Closed)
    EntitiesFactoryImpl.getEntitiesContainerMonitor.addEntity(immobileEntity)
    immobileEntity
  }

}

object WaterPool {

  def apply(position: (Float, Float),
                               size: (Float, Float)): Entity = {
    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Pool,
      WATER_LAVA_COLLISIONS, createPolygonalShape(size.PPM), position.PPM, isSensor = true)

    val immobileEntity: Entity = ImmobileEntity(EntityType.Water, entityBody, size.PPM)
    immobileEntity.setCollisionStrategy(new WaterCollisionStrategy)
    EntitiesFactoryImpl.getEntitiesContainerMonitor.addEntity(immobileEntity)
    immobileEntity
  }

}

object LavaPool {

  def apply(position: (Float, Float),
                              size: (Float, Float)): Entity = {
    val entityBody: EntityBody = defineEntityBody(BodyType.StaticBody, EntityCollisionBit.Pool,
      WATER_LAVA_COLLISIONS, createPolygonalShape(size.PPM), position.PPM, isSensor = true)

    val immobileEntity: Entity = ImmobileEntity(EntityType.Lava, entityBody, size.PPM)

    immobileEntity.setCollisionStrategy(LavaCollisionStrategy())
    EntitiesFactoryImpl.getEntitiesContainerMonitor.addEntity(immobileEntity)
    immobileEntity
  }
}
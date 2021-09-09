package view.screens.helpers

import _root_.utils.ApplicationConstants.PIXELS_PER_METER
import _root_.utils.MapConstants.{BOSS_ROOM_MAP_NAME, BOTTOM_KEY_ITEM_ROOM_NAME, TOP_KEY_ITEM_ROOM_NAME}
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.{TiledMap, TmxMapLoader}
import com.badlogic.gdx.math.Rectangle
import model.collisions.EntityCollisionBit
import model.collisions.ImplicitConversions._
import model.entities.EntityType
import model.helpers.EntitiesFactoryImpl

import scala.util.Random

object TileMapHelper {

  private val scale: Float = 1/(PIXELS_PER_METER/2)
  private var keyLocation: String = null

  def getMapRenderer(tiledMap: TiledMap): OrthogonalTiledMapRenderer = {
    new OrthogonalTiledMapRenderer(tiledMap, scale)
  }

  //restituisce la tiledMap settando l'offset di renderizzazione
  def getTiledMap(mapName: String, offset: (Integer, Integer)): TiledMap = {
    val tiledMap: TiledMap = getTiledMap(mapName)

    tiledMap.getLayers.forEach(layer => {
      layer.setOffsetX(offset._1*8)
      layer.setOffsetY(offset._2*8)
    })

    tiledMap
  }

  def getTiledMap(mapName: String): TiledMap = {
    new TmxMapLoader().load("assets/maps/" + mapName + ".tmx")
  }

  def setWorld(rooms: Array[(String, (Integer, Integer))]): Unit = {
    val possibleLocation : Array[String] = Array("TOP", "DOWN")
    keyLocation = possibleLocation(Random.nextInt(possibleLocation.length))
    rooms.foreach(room => loadRoomObjects(room._1, room._2))
  }

  def loadRoomObjects(mapName: String, offset: (Integer, Integer)): Unit = {
    var rect: Rectangle = new Rectangle()
    val tiledMap: TiledMap = getTiledMap(mapName)

    tiledMap.getLayers.forEach(layer => {
      layer.getObjects.forEach(obj => {
        rect = obj.asInstanceOf[RectangleMapObject].getRectangle

        val size: (Float, Float) = (rect.getWidth, rect.getHeight)
        val position: (Float, Float) = (
          rect.getX*2 + rect.getWidth + offset._1*16,
          rect.getY*2 + rect.getHeight - offset._2*16)

        layer.getName match {
          case "ground" => spawnEntity(() => EntitiesFactoryImpl.createImmobileEntity(EntityType.Immobile, size, position, EntityCollisionBit.Immobile, EntityCollisionBit.Hero | EntityCollisionBit.Enemy | EntityCollisionBit.Arrow | EntityCollisionBit.EnemyAttack))
          case "bridge" => spawnEntity(() => EntitiesFactoryImpl.createPlatform(position, size))
          case "door" =>
            if(mapName.equalsIgnoreCase(BOSS_ROOM_MAP_NAME)){
              //TODO spawn boss room door
            } else spawnEntity(() => EntitiesFactoryImpl.createDoor(size, position))
          case "chest" =>
            if(mapName.equalsIgnoreCase(TOP_KEY_ITEM_ROOM_NAME) || mapName.equalsIgnoreCase(BOTTOM_KEY_ITEM_ROOM_NAME))
              if (mapName.contains(keyLocation)) {
                //TODO spawnare la chiave per il boss
              } else {
                //TODO spawnare un oggetto speciale
              }
            else spawnEntity(() => EntitiesFactoryImpl.createImmobileEntity(EntityType.Immobile, size, position, EntityCollisionBit.Immobile, EntityCollisionBit.Hero | EntityCollisionBit.Enemy | EntityCollisionBit.Arrow | EntityCollisionBit.EnemyAttack))
          case "ladder" => spawnEntity(() => EntitiesFactoryImpl.createLadder(position, size))
          case "water" => spawnEntity(() => EntitiesFactoryImpl.createWaterPool(position,size))
          case "lava" => spawnEntity(() => EntitiesFactoryImpl.createLavaPool(position, size))
          case "enemy" =>
            if(mapName.equalsIgnoreCase(BOSS_ROOM_MAP_NAME)) spawnEntity(() => EntitiesFactoryImpl.createWizardBossEnemy(position))
            else spawnEntity(() => EntitiesFactoryImpl.createEnemies(size, position))
          case "portal" => //TODO spawn portal to new world (inactive)
          case _ => println("not supported layer: " + layer.getName)
        }
      })

    })

  }

  private def spawnEntity(f:() => Unit): Unit =
    EntitiesFactoryImpl.addPendingEntityCreation(f)

}

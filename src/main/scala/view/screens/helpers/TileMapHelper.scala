package view.screens.helpers

import _root_.utils.ApplicationConstants.PIXELS_PER_METER
import _root_.utils.MapConstants._
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.{TiledMap, TmxMapLoader}
import com.badlogic.gdx.math.Rectangle
import model.collisions.EntityCollisionBit
import model.collisions.ImplicitConversions._
import model.entities.EntityType
import model.helpers.EntitiesFactoryImpl

import scala.util.Random

class TileMapHelper {

  private val scale: Float = 1/(PIXELS_PER_METER/2)
  private var keyLocation: String = null

  //array: TiledMap, mapName, mapOffset
  private var tiledMapList: Array[(TiledMap, String, (Integer, Integer))] = Array()

  def getMapRenderer(tiledMap: TiledMap): OrthogonalTiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, scale)

  def getTiledMap(mapName: String): TiledMap = new TmxMapLoader().load("assets/maps/" + mapName + ".tmx")

  def loadTiledMaps(): Unit = {
    //scelgo casualmente 6 stanze da mettere nel world (le stanze non devono ripetersi)
    var innerRooms: Array[String] = Array()
    while (innerRooms.length < 6){
      val room: String = INNER_ROOM_MAP_NAMES(Random.nextInt(INNER_ROOM_MAP_NAMES.size))
      if(!innerRooms.contains(room)) innerRooms = innerRooms :+ room
    }

    this.tiledMapList = Array(
      (getTiledMap(WORLD_LEFT_BORDER_NAME), WORLD_LEFT_BORDER_NAME, WORLD_LEFT_BORDER_OFFSET),
      (getTiledMap(WORLD_TOP_BORDER_NAME), WORLD_TOP_BORDER_NAME, WORLD_TOP_BORDER_OFFSET),
      (getTiledMap(WORLD_RIGHT_BORDER_NAME), WORLD_RIGHT_BORDER_NAME, WORLD_RIGHT_BORDER_OFFSET),
      (getTiledMap(WORLD_BOTTOM_BORDER_NAME), WORLD_BOTTOM_BORDER_NAME, WORLD_BOTTOM_BORDER_OFFSET),
      (getTiledMap(HERO_ROOM_MAP_NAME), HERO_ROOM_MAP_NAME, HERO_ROOM_OFFSET),
      (getTiledMap(BOSS_ROOM_MAP_NAME), BOSS_ROOM_MAP_NAME, BOSS_ROOM_OFFSET),
      (getTiledMap(TOP_KEY_ITEM_ROOM_NAME), TOP_KEY_ITEM_ROOM_NAME, TOP_KEY_ITEM_ROOM_OFFSET),
      (getTiledMap(BOTTOM_KEY_ITEM_ROOM_NAME), BOTTOM_KEY_ITEM_ROOM_NAME, BOTTOM_KEY_ITEM_ROOM_OFFSET),
      (getTiledMap(innerRooms(0)), innerRooms(0), INNER_ROOM_MAP_OFFSET(0)),
      (getTiledMap(innerRooms(1)), innerRooms(1), INNER_ROOM_MAP_OFFSET(1)),
      (getTiledMap(innerRooms(2)), innerRooms(2), INNER_ROOM_MAP_OFFSET(2)),
      (getTiledMap(innerRooms(3)), innerRooms(3), INNER_ROOM_MAP_OFFSET(3)),
      (getTiledMap(innerRooms(4)), innerRooms(4), INNER_ROOM_MAP_OFFSET(4)),
      (getTiledMap(innerRooms(5)), innerRooms(5), INNER_ROOM_MAP_OFFSET(5)),
      (getTiledMap(INNER_BORDER_NAMES(Random.nextInt(INNER_BORDER_NAMES.length))), null, INNER_BORDER_OFFSET))
  }

  def renderWorld(orthogonalTiledMapRenderer: OrthogonalTiledMapRenderer) : Unit = {
    tiledMapList.foreach(tiledMap => {

      //setto l'offset di renderizzazione
      tiledMap._1.getLayers.forEach(layer => {
        layer.setOffsetX(tiledMap._3._1*8)
        layer.setOffsetY(tiledMap._3._2*8)
      })

      orthogonalTiledMapRenderer.setMap(tiledMap._1)
      orthogonalTiledMapRenderer.render
    })
  }

  def setWorld(): Unit = {

    val possibleLocation : Array[String] = Array("TOP", "DOWN")
    keyLocation = possibleLocation(Random.nextInt(possibleLocation.length))

    tiledMapList.foreach(room => loadRoomObjects(room._1, room._2, room._3))
  }

  def loadRoomObjects(tiledMap: TiledMap, mapName:String, offset: (Integer, Integer)): Unit = {
    var rect: Rectangle = new Rectangle()

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
            if(mapName.equalsIgnoreCase(BOSS_ROOM_MAP_NAME)){} //TODO spawn boss room door
            else spawnEntity(() => EntitiesFactoryImpl.createDoor(size, position))
          case "chest" =>
            if(mapName.equalsIgnoreCase(TOP_KEY_ITEM_ROOM_NAME) || mapName.equalsIgnoreCase(BOTTOM_KEY_ITEM_ROOM_NAME))
              if (mapName.contains(keyLocation)) {} //TODO spawnare la chiave per il boss
              else {} //TODO spawnare un oggetto speciale
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

  private def spawnEntity(f:() => Unit): Unit = EntitiesFactoryImpl.addPendingEntityCreation(f)
}

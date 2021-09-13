
package view.screens.helpers

import _root_.utils.ApplicationConstants.{PIXELS_PER_METER, RANDOM_SEED, ROOM_MAP_NAMES}
import _root_.utils.MapConstants._
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.{TiledMap, TmxMapLoader}
import com.badlogic.gdx.math.Rectangle
import model.collisions.EntityCollisionBit
import model.collisions.ImplicitConversions._
import model.entities.{EntityType, ItemPools}
import model.helpers.EntitiesFactoryImpl

import scala.util.Random

class TileMapHelper {

  private val scale: Float = 1/(PIXELS_PER_METER/2)
  private var keyLocation: String = null

  //array: TiledMap, mapOffset, mapName
  private var tiledMapList: Array[(TiledMap, (Integer, Integer), String)] = Array()

  def getMapRenderer(tiledMap: TiledMap): OrthogonalTiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, scale)

  def getTiledMap(mapName: String): TiledMap = new TmxMapLoader().load("assets/maps/" + mapName + ".tmx")

//  def loadTiledMaps(seed: Int): Unit = {
  def loadTiledMaps(): Unit = {

    if(RANDOM_SEED%2 == 0) keyLocation = "TOP"
    else keyLocation = "BOTTOM"

    //scelgo casualmente 6 stanze da mettere nel world (le stanze non devono ripetersi)
    val innerRooms: Array[String] = getNonStaticRooms(RANDOM_SEED)

    innerRooms.foreach(elem => println(elem))
    this.tiledMapList = Array(
      (getTiledMap(WORLD_LEFT_BORDER_NAME), WORLD_LEFT_BORDER_OFFSET, null),
      (getTiledMap(WORLD_TOP_BORDER_NAME), WORLD_TOP_BORDER_OFFSET, null),
      (getTiledMap(WORLD_RIGHT_BORDER_NAME), WORLD_RIGHT_BORDER_OFFSET, null),
      (getTiledMap(WORLD_BOTTOM_BORDER_NAME), WORLD_BOTTOM_BORDER_OFFSET, null),
      (getTiledMap(HERO_ROOM_MAP_NAME), HERO_ROOM_OFFSET, null),
      (getTiledMap(BOSS_ROOM_MAP_NAME), BOSS_ROOM_OFFSET, BOSS_ROOM_MAP_NAME),
      (getTiledMap(innerRooms(0)), INNER_ROOM_MAP_OFFSET(0), null),
      (getTiledMap(innerRooms(1)), INNER_ROOM_MAP_OFFSET(1), null),
      (getTiledMap(innerRooms(2)), INNER_ROOM_MAP_OFFSET(2), null),
      (getTiledMap(innerRooms(3)), INNER_ROOM_MAP_OFFSET(3), null),
      (getTiledMap(innerRooms(4)), INNER_ROOM_MAP_OFFSET(4), null),
      (getTiledMap(innerRooms(5)), INNER_ROOM_MAP_OFFSET(5), null),
      (getTiledMap(innerRooms(6)), INNER_BORDER_OFFSET, null),
      (getTiledMap(TOP_KEY_ITEM_ROOM_NAME), TOP_KEY_ITEM_ROOM_OFFSET, TOP_KEY_ITEM_ROOM_NAME),
      (getTiledMap(BOTTOM_KEY_ITEM_ROOM_NAME), BOTTOM_KEY_ITEM_ROOM_OFFSET, BOTTOM_KEY_ITEM_ROOM_NAME)
    )

  }

  def renderWorld(orthogonalTiledMapRenderer: OrthogonalTiledMapRenderer) : Unit = {
    tiledMapList.foreach(tiledMap => {

      //setto l'offset di renderizzazione
      tiledMap._1.getLayers.forEach(layer => {
        layer.setOffsetX(tiledMap._2._1*8)
        layer.setOffsetY(tiledMap._2._2*8)
      })

      orthogonalTiledMapRenderer.setMap(tiledMap._1)
      orthogonalTiledMapRenderer.render
    })
  }

  def setWorld(): Unit = {
    tiledMapList.foreach(room => loadRoomObjects(room._1, room._2, room._3))
  }

  def loadRoomObjects(tiledMap: TiledMap, offset: (Integer, Integer), mapName:String): Unit = {
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
            if(mapName!=null && mapName.equalsIgnoreCase(BOSS_ROOM_MAP_NAME))
              EntitiesFactoryImpl.createBossDoor(size, position)
            else spawnEntity(() => EntitiesFactoryImpl.createDoor(size, position))
          case "chest" =>
            if(mapName!=null && (mapName.equalsIgnoreCase(TOP_KEY_ITEM_ROOM_NAME) || mapName.equalsIgnoreCase(BOTTOM_KEY_ITEM_ROOM_NAME)))
              if (mapName.toUpperCase.contains(keyLocation))
                spawnEntity(() => EntitiesFactoryImpl.createItem(ItemPools.Keys, size, position))
              else
                spawnEntity(() => EntitiesFactoryImpl.createItem(ItemPools.Default, size, position))

            else spawnEntity(() => EntitiesFactoryImpl.createChest(size, position))
          case "ladder" => spawnEntity(() => EntitiesFactoryImpl.createLadder(position, size))
          case "water" => spawnEntity(() => EntitiesFactoryImpl.createWaterPool(position,size))
          case "lava" => spawnEntity(() => EntitiesFactoryImpl.createLavaPool(position, size))
          case "enemy" =>
            if(mapName!=null && mapName.equalsIgnoreCase(BOSS_ROOM_MAP_NAME))
              spawnEntity(() => EntitiesFactoryImpl.spawnBoss(size, position))
            else
              spawnEntity(() => EntitiesFactoryImpl.spawnEnemies(size, position))
          case "portal" => spawnEntity(() => EntitiesFactoryImpl.createPortal(size, position))
          case _ => println("not supported layer: " + layer.getName)
        }
      })

    })

  }

  private def spawnEntity(f:() => Unit): Unit = EntitiesFactoryImpl.addPendingEntityCreation(f)

  //in base al seed restituisce le stanze non fisse: le 6 stanze interne e il bordo interno del world
  private def getNonStaticRooms(seed: Int): Array[String] = {
    var rooms: Array[String] = Array()

    var index = seed % INNER_ROOM_MAP_NAMES.length
    //il secondo index serve per non rimanere nel loop per sempre
    var supportIndex : Integer = 0

    while(rooms.length < 6){
      if(!rooms.contains(INNER_ROOM_MAP_NAMES(index))) rooms = rooms :+ INNER_ROOM_MAP_NAMES(index)
      //aggiorno l'index per la prossima iterazione e il supportIndex per evitare scenari ciclici infiniti
      index = (seed+supportIndex) % INNER_ROOM_MAP_NAMES.length
      supportIndex = supportIndex+1
    }

    //genero un inner-border in base al seed
    index = seed % INNER_BORDER_NAMES.length
    rooms = rooms :+ INNER_BORDER_NAMES(index)

    rooms
  }

}

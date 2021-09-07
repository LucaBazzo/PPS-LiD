package view.screens.helpers

import _root_.utils.ApplicationConstants.PIXELS_PER_METER
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.{TiledMap, TmxMapLoader}
import com.badlogic.gdx.math.Rectangle
import model.collisions.EntityCollisionBit
import model.collisions.ImplicitConversions._
import model.entities.EntityType
import model.helpers.EntitiesFactoryImpl

object TileMapHelper {

  private val scale: Float = 1/(PIXELS_PER_METER/2)
//  private val scale: Float = 1/(PIXELS_PER_METER*4)

  def getMapRenderer(tiledMap: TiledMap): OrthogonalTiledMapRenderer = {
    new OrthogonalTiledMapRenderer(tiledMap, scale)
  }

  //restituisce la tiledMap settando l'offset di renderizzazione
  def getTiledMap(mapName: String, offset: (Integer, Integer)): TiledMap = {
    val tiledMap: TiledMap = new TmxMapLoader().load("assets/maps/" + mapName + ".tmx")

    tiledMap.getLayers.forEach(layer => {
      layer.setOffsetX(offset._1*8)
      layer.setOffsetY(offset._2*8)
    })

    tiledMap
  }

  def setWorld(rooms: Array[(String, (Integer, Integer))]): Unit = {
    rooms.foreach(room => loadRoomObjects("assets/maps/" + room._1 + ".tmx", room._2))
  }

  def loadRoomObjects(path: String, offset: (Integer, Integer)): Unit = {
    var rect: Rectangle = new Rectangle()
    val tiledMap: TiledMap = new TmxMapLoader().load(path)

    tiledMap.getLayers.forEach(layer => {
      layer.getObjects.forEach(obj => {
        rect = obj.asInstanceOf[RectangleMapObject].getRectangle

        val size: (Float, Float) = (rect.getWidth, rect.getHeight)
        val position: (Float, Float) = (
          rect.getX*2 + rect.getWidth + offset._1*16,
          rect.getY*2 + rect.getHeight - offset._2*16)

        layer.getName match {
          case "ground" | "bridge" => spawnEntity(() => EntitiesFactoryImpl.createImmobileEntity(EntityType.Immobile, size, position, EntityCollisionBit.Immobile, EntityCollisionBit.Hero | EntityCollisionBit.Enemy | EntityCollisionBit.Arrow | EntityCollisionBit.EnemyAttack))
          case "door" => spawnEntity(() => EntitiesFactoryImpl.createDoor(size, position))
          case "chest" => spawnEntity(() => EntitiesFactoryImpl.createImmobileEntity(EntityType.Immobile, size, position, EntityCollisionBit.Immobile, EntityCollisionBit.Hero | EntityCollisionBit.Enemy | EntityCollisionBit.Arrow | EntityCollisionBit.EnemyAttack))
          case "ladder" => spawnEntity(() => EntitiesFactoryImpl.createLadder(position, size))
          case "water" => spawnEntity(() => EntitiesFactoryImpl.createWaterPool(position,size))
          case "lava" => spawnEntity(() => EntitiesFactoryImpl.createLavaPool(position, size))
          case "enemy" => spawnEntity(() => EntitiesFactoryImpl.createEnemies(size, position))
          case _ => println("not supported layer: " + layer.getName)
        }
      })

    })

  }

  private def spawnEntity(f:() => Unit): Unit =
    EntitiesFactoryImpl.addPendingEntityCreation(f)

}

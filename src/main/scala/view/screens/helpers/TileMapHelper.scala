package view.screens.helpers

import com.badlogic.gdx.maps.MapProperties
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.{TiledMap, TmxMapLoader}
import com.badlogic.gdx.math.Rectangle
import model.Level
import model.collisions.EntityCollisionBit
import model.collisions.ImplicitConversions._
import model.entities.{Entity, EntityType}
import model.helpers.EntitiesFactoryImpl
import _root_.utils.ApplicationConstants.PIXELS_PER_METER

object TileMapHelper {

  private val scale: Float = 1/(PIXELS_PER_METER/2)

  private var xOffset: Float = 0f
  private var xOffsetRenderer: Float = 0f

  def getMapRenderer(tiledMap: TiledMap): OrthogonalTiledMapRenderer = {
    new OrthogonalTiledMapRenderer(tiledMap, scale)
  }

  //restituisce la tiledMap settando l'offset di renderizzazione relativo all'asse x
  def getTiledMap(mapName: String): TiledMap = {
    val tiledMap: TiledMap = new TmxMapLoader().load("assets/maps/" + mapName + ".tmx")

    tiledMap.getLayers().forEach(layer => {
      layer.setOffsetX(xOffsetRenderer*8)
    })

    //update x offset
    val mapProperties: MapProperties = tiledMap.getProperties
    val width: Integer = mapProperties.get("width", classOf[Integer])
    this.xOffsetRenderer = this.xOffsetRenderer + width

    tiledMap
  }

  def setWorld(level: Level, rooms: Array[String]): Unit = {

    this.xOffset = 0f

    rooms.foreach(room => {
      loadRoomObjects(level, "assets/maps/" + room + ".tmx")
    })
  }

  def loadRoomObjects(level: Level, path: String): Unit = {
    var rect: Rectangle = new Rectangle()
    val tiledMap: TiledMap = new TmxMapLoader().load(path)

    tiledMap.getLayers().forEach(layer => {
      layer.getObjects().forEach(obj => {

        rect = obj.asInstanceOf[RectangleMapObject].getRectangle

        val size: (Float, Float) = (rect.getWidth, rect.getHeight)
        val position: (Float, Float) = ((rect.getX*2 + rect.getWidth + (this.xOffset*16)) , (rect.getY*2 + rect.getHeight) )

        var entity: Entity = null

        layer.getName() match {
          case "ground" | "bridge" => {
              entity = EntitiesFactoryImpl.createImmobileEntity(EntityType.Immobile, size, position, EntityCollisionBit.Immobile, EntityCollisionBit.Hero | EntityCollisionBit.Enemy | EntityCollisionBit.Arrow | EntityCollisionBit.EnemyAttack)
          }
          case "door" => {
            entity = EntitiesFactoryImpl.createDoor(size, position)
          }
          case "chest" => {
            entity = EntitiesFactoryImpl.createImmobileEntity(EntityType.Immobile, size, position, EntityCollisionBit.Immobile, EntityCollisionBit.Hero | EntityCollisionBit.Enemy | EntityCollisionBit.Arrow | EntityCollisionBit.EnemyAttack)
          }
          case "water" | "lava" | "ladder" => {
            entity = EntitiesFactoryImpl.createImmobileEntity(EntityType.Immobile, size, position, EntityCollisionBit.Immobile)
          }
          case "enemy" => {
            entity = EntitiesFactoryImpl.createImmobileEntity(EntityType.Immobile, size, position, EntityCollisionBit.Immobile)
          }
          case _ => {
            println("not supported layer: " + layer.getName())
          }
        }

        if (entity != null) level.addEntity(entity)
      })

    })

    //update x offset
    val mapProperties: MapProperties = tiledMap.getProperties
    val width: Integer = mapProperties.get("width", classOf[Integer])
    this.xOffset = this.xOffset + width
  }

}

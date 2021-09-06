package view.screens.helpers

import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.{TiledMap, TmxMapLoader}
import com.badlogic.gdx.math.Rectangle
import model.Level
import model.collisions.EntityCollisionBit
import model.collisions.ImplicitConversions._
import model.entities.{Entity, EntityType}
import model.helpers.EntitiesFactoryImpl
import utils.ApplicationConstants.PIXELS_PER_METER

object TileMapHelper {

  private val scale: Float = 1/(PIXELS_PER_METER/2)

  def getMap(path: String): OrthogonalTiledMapRenderer = {
    new OrthogonalTiledMapRenderer(new TmxMapLoader().load(path), scale)
  }

  def getTiledMap(path: String): TiledMap = {
    new TmxMapLoader().load(path)
  }

  def setWorld(level: Level, path: String): Unit = {

    var rect: Rectangle = new Rectangle()

    val tiledMap: TiledMap = new TmxMapLoader().load(path)

    tiledMap.getLayers().forEach(layer => {
      layer.getObjects().forEach(obj => {

        rect = obj.asInstanceOf[RectangleMapObject].getRectangle

        val size: (Float, Float) = (rect.getWidth, rect.getHeight)

        val position: (Float, Float) = ((rect.getX*2 + rect.getWidth) , (rect.getY*2 + rect.getHeight) )

        var entity: Entity = null

        layer.getName() match {
          case "ground" | "bridge" => {
            entity = EntitiesFactoryImpl.createImmobileEntity(EntityType.Immobile, size, position, EntityCollisionBit.Immobile, EntityCollisionBit.Hero | EntityCollisionBit.Enemy | EntityCollisionBit.Arrow | EntityCollisionBit.EnemyAttack)
          }
          case "water" | "lava" | "ladder" => {
            entity = EntitiesFactoryImpl.createImmobileEntity(EntityType.Immobile, size, position, EntityCollisionBit.Immobile)
          }
          case _ => {
            println("not supported layer: " + layer.getName())
          }
        }

        if (entity != null) level.addEntity(entity)

      })

    })

  }

}

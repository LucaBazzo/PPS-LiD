package view.screens.helpers

import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.{TiledMap, TmxMapLoader}
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.physics.box2d.World
import model.Level
import model.collisions.EntityType
import model.entities.Entity
import model.helpers.EntitiesFactoryImpl
import utils.ApplicationConstants.PIXELS_PER_METER
import view.screens.game.GameScreen

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
            entity = EntitiesFactoryImpl.createImmobileEntity(size, position, (EntityType.Hero | EntityType.Enemy).asInstanceOf[Short] )
          }
          case "water" | "lava" | "ladder" => {
            entity = EntitiesFactoryImpl.createImmobileEntity(size, position)
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

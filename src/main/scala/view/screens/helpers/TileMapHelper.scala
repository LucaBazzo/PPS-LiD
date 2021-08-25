package view.screens.helpers

import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.{TiledMap, TmxMapLoader}
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.physics.box2d.{Body, BodyDef, FixtureDef, PolygonShape, World}
import model.Level
import model.collisions.EntityType
import model.entities.Entity
import model.helpers.EntitiesFactoryImpl
import utils.ApplicationConstants.PIXELS_PER_METER

object TileMapHelper {

  def getMap(path: String): OrthogonalTiledMapRenderer = {
    new OrthogonalTiledMapRenderer(new TmxMapLoader().load(path), 1 / PIXELS_PER_METER)
  }

  def getTiledMap(path: String): TiledMap = {
    new TmxMapLoader().load(path)
  }

  def setWorld(world: World, tiledMap: TiledMap, level: Level): String = {

    var rect: Rectangle = new Rectangle()
    val bdef: BodyDef = new BodyDef()
    val shape: PolygonShape = new PolygonShape()
    val fdef: FixtureDef = new FixtureDef()
    var body: Body = null

    val tiledMap: TiledMap = new TmxMapLoader().load("assets/maps/level1.tmx")

    tiledMap.getLayers().forEach(layer => {
      layer.getObjects().forEach(obj => {

        rect = obj.asInstanceOf[RectangleMapObject].getRectangle

        val position: (Float, Float) = ((rect.getX + rect.getWidth / 2) , (rect.getY + rect.getHeight / 2) )
        val size: (Float, Float) = (rect.getWidth / 2 , rect.getHeight / 2 )
        val entity: Entity = EntitiesFactoryImpl.createImmobileEntity(size, position, EntityType.Hero )
        level.addEntity(entity)

      })

    })

    "ok"

  }

}

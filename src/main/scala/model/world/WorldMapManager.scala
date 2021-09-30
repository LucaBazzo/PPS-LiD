package model.world

import _root_.utils.ApplicationConstants.PIXELS_PER_METER
import _root_.utils.MapConstants._
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Rectangle
import model.entity._
import model.entity.collision.EntityCollisionBit
import model.helpers.ImplicitConversions._
import model.helpers.{EntitiesFactoryImpl, ItemPools}

/**
 * Utilities for rendering maps and the contained entities
 */
trait WorldMapUtilities {

  /**
   * @return the GDX map renderer for orthogonal maps
   */
  def getMapRenderer(): OrthogonalTiledMapRenderer

  /**
   * Render in the game view the rooms from the room list using the GDX map renderer
   * @param orthogonalTiledMapRenderer
   */
  def renderWorld(orthogonalTiledMapRenderer: OrthogonalTiledMapRenderer): Unit

  /**
   * Instantiate the entities defined in the tiled map
   */
  def createWorldEntities(): Unit

}

/**
 * permit the generation of maps and entities, previously created with Tiled,
 * and the rendering in the screen via GDX map renderer
 */
class TileMapManager extends WorldMapUtilities with TiledMapUtilities {

  private val scale: Float = 1 / (PIXELS_PER_METER / 2)

  override def getMapRenderer(): OrthogonalTiledMapRenderer = new OrthogonalTiledMapRenderer(new TiledMap, scale)

  override def renderWorld(orthogonalTiledMapRenderer: OrthogonalTiledMapRenderer) : Unit = {
    this.getTiledMapList().foreach(elem => {

      //set the render offeset
      elem.tiledMap.getLayers.forEach(layer => {
        layer.setOffsetX(elem.offset._1 * 8)
        layer.setOffsetY(elem.offset._2 * 8)
      })

      orthogonalTiledMapRenderer.setMap(elem.tiledMap)
      orthogonalTiledMapRenderer.render()

    })
  }

  override def createWorldEntities(): Unit = {
    this.getTiledMapList().foreach(elem => createRoomEntities(elem))
  }

  private def createRoomEntities(richTiledMapInfo: RichTiledMapInfo): Unit = {
    var rect: Rectangle = new Rectangle()

    richTiledMapInfo.tiledMap.getLayers.forEach(layer => {
      layer.getObjects.forEach(obj => {
        rect = obj.asInstanceOf[RectangleMapObject].getRectangle

        val size: (Float, Float) = (rect.getWidth, rect.getHeight)
        val position: (Float, Float) = (
          rect.getX * 2 + rect.getWidth + richTiledMapInfo.offset._1 * 16,
          rect.getY * 2 + rect.getHeight - richTiledMapInfo.offset._2 * 16)

        layer.getName match {
          case "ground" => spawnEntity(() => EntitiesFactoryImpl.createImmobileEntity(EntityType.Immobile,
            size, position, EntityCollisionBit.Immobile, EntityCollisionBit.Hero | EntityCollisionBit.Enemy |
              EntityCollisionBit.Arrow | EntityCollisionBit.EnemyAttack))
          case "bridge" => spawnEntity(() => Platform(position, size))
          case "door" => spawnEntity(() => Door(size, position, richTiledMapInfo.name != null && richTiledMapInfo.name.equalsIgnoreCase(BOSS_ROOM_MAP_NAME)))
          case "chest" =>
            if (richTiledMapInfo.name != null && (richTiledMapInfo.name.equalsIgnoreCase(TOP_KEY_ITEM_ROOM_NAME)
              || richTiledMapInfo.name.equalsIgnoreCase(BOTTOM_KEY_ITEM_ROOM_NAME)))
              if (richTiledMapInfo.name.equalsIgnoreCase(keyLocation)) {
                spawnEntity(() => Item(ItemPools.Keys, EntitiesFactoryImpl.getItemPool,
                  EntitiesFactoryImpl.getEntitiesContainerMonitor, size, position))
              } else
                spawnEntity(() => Item(ItemPools.Default, EntitiesFactoryImpl.getItemPool,
                  EntitiesFactoryImpl.getEntitiesContainerMonitor, size, position))
            else spawnEntity(() => Chest(size, position))
          case "ladder" => spawnEntity(() => Ladder(position, size))
          case "water" => spawnEntity(() => WaterPool(position, size))
          case "lava" => spawnEntity(() => LavaPool(position, size))
          case "enemy" =>
            if (richTiledMapInfo.name.equals(BOSS_ROOM_MAP_NAME))
              spawnEntity(() => EntitiesFactoryImpl.spawnBoss(size, position))
            else
              spawnEntity(() => EntitiesFactoryImpl.spawnEnemy(size, position))
          case "portal" => spawnEntity(() => Portal(size, position))
          case _ =>
        }
      })
    })
  }

  private def spawnEntity(f:() => Unit): Unit = EntitiesFactoryImpl.addPendingFunction(f)

}

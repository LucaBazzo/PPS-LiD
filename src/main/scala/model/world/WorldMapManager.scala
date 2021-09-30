package model.world

import _root_.utils.ApplicationConstants.{PIXELS_PER_METER, RANDOM_SEED}
import _root_.utils.MapConstants._
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.{TiledMap, TmxMapLoader}
import com.badlogic.gdx.math.Rectangle
import model.entity._
import model.entity.collision.EntityCollisionBit
import model.helpers.ImplicitConversions._
import model.helpers.{EntitiesFactoryImpl, ItemPools}

case class TiledMapInfo(name: String, offset: (Float, Float))

case class RichTiledMapInfo(name: String, offset: (Float, Float), tiledMap: TiledMap)

trait WorldMapUtilities {

  def getMapRenderer(): OrthogonalTiledMapRenderer

  def updateTiledMapList(): Unit

  def renderWorld(orthogonalTiledMapRenderer: OrthogonalTiledMapRenderer): Unit

  def createWorldEntities(): Unit

}

class TileMapManager extends WorldMapUtilities {

  private val scale: Float = 1 / (PIXELS_PER_METER / 2)
  private var keyLocation: String = _

  private var tiledMapList: List[RichTiledMapInfo] = List.empty

  def getTiledMapList: List[RichTiledMapInfo] = tiledMapList

  override def getMapRenderer(): OrthogonalTiledMapRenderer = new OrthogonalTiledMapRenderer(null, scale)

  override def updateTiledMapList(): Unit = {

    val seed = RANDOM_SEED
    if (seed % 2 == 0) keyLocation = TOP_KEY_ITEM_ROOM_NAME
    else keyLocation = BOTTOM_KEY_ITEM_ROOM_NAME

    //scelgo casualmente 6 stanze da mettere nel world (le stanze non devono ripetersi)
    val innerRooms: List[String] = getNonStaticRooms(seed)

    this.tiledMapList = List(
      TiledMapInfo(WORLD_TOP_BORDER_NAME, WORLD_TOP_BORDER_OFFSET),
      TiledMapInfo(WORLD_BOTTOM_BORDER_NAME, WORLD_BOTTOM_BORDER_OFFSET),
      TiledMapInfo(WORLD_LEFT_BORDER_NAME, WORLD_LEFT_BORDER_OFFSET),
      TiledMapInfo(WORLD_RIGHT_BORDER_NAME, WORLD_RIGHT_BORDER_OFFSET),
      TiledMapInfo(HERO_ROOM_MAP_NAME, HERO_ROOM_OFFSET),
      TiledMapInfo(BOSS_ROOM_MAP_NAME, BOSS_ROOM_OFFSET),
      TiledMapInfo(innerRooms.head, INNER_ROOM_MAP_OFFSET.head),
      TiledMapInfo(innerRooms(1), INNER_ROOM_MAP_OFFSET(1)),
      TiledMapInfo(innerRooms(2), INNER_ROOM_MAP_OFFSET(2)),
      TiledMapInfo(innerRooms(3), INNER_ROOM_MAP_OFFSET(3)),
      TiledMapInfo(innerRooms(4), INNER_ROOM_MAP_OFFSET(4)),
      TiledMapInfo(innerRooms(5), INNER_ROOM_MAP_OFFSET(5)),
      TiledMapInfo(innerRooms(6), INNER_BORDER_OFFSET),
      TiledMapInfo(TOP_KEY_ITEM_ROOM_NAME, TOP_KEY_ITEM_ROOM_OFFSET),
      TiledMapInfo(BOTTOM_KEY_ITEM_ROOM_NAME, BOTTOM_KEY_ITEM_ROOM_OFFSET)
    )
  }

  //in base al seed restituisce le stanze non fisse: le 6 stanze interne e il bordo interno del world
  private def getNonStaticRooms(seed: Int): List[String] = {
    var rooms: List[String] = List()

    var index = seed % INNER_ROOM_MAP_NAMES.length
    //il secondo index serve per non rimanere nel loop per sempre
    var supportIndex: Integer = 0

    while (rooms.length < 6) {
      if (!rooms.contains(INNER_ROOM_MAP_NAMES(index))) rooms = rooms :+ INNER_ROOM_MAP_NAMES(index)
      //aggiorno l'index per la prossima iterazione e il supportIndex per evitare scenari ciclici infiniti
      index = (seed + supportIndex) % INNER_ROOM_MAP_NAMES.length
      supportIndex = supportIndex + 1
    }

    //genero un inner-border in base al seed
    index = seed % INNER_BORDER_NAMES.length
    rooms = rooms :+ INNER_BORDER_NAMES(index)

    rooms
  }

  override def renderWorld(orthogonalTiledMapRenderer: OrthogonalTiledMapRenderer): Unit = {
    tiledMapList.foreach(elem => {

      //setto l'offset di renderizzazione
      elem.tiledMap.getLayers.forEach(layer => {
        layer.setOffsetX(elem.offset._1 * 8)
        layer.setOffsetY(elem.offset._2 * 8)
      })

      orthogonalTiledMapRenderer.setMap(elem.tiledMap)
      orthogonalTiledMapRenderer.render()
    })
  }

  override def createWorldEntities(): Unit = {
    tiledMapList.foreach(elem => createRoomEntities(elem))
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

  private def spawnEntity(f: () => Unit): Unit = EntitiesFactoryImpl.addPendingFunction(f)

  implicit def tileMap2RichTiledMap(tiledMapInfo: TiledMapInfo): RichTiledMapInfo = {
    RichTiledMapInfo(tiledMapInfo.name, tiledMapInfo.offset, new TmxMapLoader().load("assets/maps/" + tiledMapInfo.name + ".tmx"))
  }
}

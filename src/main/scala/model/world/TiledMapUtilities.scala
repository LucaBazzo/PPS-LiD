package model.world

import com.badlogic.gdx.maps.tiled.{TiledMap, TmxMapLoader}
import utils.ApplicationConstants.RANDOM_SEED
import utils.MapConstants._

/**
 * double with information on a map
 * @param name name of the map
 * @param offset offset of the map wrt the view origin
 */
case class TiledMapInfo(name: String, offset: (Float, Float))

/**
 * double with information on a map
 * @param name name of the map
 * @param offset offset of the map wrt the view origin
 * @param tiledMap object containing the tiled map loaded with TMXMapLoader
 */
case class RichTiledMapInfo(name: String, offset: (Float, Float), tiledMap: TiledMap)

trait TiledMapUtilities {

  private var fixedTiledMapList: List[RichTiledMapInfo] = List.empty
  private var nonFixedTiledMapList: List[RichTiledMapInfo] = List.empty

  var keyLocation: String = _

  /**
   * implicit conversion to convert TiledMapInfo in RichTiledMapInfo, with TiledMap loading
   */
  implicit def tileMap2RichTiledMap(tiledMapInfo: TiledMapInfo): RichTiledMapInfo = {
    RichTiledMapInfo(tiledMapInfo.name, tiledMapInfo.offset, new TmxMapLoader().load("assets/maps/" + tiledMapInfo.name + ".tmx"))
  }

  /**
   *  return the list of tiled map actually loaded
   * @return
   */
  def getTiledMapList(): List[RichTiledMapInfo] = fixedTiledMapList.concat(nonFixedTiledMapList)

  /**
   * Updates the list of rooms to be load in the new dungeon based on the seed passed
   * @param seed same seed generates the same dungeon
   */
  def updateTiledMapList(seed: Int): Unit = {
    if (fixedTiledMapList.isEmpty) fixedTiledMapList = getFixedTiledMaps()
    nonFixedTiledMapList = getNonFixedTiledMaps(seed)
  }

  /**
   * Updates the list of rooms to be load in the new dungeon with a randomly generated seed
   */
  def updateTiledMapList(): Unit = {
    val seed = RANDOM_SEED
    if (seed % 2 == 0) keyLocation = TOP_KEY_ITEM_ROOM_NAME
    else keyLocation = BOTTOM_KEY_ITEM_ROOM_NAME

    this.updateTiledMapList(seed)
  }

  private def getFixedTiledMaps(): List[RichTiledMapInfo] = List(
    TiledMapInfo(WORLD_TOP_BORDER_NAME, WORLD_TOP_BORDER_OFFSET),
    TiledMapInfo(WORLD_BOTTOM_BORDER_NAME, WORLD_BOTTOM_BORDER_OFFSET),
    TiledMapInfo(WORLD_LEFT_BORDER_NAME, WORLD_LEFT_BORDER_OFFSET),
    TiledMapInfo(WORLD_RIGHT_BORDER_NAME, WORLD_RIGHT_BORDER_OFFSET),
    TiledMapInfo(HERO_ROOM_MAP_NAME, HERO_ROOM_OFFSET),
    TiledMapInfo(BOSS_ROOM_MAP_NAME, BOSS_ROOM_OFFSET),
    TiledMapInfo(TOP_KEY_ITEM_ROOM_NAME, TOP_KEY_ITEM_ROOM_OFFSET),
    TiledMapInfo(BOTTOM_KEY_ITEM_ROOM_NAME, BOTTOM_KEY_ITEM_ROOM_OFFSET)
  )

  /**
   * @param seed same seeds return the same rooms
   * @return a list containing 6 rooms choosen randomly,
   *         without repeated rooms, and 1 inner border
   */
  private def getNonFixedTiledMaps(seed: Int): List[RichTiledMapInfo] = {
    val innerRooms: List[String] = getNonStaticRooms(seed)
    List(
      TiledMapInfo(innerRooms.head, INNER_ROOM_MAP_OFFSET.head),
      TiledMapInfo(innerRooms(1), INNER_ROOM_MAP_OFFSET(1)),
      TiledMapInfo(innerRooms(2), INNER_ROOM_MAP_OFFSET(2)),
      TiledMapInfo(innerRooms(3), INNER_ROOM_MAP_OFFSET(3)),
      TiledMapInfo(innerRooms(4), INNER_ROOM_MAP_OFFSET(4)),
      TiledMapInfo(innerRooms(5), INNER_ROOM_MAP_OFFSET(5)),
      TiledMapInfo(innerRooms(6), INNER_BORDER_OFFSET),
    )
  }

  /**
   * @param seed same seeds return the same rooms
   * @return non-fixed room names and inner border name
   */
  private def getNonStaticRooms(seed: Int): List[String] = {
    var rooms: List[String] = List()
    //use abs val of the seed cause with negative seed it can obtain negative index
    val absSeed: Int = seed.abs

    var index = absSeed % INNER_ROOM_MAP_NAMES.length
    println(index)
    //second index is usefull to escape the infinite loop
    var supportIndex : Integer = 0

    while(rooms.length < 6){
      if(!rooms.contains(INNER_ROOM_MAP_NAMES(index))) rooms = rooms :+ INNER_ROOM_MAP_NAMES(index)
      //update the index for the next iteration and the supportIndex for avoiding infinite loop
      index = (absSeed+supportIndex) % INNER_ROOM_MAP_NAMES.length
      supportIndex = supportIndex+1
    }

    //choose an inne border based on the seed
    index = absSeed % INNER_BORDER_NAMES.length
    rooms = rooms :+ INNER_BORDER_NAMES(index)

    rooms
  }

}
import com.badlogic.gdx.maps.tiled.TiledMap
import model.world.{RichTiledMapInfo, TileMapManager, TiledMapInfo}
import org.scalatest.flatspec.AnyFlatSpec
import utils.MapConstants._

class TestWorld extends AnyFlatSpec{

  private val tileMapManager: TileMapManager = new TileMapManager
  tileMapManager.setIsTesting()

  "A world" should "have 4 border" in {
    tileMapManager.updateTiledMapList()
    val list: List[RichTiledMapInfo] = tileMapManager.getTiledMapList()

    val bossRoomFilteredList: List[RichTiledMapInfo] =
      list.filter(elem => elem.name.equalsIgnoreCase(WORLD_TOP_BORDER_NAME) |
        elem.name.equalsIgnoreCase(WORLD_RIGHT_BORDER_NAME) |
        elem.name.equalsIgnoreCase(WORLD_BOTTOM_BORDER_NAME) |
        elem.name.equalsIgnoreCase(WORLD_LEFT_BORDER_NAME)
      )
    assert(bossRoomFilteredList.length == 4)

  }

  "A world" should "contain a boss room, an hero room and 2 key item rooms" in {
    tileMapManager.updateTiledMapList()
    val list: List[RichTiledMapInfo] = tileMapManager.getTiledMapList()

    val bossRoomFilteredList: List[RichTiledMapInfo] =
      list.filter(elem => elem.name.equalsIgnoreCase(BOSS_ROOM_MAP_NAME))
    assert(bossRoomFilteredList.length == 1)

    val heroRoomFilteredList: List[RichTiledMapInfo] =
      list.filter(elem => elem.name.equalsIgnoreCase(HERO_ROOM_MAP_NAME))
    assert(heroRoomFilteredList.length == 1)

    val keyItemFilteredList: List[RichTiledMapInfo] =
      list.filter(elem => elem.name.equalsIgnoreCase(TOP_KEY_ITEM_ROOM_NAME) || elem.name.equalsIgnoreCase(BOTTOM_KEY_ITEM_ROOM_NAME))
    assert(keyItemFilteredList.length == 2)
  }

  "A world" should "choose 6 different rooms" in {
    tileMapManager.updateTiledMapList()
    val list: List[RichTiledMapInfo] = tileMapManager.getTiledMapList()

    INNER_ROOM_MAP_NAMES.foreach(innerRoom => {
      assert(list.filter(elem => elem.name.equalsIgnoreCase(innerRoom)).length <= 1)
    })
  }

  "A world" should "should have the same rooms if the seeds are the same" in {
    tileMapManager.updateTiledMapList(50)
    val list1: List[RichTiledMapInfo] = tileMapManager.getTiledMapList()
    tileMapManager.updateTiledMapList(50)
    val list2: List[RichTiledMapInfo] = tileMapManager.getTiledMapList()
    for(index <- 0 to list1.length-1)
      assert(list1(index).name equals list2(index).name)
  }

}

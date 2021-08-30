import model.{Level, LevelImpl}
import model.entities.{Item, ItemPools, Items}
import model.entities.Items.Items
import model.helpers.EntitiesContainerMonitor
import org.scalatest.flatspec.AnyFlatSpec

class TestItemPool extends AnyFlatSpec{

  private val LEVEL_1_ITEMS: List[Items] = List(Items.Cake, Items.Wrench, Items.Map)
  private val LEVEL_2_ITEMS: List[Items] = LEVEL_1_ITEMS ++ List(Items.Armor, Items.SkeletonKey, Items.Boots, Items.BFSword)
  private val BOSS_ITEMS: List[Items] = List(Items.Bow, Items.Shield)

  "An item pool (excluding keys and enemy drops)" should "never give the same item twice unless it has exhausted all its items" in {
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    val level: Level = new LevelImpl(monitor)
    var itemList1: List[Items] = List()
    var itemList2: List[Items] = List()
    var itemList3: List[Items] = List()
    for(_ <- List.range(0, LEVEL_1_ITEMS.length - 1))
    {
      level.spawnItem(ItemPools.Level_1)
    }
    itemList1 = monitor.getEntities(x => x.isInstanceOf[Item]).get.map(x => x.asInstanceOf[Item].getEnumVal)
    for(x <- monitor.getEntities(x => x.isInstanceOf[Item]).get)
      x.destroyEntity()
    level.updateEntities(List.empty)

    for(_ <- List.range(0, LEVEL_2_ITEMS.length))
    {
      level.spawnItem(ItemPools.Level_2)
    }
    itemList2 = monitor.getEntities(x => x.isInstanceOf[Item]).get.map(x => x.asInstanceOf[Item].getEnumVal)
    for(x <- monitor.getEntities(x => x.isInstanceOf[Item]).get)
      x.destroyEntity()
    level.updateEntities(List.empty)

    for(_ <- List.range(0, BOSS_ITEMS.length))
    {
      level.spawnItem(ItemPools.Boss)
    }
    itemList3 = monitor.getEntities(x => x.isInstanceOf[Item]).get.map(x => x.asInstanceOf[Item].getEnumVal)
    for(x <- monitor.getEntities(x => x.isInstanceOf[Item]).get)
      x.destroyEntity()
    level.updateEntities(List.empty)

    assert(itemList1.sorted == LEVEL_1_ITEMS.sorted && itemList2.sorted == LEVEL_2_ITEMS.sorted && itemList3.sorted == BOSS_ITEMS.sorted)
  }

  "An exhausted item pool" should "spawn only cake" in {
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    val level: Level = new LevelImpl(monitor)
    var itemList1: List[Items] = List()
    var itemList2: List[Items] = List()
    var itemList3: List[Items] = List()

    for(_ <- List.range(0, LEVEL_1_ITEMS.length + 1))
    {
      level.spawnItem(ItemPools.Level_1)
    }
    itemList1 = monitor.getEntities(x => x.isInstanceOf[Item]).get.map(x => x.asInstanceOf[Item].getEnumVal)
    for(x <- monitor.getEntities(x => x.isInstanceOf[Item]).get)
      x.destroyEntity()
    level.updateEntities(List.empty)

    for(_ <- List.range(0, LEVEL_2_ITEMS.length + 2))
    {
      level.spawnItem(ItemPools.Level_2)
    }
    itemList2 = monitor.getEntities(x => x.isInstanceOf[Item]).get.map(x => x.asInstanceOf[Item].getEnumVal)
    for(x <- monitor.getEntities(x => x.isInstanceOf[Item]).get)
      x.destroyEntity()
    level.updateEntities(List.empty)

    for(_ <- List.range(0, BOSS_ITEMS.length + 2))
    {
      level.spawnItem(ItemPools.Boss)
    }
    itemList3 = monitor.getEntities(x => x.isInstanceOf[Item]).get.map(x => x.asInstanceOf[Item].getEnumVal)
    for(x <- monitor.getEntities(x => x.isInstanceOf[Item]).get)
      x.destroyEntity()
    level.updateEntities(List.empty)

    assert(itemList1.take(2).forall(x => x == Items.Cake) && itemList2.take(2).forall(x => x == Items.Cake) &&
      itemList3.take(2).forall(x => x == Items.Cake))
  }


}

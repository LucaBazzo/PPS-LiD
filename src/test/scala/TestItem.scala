import controller.GameEvent
import model.entities.Items.Items
import model.entities._
import model.helpers.{EntitiesContainerMonitor, EntitiesFactoryImpl}
import model.{Level, LevelImpl}
import org.scalatest.flatspec.AnyFlatSpec

class TestItem extends AnyFlatSpec {

  private val LEVEL_1_ITEMS: List[Items] = List(Items.Cake, Items.Wrench, Items.Map)
  private val LEVEL_2_ITEMS: List[Items] = LEVEL_1_ITEMS ++ List(Items.Armor, Items.SkeletonKey, Items.Boots, Items.BFSword)
  private val BOSS_ITEMS: List[Items] = List(Items.Bow, Items.Shield)
  private val MAP_ITEMS: List[Items] = List(Items.Key)
  private val ENEMY_ITEMS: List[Items] = List(Items.PotionS, Items.PotionM, Items.PotionL, Items.PotionXL)

  "A Level" must "start with an Item from level 1 pool" in {
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    val level: Level = new LevelImpl(monitor)
    val item: Item = monitor.getEntities(x => x.isInstanceOf[Item]).get.head.asInstanceOf[Item]
    assert(LEVEL_1_ITEMS.contains(item.getEnumVal))
  }

  "In a Level" should "be able to spawn items from every pool" in {
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    val level: Level = new LevelImpl(monitor)
    level.spawnItem(ItemPools.Level_2)
    val item2: Item = monitor.getEntities(x => x.isInstanceOf[Item]
      && LEVEL_2_ITEMS.contains(x.asInstanceOf[Item].getEnumVal)).get.head.asInstanceOf[Item]
    level.spawnItem(ItemPools.Boss)
    val item3: Item = monitor.getEntities(x => x.isInstanceOf[Item]).get.head.asInstanceOf[Item]
    level.spawnItem(ItemPools.Keys)
    val item4: Item = monitor.getEntities(x => x.isInstanceOf[Item]).get.head.asInstanceOf[Item]
    level.spawnItem(ItemPools.Enemy_Drops)
    val item5: Item = monitor.getEntities(x => x.isInstanceOf[Item]).get.head.asInstanceOf[Item]
    assert(LEVEL_2_ITEMS.contains(item2.getEnumVal) && BOSS_ITEMS.contains(item3.getEnumVal)
      && MAP_ITEMS.contains(item4.getEnumVal) && ENEMY_ITEMS.contains(item5.getEnumVal))
  }

  "An Item" should "disappear when picked up" in {
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    val level: Level = new LevelImpl(monitor)
    level.spawnItem(ItemPools.Level_1)
    for(item <- monitor.getEntities(x => x.isInstanceOf[Item]).get)
      item.asInstanceOf[Item].collect()

    EntitiesFactoryImpl.destroyBodies()
    EntitiesFactoryImpl.applyEntityCollisionChanges()
    assert(monitor.getEntities(x => x.isInstanceOf[Item]).get.isEmpty)
  }

  "An Item" should "grant stats when picked up" in {
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    val level: Level = new LevelImpl(monitor)
    level.spawnItem(ItemPools.Level_1)
    val item: Item = monitor.getEntities(x => x.isInstanceOf[Item]).get.head.asInstanceOf[Item]
    val hero: Hero = monitor.getEntities(x => x.isInstanceOf[Hero]).get.head.asInstanceOf[Hero]
    val effect = item.collect()
    val preStat: Float = hero.getStatistics(Statistic.Defence)
    item.collisionDetected(Option.apply(hero))
    assert(hero.getStatistics(effect._1) == preStat + effect._2)
    assert(hero.getItemsPicked.head == item.getEnumVal)
  }
}
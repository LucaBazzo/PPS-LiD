import model.entities.Items.Items
import model.entities._
import model.helpers.EntitiesContainerMonitor
import model.{Level, LevelImpl}
import org.scalatest.flatspec.AnyFlatSpec

class TestItem extends AnyFlatSpec {

  private val DEFAULT_ITEMS: List[Items] = List(Items.Cake, Items.Wrench, Items.Map,
    Items.Armor, Items.SkeletonKey, Items.Boots, Items.BFSword, Items.Shield)
  private val BOSS_ITEMS: List[Items] = List(Items.Bow)
  private val MAP_ITEMS: List[Items] = List(Items.Key)
  private val ENEMY_ITEMS: List[Items] = List(Items.PotionS, Items.PotionM, Items.PotionL, Items.PotionXL)

  "A Level" must "start with an Item from default pool" in {
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    //TODO null temporaneo
    val level: Level = new LevelImpl(null, monitor)
    val item: Item = monitor.getEntities(x => x.isInstanceOf[Item]).get.head.asInstanceOf[Item]
    assert(DEFAULT_ITEMS.contains(item.getEnumVal))
  }

  "In a Level" should "spawn items from every pool" in {
    //TODO null temporaneo
    /*
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    val level: Level = new LevelImpl(monitor)
    val item: Item = monitor.getEntities(x => x.isInstanceOf[Item]).get.head.asInstanceOf[Item]
    level.spawnItem(ItemPools.Boss)
    val item2: Item = monitor.getEntities(x => x.isInstanceOf[Item]).get.head.asInstanceOf[Item]
    level.spawnItem(ItemPools.Keys)
    val item3: Item = monitor.getEntities(x => x.isInstanceOf[Item]).get.head.asInstanceOf[Item]
    level.spawnItem(ItemPools.Enemy_Drops)
    val item4: Item = monitor.getEntities(x => x.isInstanceOf[Item]).get.head.asInstanceOf[Item]
    assert(DEFAULT_ITEMS.contains(item.getEnumVal) && BOSS_ITEMS.contains(item2.getEnumVal)
      && MAP_ITEMS.contains(item3.getEnumVal) && ENEMY_ITEMS.contains(item4.getEnumVal))*/
  }

  "An Item" should "disappear when picked up" in {
    //TODO null temporaneo
    /*
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    val level: Level = new LevelImpl(monitor)
    level.spawnItem(ItemPools.Default)
    for(item <- monitor.getEntities(x => x.isInstanceOf[Item]).get)
      item.asInstanceOf[Item].collect()

    EntitiesFactoryImpl.destroyBodies()
    EntitiesFactoryImpl.applyEntityCollisionChanges()
    assert(monitor.getEntities(x => x.isInstanceOf[Item]).get.isEmpty)*/
  }

  "An Item" should "grant stats when picked up" in {
    //TODO null temporaneo
    /*
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    val level: Level = new LevelImpl(monitor)
    for (_ <- List.range(0, DEFAULT_ITEMS.length)) {
      level.spawnItem(ItemPools.Default)
      val item: Item = monitor.getEntities(x => x.isInstanceOf[Item]).get.head.asInstanceOf[Item]
      val hero: Hero = monitor.getEntities(x => x.isInstanceOf[Hero]).get.head.asInstanceOf[Hero]
      val effect = item.collect()
      var preStat: Float = 0
      var incStat: Float = 0
      if (effect._1.nonEmpty) {
        preStat = hero.getStatistics(effect._1.get.head._1)
        incStat = effect._1.get.head._2
      }
      item.collisionDetected(Option.apply(hero))
      if (effect._1.nonEmpty)
        assert(hero.getStatistics(effect._1.get.head._1) == preStat + incStat)
      assert(hero.getItemsPicked.head == item.getEnumVal)
    }*/
  }

  "A Potion" should "heal the damaged Hero" in {
    //TODO null temporaneo
    /*
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    val level: Level = new LevelImpl(monitor)
    for (_ <- List.range(0, ENEMY_ITEMS.length)) {
      level.spawnItem(ItemPools.Enemy_Drops)
      val item: Item = monitor.getEntities(x => x.isInstanceOf[Item]).get.head.asInstanceOf[Item]
      val hero: Hero = monitor.getEntities(x => x.isInstanceOf[Hero]).get.head.asInstanceOf[Hero]
      hero.sufferDamage(999)
      val prevLife: Float = hero.getStatistics(Statistic.CurrentHealth)
      item.collisionDetected(Option.apply(hero))
      val actLife: Float = hero.getStatistics(Statistic.CurrentHealth)
      assert(item.collect()._1.get.head._2 <= (actLife - prevLife))
    }*/
  }

  "An item pool (excluding keys and enemy drops)" should "never give the same item twice unless it has exhausted all its items" in {
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    //TODO null temporaneo
    /*val level: Level = new LevelImpl(null, monitor)
    var itemList1: List[Items] = List()
    var itemList2: List[Items] = List()
    for(_ <- List.range(0, DEFAULT_ITEMS.length - 1))
    {
      level.spawnItem(ItemPools.Default)
    }
    itemList1 = monitor.getEntities(x => x.isInstanceOf[Item]).get.map(x => x.asInstanceOf[Item].getEnumVal)
    for(x <- monitor.getEntities(x => x.isInstanceOf[Item]).get)
      x.destroyEntity()
    level.updateEntities(List.empty)

    for(_ <- List.range(0, BOSS_ITEMS.length))
    {
      level.spawnItem(ItemPools.Boss)
    }
    itemList2 = monitor.getEntities(x => x.isInstanceOf[Item]).get.map(x => x.asInstanceOf[Item].getEnumVal)
    for(x <- monitor.getEntities(x => x.isInstanceOf[Item]).get)
      x.destroyEntity()
    level.updateEntities(List.empty)

    assert(itemList1.sorted == DEFAULT_ITEMS.sorted && itemList2.sorted == BOSS_ITEMS.sorted)

     */
  }

  "An exhausted item pool" should "spawn only wrench" in {
    //TODO null temporaneo
    /*
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    val level: Level = new LevelImpl(null, monitor)
    var itemList1: List[Items] = List()
    var itemList2: List[Items] = List()

    for(_ <- List.range(0, DEFAULT_ITEMS.length + 1))
    {
      level.spawnItem(ItemPools.Default)
    }
    itemList1 = monitor.getEntities(x => x.isInstanceOf[Item]).get.map(x => x.asInstanceOf[Item].getEnumVal)
    for(x <- monitor.getEntities(x => x.isInstanceOf[Item]).get)
      x.destroyEntity()
    level.updateEntities(List.empty)

    for(_ <- List.range(0, BOSS_ITEMS.length + 2))
    {
      level.spawnItem(ItemPools.Boss)
    }
    itemList2 = monitor.getEntities(x => x.isInstanceOf[Item]).get.map(x => x.asInstanceOf[Item].getEnumVal)
    for(x <- monitor.getEntities(x => x.isInstanceOf[Item]).get)
      x.destroyEntity()
    level.updateEntities(List.empty)

    assert(itemList1.take(2).forall(x => x == Items.Wrench) && itemList2.take(2).forall(x => x == Items.Wrench))
     */
  }
}
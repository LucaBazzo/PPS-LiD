import controller.ModelResources
import model.entity.Items.Items
import model.entity._
import model.helpers.{EntitiesFactoryImpl, ItemPoolImpl, ItemPools}
import model.{Level, LevelImpl}
import org.scalatest.flatspec.AnyFlatSpec

class TestItem extends AnyFlatSpec {

  private val DEFAULT_ITEMS: List[Items] = List(Items.Cake, Items.Wrench, Items.Map,
    Items.Armor, Items.SkeletonKey, Items.Boots, Items.BFSword, Items.Shield)
  private val BOSS_ITEMS: List[Items] = List(Items.Bow)
  private val MAP_ITEMS: List[Items] = List(Items.Key)
  private val ENEMY_ITEMS: List[Items] = List(Items.PotionS, Items.PotionM, Items.PotionL, Items.PotionXL)

  private def initialize(): ModelResources = {
    val monitor: ModelResources = new ModelResources
    EntitiesFactoryImpl.setEntitiesContainerMonitor(monitor)
    //TODO null temporaneo
    val _: Level = new LevelImpl(null, monitor, new ItemPoolImpl())
    monitor
  }

  "In a Level" should "spawn items from every pool" in {
    val monitor: ModelResources = this.initialize()
    Item(ItemPools.Default, EntitiesFactoryImpl.getItemPool, EntitiesFactoryImpl.getEntitiesContainerMonitor)
    val item: Item = monitor.getEntities(x => x.isInstanceOf[Item]).head.asInstanceOf[Item]
    Item(ItemPools.Boss, EntitiesFactoryImpl.getItemPool, EntitiesFactoryImpl.getEntitiesContainerMonitor)
    val item2: Item = monitor.getEntities(x => x.isInstanceOf[Item]).head.asInstanceOf[Item]
    Item(ItemPools.Keys, EntitiesFactoryImpl.getItemPool, EntitiesFactoryImpl.getEntitiesContainerMonitor)
    val item3: Item = monitor.getEntities(x => x.isInstanceOf[Item]).head.asInstanceOf[Item]
    Item(ItemPools.Enemy_Drops, EntitiesFactoryImpl.getItemPool, EntitiesFactoryImpl.getEntitiesContainerMonitor)
    val item4: Item = monitor.getEntities(x => x.isInstanceOf[Item]).head.asInstanceOf[Item]
    assert(DEFAULT_ITEMS.contains(item.getName) && BOSS_ITEMS.contains(item2.getName)
      && MAP_ITEMS.contains(item3.getName) && ENEMY_ITEMS.contains(item4.getName))
  }

  "An Item" should "disappear when picked up" in {
    val monitor: ModelResources = this.initialize()
    Item(ItemPools.Default, EntitiesFactoryImpl.getItemPool, EntitiesFactoryImpl.getEntitiesContainerMonitor)
    for(item <- monitor.getEntities(x => x.isInstanceOf[Item]))
      item.asInstanceOf[Item].collect()

    /*EntitiesFactoryImpl.destroyBodies()
    EntitiesFactoryImpl.applyEntityCollisionChanges()*/
    assert(monitor.getEntities(x => x.isInstanceOf[Item]).isEmpty)
  }

  "An Item" should "grant stats when picked up" in {
    val monitor: ModelResources = this.initialize()
    for (_ <- List.range(0, DEFAULT_ITEMS.length)) {
      Item(ItemPools.Default, EntitiesFactoryImpl.getItemPool, EntitiesFactoryImpl.getEntitiesContainerMonitor)
      val item: Item = monitor.getEntities(x => x.isInstanceOf[Item]).head.asInstanceOf[Item]
      val hero: Hero = monitor.getEntities(x => x.isInstanceOf[Hero]).head.asInstanceOf[Hero]
      val effect = item.collect()
      var preStat: Float = 0
      var incStat: Float = 0
      if (effect._1.nonEmpty) {
        preStat = hero.getStatistics(effect._1.get.head._1)
        incStat = effect._1.get.head._2
      }
      item.collisionDetected(hero)
      if (effect._1.nonEmpty)
        assert(hero.getStatistics(effect._1.get.head._1) == preStat + incStat)
      assert(hero.isItemPicked(item.getName))
    }
  }

  "A Potion" should "heal the damaged Hero" in {
    val monitor: ModelResources = this.initialize()
    for (_ <- List.range(0, ENEMY_ITEMS.length)) {
      Item(ItemPools.Enemy_Drops, EntitiesFactoryImpl.getItemPool, EntitiesFactoryImpl.getEntitiesContainerMonitor)
      val item: Item = monitor.getEntities(x => x.isInstanceOf[Item]).head.asInstanceOf[Item]
      val hero: Hero = monitor.getEntities(x => x.isInstanceOf[Hero]).head.asInstanceOf[Hero]
      hero.sufferDamage(999)
      val prevLife: Float = hero.getStatistics(Statistic.CurrentHealth)
      item.collisionDetected(hero)
      val actLife: Float = hero.getStatistics(Statistic.CurrentHealth)
      assert(item.collect()._1.get.head._2 <= (actLife - prevLife + 1))
    }
  }

  "An item pool (excluding keys and enemy drops)" should "never give the same item twice unless it has exhausted all its items" in {
    val monitor: ModelResources = this.initialize()
    var itemList1: List[Items] = List()
    var itemList2: List[Items] = List()
    for(_ <- List.range(0, DEFAULT_ITEMS.length))
    {
      Item(ItemPools.Default, EntitiesFactoryImpl.getItemPool, EntitiesFactoryImpl.getEntitiesContainerMonitor)
    }
    itemList1 = monitor.getEntities(x => x.isInstanceOf[Item]).map(x => x.asInstanceOf[Item].getName)
    for(x <- monitor.getEntities(x => x.isInstanceOf[Item]))
      x.destroyEntity()

    for(_ <- List.range(0, BOSS_ITEMS.length))
    {
      Item(ItemPools.Boss, EntitiesFactoryImpl.getItemPool, EntitiesFactoryImpl.getEntitiesContainerMonitor)
    }
    itemList2 = monitor.getEntities(x => x.isInstanceOf[Item]).map(x => x.asInstanceOf[Item].getName)
    for(x <- monitor.getEntities(x => x.isInstanceOf[Item]))
      x.destroyEntity()

    assert(itemList1.sorted == DEFAULT_ITEMS.sorted && itemList2.sorted == BOSS_ITEMS.sorted)

  }

  "An exhausted item pool" should "spawn only wrench" in {
    val monitor: ModelResources = this.initialize()
    var itemList1: List[Items] = List()
    var itemList2: List[Items] = List()

    for(_ <- List.range(0, DEFAULT_ITEMS.length + 2))
    {
      Item(ItemPools.Default, EntitiesFactoryImpl.getItemPool, EntitiesFactoryImpl.getEntitiesContainerMonitor)
    }
    itemList1 = monitor.getEntities(x => x.isInstanceOf[Item]).map(x => x.asInstanceOf[Item].getName)
    for(x <- monitor.getEntities(x => x.isInstanceOf[Item]))
      x.destroyEntity()

    for(_ <- List.range(0, BOSS_ITEMS.length + 2))
    {
      Item(ItemPools.Boss, EntitiesFactoryImpl.getItemPool, EntitiesFactoryImpl.getEntitiesContainerMonitor)
    }
    itemList2 = monitor.getEntities(x => x.isInstanceOf[Item]).map(x => x.asInstanceOf[Item].getName)
    for(x <- monitor.getEntities(x => x.isInstanceOf[Item]))
      x.destroyEntity()

    assert(itemList1.take(2).forall(x => x == Items.Wrench) && itemList2.take(2).forall(x => x == Items.Wrench))
  }
}
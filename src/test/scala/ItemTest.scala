
import com.badlogic.gdx.physics.box2d.World
import controller.GameEvent
import controller.GameEvent.GameEvent
import model.entities.Items.Items
import model.{Level, LevelImpl}
import model.entities._
import model.helpers.{EntitiesContainerMonitor, EntitiesSetter}
import org.scalatest.flatspec.AnyFlatSpec

class ItemTest extends AnyFlatSpec {

  private val LEVEL_1_ITEMS: List[Items] = List(Items.Cake, Items.Wrench, Items.Map)
  private val LEVEL_2_ITEMS: List[Items] = LEVEL_1_ITEMS ++ List(Items.Armor, Items.SkeletonKey, Items.Boots, Items.BFSword)
  private val BOSS_ITEMS: List[Items] = List(Items.Bow, Items.Shield)
  private val MAP_ITEMS: List[Items] = List(Items.Key)
  private val ENEMY_ITEMS: List[Items] = List(Items.PotionS, Items.PotionM, Items.PotionL, Items.PotionXL)

  "A Level" must "with an Item from level 1 pool" in {
    val level: Level = new LevelImpl(new EntitiesContainerMonitor)
    val item: ItemImpl = level.getEntity(x => x.isInstanceOf[ItemImpl]).asInstanceOf[ItemImpl]
    assert(LEVEL_1_ITEMS.contains(item.getEnumVal()))
  }

  "In a Level" should "be able to spawn items from every pool" in {
    val level: Level = new LevelImpl(new EntitiesContainerMonitor)
    level.spawnItem(ItemPools.Level_2)
    val item2: ItemImpl = level.getEntity(x => x.isInstanceOf[ItemImpl]
      && LEVEL_2_ITEMS.contains(x.asInstanceOf[ItemImpl].getEnumVal())).asInstanceOf[ItemImpl]
    level.spawnItem(ItemPools.Boss)
    val item3: ItemImpl = level.getEntity(x => x.isInstanceOf[ItemImpl]).asInstanceOf[ItemImpl]
    level.spawnItem(ItemPools.Keys)
    val item4: ItemImpl = level.getEntity(x => x.isInstanceOf[ItemImpl]).asInstanceOf[ItemImpl]
    level.spawnItem(ItemPools.Enemy_Drops)
    val item5: ItemImpl = level.getEntity(x => x.isInstanceOf[ItemImpl]).asInstanceOf[ItemImpl]
    assert(LEVEL_2_ITEMS.contains(item2.getEnumVal()) && BOSS_ITEMS.contains(item3.getEnumVal())
      && MAP_ITEMS.contains(item4.getEnumVal()) && ENEMY_ITEMS.contains(item5.getEnumVal()))
  }

  "An item pool (excluding keys and enemy drops)" should "never give the same item twice unless it has exhausted all its items" in {
    val level: Level = new LevelImpl(new EntitiesContainerMonitor)
    var itemList1: List[ItemImpl] = List()
    var itemList2: List[ItemImpl] = List()
    var itemList3: List[ItemImpl] = List()
    for(n <- List.range(0, LEVEL_1_ITEMS.length))
      {
        level.spawnItem(ItemPools.Level_1)
        itemList1 = itemList1 ++ List(level.getEntity(x => x.isInstanceOf[ItemImpl]).asInstanceOf[ItemImpl])
      }
    for(n <- List.range(0, LEVEL_2_ITEMS.length))
    {
      level.spawnItem(ItemPools.Level_2)
      itemList2 = itemList2 ++ List(level.getEntity(x => x.isInstanceOf[ItemImpl]).asInstanceOf[ItemImpl])
    }
    for(n <- List.range(0, BOSS_ITEMS.length))
    {
      level.spawnItem(ItemPools.Boss)
      itemList3 = itemList3 ++ List(level.getEntity(x => x.isInstanceOf[ItemImpl]).asInstanceOf[ItemImpl])
    }
    assert(itemList1.distinct == itemList1 && itemList2.distinct == itemList2 && itemList3.distinct == itemList3)
  }

  "An exhausted item pool" should "spawn only cake" in {
    val level: Level = new LevelImpl(new EntitiesContainerMonitor)
    var itemList1: List[ItemImpl] = List()
    var itemList2: List[ItemImpl] = List()
    var itemList3: List[ItemImpl] = List()
    for(n <- List.range(0, LEVEL_1_ITEMS.length + 2))
    {
      level.spawnItem(ItemPools.Level_1)
      itemList1 = itemList1 ++ List(level.getEntity(x => x.isInstanceOf[ItemImpl]).asInstanceOf[ItemImpl])
    }
    for(n <- List.range(0, LEVEL_2_ITEMS.length + 2))
    {
      level.spawnItem(ItemPools.Level_2)
      itemList2 = itemList2 ++ List(level.getEntity(x => x.isInstanceOf[ItemImpl]).asInstanceOf[ItemImpl])
    }
    for(n <- List.range(0, BOSS_ITEMS.length + 2))
    {
      level.spawnItem(ItemPools.Boss)
      itemList3 = itemList3 ++ List(level.getEntity(x => x.isInstanceOf[ItemImpl]).asInstanceOf[ItemImpl])
    }
    assert(itemList1.takeRight(2).forall(x => x.getEnumVal() == Items.Cake) && itemList2.takeRight(2).forall(x => x.getEnumVal() == Items.Cake) &&
      itemList3.takeRight(2).forall(x => x.getEnumVal() == Items.Cake))
  }
}
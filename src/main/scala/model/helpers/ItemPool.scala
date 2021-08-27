package model.helpers

import model.EntityBody
import model.collisions.EntityType
import model.entities.ItemPools.ItemPools
import model.entities.Items.Items
import model.entities._

import scala.util.Random

trait ItemPool {
  def getItem(entityBody: EntityBody, size: (Float, Float), PoolName: ItemPools): ItemImpl
}

class ItemPoolImpl extends ItemPool {

  private var level1_Item_List: List[Items] = List(Items.Cake, Items.Wrench, Items.Map)
  private var level2_Item_List: List[Items] = level1_Item_List ++ List(Items.Armor, Items.SkeletonKey, Items.Boots, Items.BFSword)
  private var boss_Item_List: List[Items] = List(Items.Bow, Items.Shield)
  private var map_Item_List: List[Items] = List(Items.Key)
  private var enemy_Item_List: List[Items] = List(Items.PotionS, Items.PotionM, Items.PotionL, Items.PotionXL)
  private val rand = new Random

  override def getItem(entityBody: EntityBody, size: (Float, Float), poolName: ItemPools): ItemImpl = {
    val pickedItem: Items = pickItemFromPool(poolName)
    pickedItem match {
      case Items.Cake => new CakeItem(EntityType.Item, entityBody, size)
      case Items.Wrench => new WrenchItem(EntityType.Item, entityBody, size)
      case Items.Map => new MapItem(EntityType.Item, entityBody, size)
      case Items.PotionS => new SmallPotionItem(EntityType.Item, entityBody, size)
      case Items.PotionM => new PotionItem(EntityType.Item, entityBody, size)
      case Items.PotionL => new LargePotionItem(EntityType.Item, entityBody, size)
      case Items.PotionXL => new HugePotionItem(EntityType.Item, entityBody, size)
      case Items.Armor => new ArmorItem(EntityType.ArmorItem, entityBody, size)
      case Items.Boots => new BootsItem(EntityType.Item, entityBody, size)
      case Items.BFSword => new BFSwordItem(EntityType.Item, entityBody, size)
      case Items.Key => new KeyItem(EntityType.Item, entityBody, size)
      case Items.SkeletonKey => new SkeletonKeyItem(EntityType.Item, entityBody, size)
      case Items.Bow => new BowItem(EntityType.Item, entityBody, size)
      case Items.Shield => new ShieldItem(EntityType.Item, entityBody, size)
      case _ => new CakeItem(EntityType.Item, entityBody, size)
    }
  }

  private def pickItemFromPool(poolName: ItemPools): Items = poolName match {
    case ItemPools.Level_1 => {
                                val item = pickRandomItemFromList(level1_Item_List)
                                level1_Item_List = level1_Item_List.filter(x => x != item)
                                item
                              }
    case ItemPools.Level_2 => {
      val item = pickRandomItemFromList(level2_Item_List)
      level2_Item_List = level2_Item_List.filter(x => x != item)
      item
    }
    case ItemPools.Boss => {
      val item = pickRandomItemFromList(boss_Item_List)
      boss_Item_List = boss_Item_List.filter(x => x != item)
      item
    }
    case ItemPools.Enemy_Drops => pickRandomItemFromList(enemy_Item_List)
    case ItemPools.Keys => pickRandomItemFromList(map_Item_List)
    case _ => Items.Cake
  }

  private def pickRandomItemFromList(itemList: List[Items]): Items = {
    if(itemList.nonEmpty)
      itemList(rand.nextInt(itemList.length))
    else
      Items.Cake
  }
}
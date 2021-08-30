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
      case Items.Cake => new CakeItem(EntityId.CakeItem, entityBody, size)
      case Items.Wrench => new WrenchItem(EntityId.WrenchItem, entityBody, size)
      case Items.Map => new MapItem(EntityId.MapItem, entityBody, size)
      case Items.PotionS => new SmallPotionItem(EntityId.SmallPotionItem, entityBody, size)
      case Items.PotionM => new PotionItem(EntityId.PotionItem, entityBody, size)
      case Items.PotionL => new LargePotionItem(EntityId.LargePotionItem, entityBody, size)
      case Items.PotionXL => new HugePotionItem(EntityId.HugePotionItem, entityBody, size)
      case Items.Armor => new ArmorItem(EntityId.ArmorItem, entityBody, size)
      case Items.Boots => new BootsItem(EntityId.BootsItem, entityBody, size)
      case Items.BFSword => new BFSwordItem(EntityId.BFSwordItem, entityBody, size)
      case Items.Key => new KeyItem(EntityId.KeyItem, entityBody, size)
      case Items.SkeletonKey => new SkeletonKeyItem(EntityId.SkeletonKeyItem, entityBody, size)
      case Items.Bow => new BowItem(EntityId.BowItem, entityBody, size)
      case Items.Shield => new ShieldItem(EntityId.ShieldItem, entityBody, size)
      case _ => new CakeItem(EntityId.CakeItem, entityBody, size)
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
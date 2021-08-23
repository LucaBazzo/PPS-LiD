package model.helpers

import com.badlogic.gdx.physics.box2d.Body
import model.EntityBody
import model.entities.ItemPools.ItemPools
import model.entities.Items.Items
import model.entities.{ArmorItem, BFSwordItem, BootsItem, BowItem, CakeItem, HugePotionItem, ItemImpl, ItemPools, Items, KeyItem, LargePotionItem, MapItem, PotionItem, ShieldItem, SkeletonKeyItem, SmallPotionItem, WrenchItem}

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
      case Items.Cake => new CakeItem(entityBody, size)
      case Items.Wrench => new WrenchItem(entityBody, size)
      case Items.Map => new MapItem(entityBody, size)
      case Items.PotionS => new SmallPotionItem(entityBody, size)
      case Items.PotionM => new PotionItem(entityBody, size)
      case Items.PotionL => new LargePotionItem(entityBody, size)
      case Items.PotionXL => new HugePotionItem(entityBody, size)
      case Items.Armor => new ArmorItem(entityBody, size)
      case Items.Boots => new BootsItem(entityBody, size)
      case Items.BFSword => new BFSwordItem(entityBody, size)
      case Items.Key => new KeyItem(entityBody, size)
      case Items.SkeletonKey => new SkeletonKeyItem(entityBody, size)
      case Items.Bow => new BowItem(entityBody, size)
      case Items.Shield => new ShieldItem(entityBody, size)
      case _ => new CakeItem(entityBody, size)
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
    if(itemList.length > 0)
      itemList(rand.nextInt(itemList.length))
    else
      Items.Cake
  }
}
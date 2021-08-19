package model.helpers

import com.badlogic.gdx.physics.box2d.Body
import model.entities.ItemPools.ItemPools
import model.entities.Items.Items
import model.entities.{ArmorItem, BFSwordItem, BootsItem, BowItem, CakeItem, HugePotionItem, ItemImpl, ItemPools, Items, KeyItem, LargePotionItem, MapItem, PotionItem, ShieldItem, SkeletonKeyItem, SmallPotionItem, WrenchItem}

import scala.util.Random

trait ItemPool {
  def getItem(body: Body, size: (Float, Float), PoolName: ItemPools): ItemImpl
}

class ItemPoolImpl extends ItemPool {

  private var level1_Item_List: List[Items] = List(Items.Cake, Items.Wrench, Items.Map)
  private var level2_Item_List: List[Items] = level1_Item_List ++ List(Items.Armor, Items.SkeletonKey, Items.Boots, Items.BFSword)
  private var boss_Item_List: List[Items] = List()
  private var map_Item_List: List[Items] = List(Items.Key)
  private var enemy_Item_List: List[Items] = List(Items.PotionS, Items.PotionM, Items.PotionL, Items.PotionXL)
  private val rand = new Random

  override def getItem(body: Body, size: (Float, Float), poolName: ItemPools): ItemImpl = {
    val pickedItem: Items = pickItemFromPool(poolName)
    pickedItem match {
      case Items.Cake => new CakeItem(body, size)
      case Items.Wrench => new WrenchItem(body, size)
      case Items.Map => new MapItem(body, size)
      case Items.PotionS => new SmallPotionItem(body, size)
      case Items.PotionM => new PotionItem(body, size)
      case Items.PotionL => new LargePotionItem(body, size)
      case Items.PotionXL => new HugePotionItem(body, size)
      case Items.Armor => new ArmorItem(body, size)
      case Items.Boots => new BootsItem(body, size)
      case Items.BFSword => new BFSwordItem(body, size)
      case Items.Key => new KeyItem(body, size)
      case Items.SkeletonKey => new SkeletonKeyItem(body, size)
      case Items.Bow => new BowItem(body, size)
      case Items.Shield => new ShieldItem(body, size)
      case _ => new CakeItem(body, size)
    }
  }

  private def pickItemFromPool(poolName: ItemPools): Items = poolName match {
    case ItemPools.Level_1 => pickRandomItemFromPool(level1_Item_List)
    case ItemPools.Level_2 => pickRandomItemFromPool(level2_Item_List)
    case ItemPools.Boss => pickRandomItemFromPool(boss_Item_List)
    case ItemPools.Enemy_Drops => pickRandomItemFromPool(enemy_Item_List)
    case ItemPools.Keys => pickRandomItemFromPool(map_Item_List)
    case _ => Items.Cake
  }

  private def pickRandomItemFromPool(itemList: List[Items]): Items = {
    if(itemList.length > 0)
      itemList(rand.nextInt(itemList.length))
    else
      Items.Cake
  }
}
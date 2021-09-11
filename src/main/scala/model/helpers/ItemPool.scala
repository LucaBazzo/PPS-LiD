package model.helpers

import model.EntityBody
import model.collisions.EntityCollisionBit
import model.entities.ItemPools.ItemPools
import model.entities.Items.Items
import model.entities._

import scala.util.Random

trait ItemPool {
  def getItem(entityBody: EntityBody, size: (Float, Float), PoolName: ItemPools): ItemImpl

  def resetBossPool(): Unit
}

class ItemPoolImpl extends ItemPool {

  private var default_Item_List: List[Items] = List(Items.Cake, Items.Wrench, Items.Map, Items.Armor,
    Items.SkeletonKey, Items.Boots, Items.BFSword, Items.Shield)
  private var boss_Item_List: List[Items] = List(Items.Bow)
  private val map_Item_List: List[Items] = List(Items.Key)
  private val enemy_Item_List: List[Items] = List(Items.PotionS, Items.PotionM, Items.PotionL, Items.PotionXL)
  private val rand = new Random

  override def getItem(entityBody: EntityBody, size: (Float, Float), poolName: ItemPools): ItemImpl = {
    val pickedItem: Items = pickItemFromPool(poolName)
    pickedItem match {
      case Items.Cake => new CakeItem(EntityType.CakeItem, entityBody, size)
      case Items.Wrench => new WrenchItem(EntityType.WrenchItem, entityBody, size)
      case Items.Map => new MapItem(EntityType.MapItem, entityBody, size)
      case Items.PotionS => new SmallPotionItem(EntityType.SmallPotionItem, entityBody, size)
      case Items.PotionM => new PotionItem(EntityType.PotionItem, entityBody, size)
      case Items.PotionL => new LargePotionItem(EntityType.LargePotionItem, entityBody, size)
      case Items.PotionXL => new HugePotionItem(EntityType.HugePotionItem, entityBody, size)
      case Items.Armor => new ArmorItem(EntityType.ArmorItem, entityBody, size)
      case Items.Boots => new BootsItem(EntityType.BootsItem, entityBody, size)
      case Items.BFSword => new BFSwordItem(EntityType.BFSwordItem, entityBody, size)
      case Items.Key => new KeyItem(EntityType.KeyItem, entityBody, size)
      case Items.SkeletonKey => new SkeletonKeyItem(EntityType.SkeletonKeyItem, entityBody, size)
      case Items.Bow => new BowItem(EntityType.BowItem, entityBody, size)
      case Items.Shield => new ShieldItem(EntityType.ShieldItem, entityBody, size)
      case _ => new CakeItem(EntityType.CakeItem, entityBody, size)
    }
  }

  private def pickItemFromPool(poolName: ItemPools): Items = poolName match {
    case ItemPools.Default =>
      val item = pickRandomItemFromList(default_Item_List)
      default_Item_List = default_Item_List.filter(x => x != item)
      item
    case ItemPools.Boss =>
      val item = pickRandomItemFromList(boss_Item_List)
      boss_Item_List = boss_Item_List.filter(x => x != item)
      item
    case ItemPools.Enemy_Drops => pickRandomItemFromList(enemy_Item_List)
    case ItemPools.Keys => pickRandomItemFromList(map_Item_List)
    case _ => Items.Cake
  }

  private def pickRandomItemFromList(itemList: List[Items]): Items = {
    if(itemList.nonEmpty)
      itemList(rand.nextInt(itemList.length))
    else
      Items.Wrench
  }

  override def resetBossPool(): Unit = this.boss_Item_List = List(Items.Bow)
}
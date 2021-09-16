package model.entities

import model.entities.EntityType.EntityType
import model.entities.Items.Items
import model.entities.Statistic.Statistic
import model.{EntityBody, Score}
import utils.ItemConstants._

object Items extends Enumeration {
  type Items = Value
  val Armor, Cake, Boots, Shield, Map, Wrench, Key, PotionS, PotionM, PotionL, PotionXL, SkeletonKey, Bow, BFSword = Value
}

trait Item extends Entity with Score {

  def collect(): (Option[List[(Statistic, Float)]], String)
  def getName: Items.Value
  def getDesc: String
}

abstract class ItemImpl(private val entityType:EntityType,
                        private val itemName: Items.Value,
                        private var entityBody: EntityBody,
                        private val size: (Float, Float))
      extends ImmobileEntity(entityType, entityBody, size) with Item {

  def collect(): (Option[List[(Statistic, Float)]], String)

  def getDesc: String

  override def update(): Unit = {}

  override def getName: Items = itemName

  override def getScore: Int = ITEM_SCORE
}

class CakeItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Cake, entityBody, size) {

  override def getDesc: String = CAKE_DESCRIPTION

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.empty, this.getDesc)
  }

}

class WrenchItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Wrench, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = WRENCH_DESCRIPTION

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.apply(List[(Statistic, Float)]((Statistic.Defence, 5), (Statistic.Strength, 5))), this.getDesc)
  }
}

class MapItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Map, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = MAP_DESCRIPTION

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.apply(List[(Statistic, Float)]((Statistic.MovementSpeed, 0.1f), (Statistic.Acceleration, 0.1f))), this.getDesc)
  }
}

class SmallPotionItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.PotionS, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = SMALL_POTION_DESCRIPTION

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.apply(List[(Statistic, Float)]((Statistic.CurrentHealth, 100))), this.getDesc)
  }

  override def getScore: Int = super.getScore / 4
}

class PotionItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.PotionM, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = POTION_DESCRIPTION

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.apply(List[(Statistic, Float)]((Statistic.CurrentHealth, 250))), this.getDesc)
  }

  override def getScore: Int = super.getScore / 4
}

class LargePotionItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.PotionL, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = LARGE_POTION_DESCRIPTION

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.apply(List[(Statistic, Float)]((Statistic.CurrentHealth, 350))), this.getDesc)
  }

  override def getScore: Int = super.getScore / 4
}

class HugePotionItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.PotionXL, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = HUGE_POTION_DESCRIPTION

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.apply(List[(Statistic, Float)]((Statistic.CurrentHealth, 500))), this.getDesc)
  }

  override def getScore: Int = super.getScore / 4
}

class ArmorItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Armor, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = ARMOR_DESCRIPTION

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.apply(List[(Statistic, Float)]((Statistic.Defence, 30))), this.getDesc)
  }
}

class BootsItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Boots, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = BOOTS_DESCRIPTION

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.apply(List[(Statistic, Float)]((Statistic.MovementSpeed, 0.3f))), this.getDesc)
  }
}

class BFSwordItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.BFSword, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = BFSWORD_DESCRIPTION

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.apply(List[(Statistic, Float)]((Statistic.Strength, 50))), this.getDesc)
  }
}

class KeyItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Key, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = KEY_DESCRIPTION

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.empty, this.getDesc)
  }
}

class SkeletonKeyItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.SkeletonKey, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = SKELETON_KEY_DESCRIPTION

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.empty, this.getDesc)
  }
}

class BowItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Bow, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = BOW_DESCRIPTION

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.empty, this.getDesc)
  }

  override def getScore: Int = super.getScore * 2
}

class ShieldItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Shield, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = SHIELD_DESCRIPTION

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.apply(List[(Statistic, Float)]((Statistic.Defence, 10))), this.getDesc)
  }
}
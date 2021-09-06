package model.entities

import model.EntityBody
import model.entities.EntityType.EntityType
import model.entities.Items.Items
import model.entities.Statistic.Statistic
import model.helpers.EntitiesFactoryImpl

object Items extends Enumeration {
  type Items = Value
  val Armor, Cake, Boots, Shield, Map, Wrench, Key, PotionS, PotionM, PotionL, PotionXL, SkeletonKey, Bow, BFSword = Value
}

object ItemPools extends Enumeration {
  type ItemPools = Value
  val Keys, Enemy_Drops, Level_1, Level_2, Boss = Value
}

trait Item extends Entity with Score {

  def collect(): (Statistic, Float, String)
  def getEnumVal: Items.Value
  def getDesc: String
}

abstract class ItemImpl(private val entityType:EntityType,
                        private val itemName: Items.Value,
                        private var entityBody: EntityBody,
                        private val size: (Float, Float))
      extends ImmobileEntity(entityType, entityBody, size) with Item {

  def collect(): (Statistic, Float, String)

  def getDesc: String

  override def getEnumVal: Items = itemName

  override def getScore: Int = 1000
}

class CakeItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Cake, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "The cake is a lie"

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.Defence, 0.5f, this.getDesc)
  }

}

class WrenchItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Wrench, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "Debug time!"

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.Defence, 0.5f, this.getDesc)
  }
}

class MapItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Map, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "Dungeon architect (full visible map)"

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.Defence, 0.5f, this.getDesc)
  }
}

class SmallPotionItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.PotionS, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "Heal 25% of maximum health"

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.CurrentHealth, 25, this.getDesc)
  }
}

class PotionItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.PotionM, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "Heal 50% of maximum health"

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.CurrentHealth, 50, this.getDesc)
  }
}

class LargePotionItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.PotionL, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "Heal 75% of maximum health"

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.CurrentHealth, 75, this.getDesc)
  }
}

class HugePotionItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.PotionXL, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "Heal 100% of maximum health"

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.CurrentHealth, 100, this.getDesc)
  }
}

class ArmorItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Armor, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "You feel protected (+1 defence)"

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.Defence, 10, this.getDesc)
  }
}

class BootsItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Boots, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "There's a time and place for everything, and it's now (speed +1)"

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.MovementSpeed, 1, this.getDesc)
  }
}

class BFSwordItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.BFSword, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "It's dangerous to go alone, take this! (dmg +1)"

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.Strength, 1, this.getDesc)
  }
}

class KeyItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Key, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "Boss Unlocked"

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.Defence, 0.1f, this.getDesc)
  }
}

class SkeletonKeyItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.SkeletonKey, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "Opens the unopenable (Boss is always unlocked)"

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.Defence, 0.5f, this.getDesc)
  }
}

class BowItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Bow, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "New Ranged weapon"

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.Defence, 0.5f, this.getDesc)
  }
}

class ShieldItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Shield, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "Blocks Projectiles"

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.Defence, 0.5f, this.getDesc)
  }
}
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
  def getEnumVal: Items
}

abstract class ItemImpl(private val entityType:EntityType,
                        private val itemName: Items,
                        private var entityBody: EntityBody,
                        private val size: (Float, Float))
      extends ImmobileEntity(entityType, entityBody, size) with Item {

  def collect(): (Statistic, Float, String)

  override def getEnumVal: Items = itemName

  override def getScore: Int = 1000
}

class CakeItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Cake, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.Defence, 0.5f, "The cake is a lie")
  }

}

class WrenchItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Wrench, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.Defence, 0.5f, "Debug time!")
  }
}

class MapItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Map, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.Defence, 0.5f, "Dungeon architect (full visible map)")
  }
}

class SmallPotionItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.PotionS, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.CurrentHealth, 25, "Heal 25% of maximum health")
  }
}

class PotionItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.PotionM, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.CurrentHealth, 50, "Heal 50% of maximum health")
  }
}

class LargePotionItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.PotionL, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.CurrentHealth, 75, "Heal 75% of maximum health")
  }
}

class HugePotionItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.PotionXL, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.CurrentHealth, 100, "Heal 100% of maximum health")
  }
}

class ArmorItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Armor, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.Defence, 10, "You feel protected (+1 defence)")
  }
}

class BootsItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Boots, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.MovementSpeed, 1, "There's a time and place for everything, and it's now (speed +1)")
  }
}

class BFSwordItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.BFSword, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.Strength, 1, "It's dangerous to go alone, take this! (dmg +1)")
  }
}

class KeyItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Key, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.Defence, 0.1f, "Boss Unlocked")
  }
}

class SkeletonKeyItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.SkeletonKey, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.Defence, 0.5f, "Opens the unopenable (Boss is always unlocked)")
  }
}

class BowItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Bow, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.Defence, 0.5f, "New Ranged weapon")
  }
}

class ShieldItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Shield, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.destroyEntity()
    (Statistic.Defence, 0.5f, "Blocks Projectiles")
  }
}
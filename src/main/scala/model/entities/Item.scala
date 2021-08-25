package model.entities

import model.EntityBody
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

trait Item extends Entity {

  def collect(): (Statistic, Float, String)
  def getEnumVal: Items
}

abstract class ItemImpl(private val itemName: Items, private var entityBody: EntityBody, private val size: (Float, Float))
      extends ImmobileEntity(entityBody, size) with Item {

  def collect(): (Statistic, Float, String)

  override def getEnumVal: Items = itemName

  def removeFromPlay(): Unit = EntitiesFactoryImpl.removeEntity(this)
}

class CakeItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(Items.Cake, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.removeFromPlay
    (Statistic.Defence, 0f, "The cake is a lie")
  }
}

class WrenchItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(Items.Wrench, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.removeFromPlay
    (Statistic.Defence, 0f, "Debug time!")
  }
}

class MapItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(Items.Map, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.removeFromPlay
    (Statistic.Defence, 0f, "Dungeon architect (full visible map)")
  }
}

class SmallPotionItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(Items.PotionS, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.removeFromPlay
    (Statistic.CurrentHealth, 25, "Heal 25% of maximum health")
  }
}

class PotionItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(Items.PotionM, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.removeFromPlay
    (Statistic.CurrentHealth, 50, "Heal 50% of maximum health")
  }
}

class LargePotionItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(Items.PotionL, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.removeFromPlay
    (Statistic.CurrentHealth, 75, "Heal 75% of maximum health")
  }
}

class HugePotionItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(Items.PotionXL, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.removeFromPlay
    (Statistic.CurrentHealth, 100, "Heal 100% of maximum health")
  }
}

class ArmorItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(Items.Armor, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.removeFromPlay
    (Statistic.Defence, 1, "You feel protected (+1 defence)")
  }
}

class BootsItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(Items.Boots, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.removeFromPlay
    (Statistic.MovementSpeed, 1, "There's a time and place for everything, and it's now (speed +1)")
  }
}

class BFSwordItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(Items.BFSword, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.removeFromPlay
    (Statistic.Strength, 1, "It's dangerous to go alone, take this! (dmg +1)")
  }
}

class KeyItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(Items.Key, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.removeFromPlay
    (Statistic.Defence, 0f, "Boss Unlocked")
  }
}

class SkeletonKeyItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(Items.SkeletonKey, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.removeFromPlay
    (Statistic.Defence, 0f, "Opens the unopenable (Boss is always unlocked)")
  }
}

class BowItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(Items.Bow, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.removeFromPlay
    (Statistic.Defence, 0f, "New Ranged weapon")
  }
}

class ShieldItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(Items.Shield, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): (Statistic, Float, String) = {
    this.removeFromPlay
    (Statistic.Defence, 0f, "Blocks Projectiles")
  }
}
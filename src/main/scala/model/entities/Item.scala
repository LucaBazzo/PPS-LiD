package model.entities

import model.entities.EntityType.EntityType
import model.entities.Items.Items
import model.entities.Statistic.Statistic
import model.{EntityBody, Score}

object Items extends Enumeration {
  type Items = Value
  val Armor, Cake, Boots, Shield, Map, Wrench, Key, PotionS, PotionM, PotionL, PotionXL, SkeletonKey, Bow, BFSword = Value
}

object ItemPools extends Enumeration {
  type ItemPools = Value
  val Keys, Enemy_Drops, Default, Boss = Value
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

  override def getName: Items = itemName

  override def getScore: Int = 1000
}

class CakeItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Cake, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "The cake is a lie"

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.empty, this.getDesc)
  }

}

class WrenchItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Wrench, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "Debug time! (DEF + and DMG +)"

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.apply(List[(Statistic, Float)]((Statistic.Defence, 5f), (Statistic.Strength, 5f))), this.getDesc)
  }
}

class MapItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Map, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "Dungeon architect (SPD +)"

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.apply(List[(Statistic, Float)]((Statistic.MovementSpeed, 0.1f), (Statistic.Acceleration, 0.1f))), this.getDesc)
  }
}

class SmallPotionItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.PotionS, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "Heal 25% of maximum health"

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.apply(List[(Statistic, Float)]((Statistic.CurrentHealth, 250))), this.getDesc)
  }

  override def getScore: Int = super.getScore / 4
}

class PotionItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.PotionM, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "Heal 50% of maximum health"

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.apply(List[(Statistic, Float)]((Statistic.CurrentHealth, 500))), this.getDesc)
  }

  override def getScore: Int = super.getScore / 4
}

class LargePotionItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.PotionL, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "Heal 75% of maximum health"

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.apply(List[(Statistic, Float)]((Statistic.CurrentHealth, 750))), this.getDesc)
  }

  override def getScore: Int = super.getScore / 4
}

class HugePotionItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.PotionXL, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "Heal 100% of maximum health"

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.apply(List[(Statistic, Float)]((Statistic.CurrentHealth, 1000))), this.getDesc)
  }

  override def getScore: Int = super.getScore / 4
}

class ArmorItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Armor, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "You feel protected (DEF +++)"

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.apply(List[(Statistic, Float)]((Statistic.Defence, 30f))), this.getDesc)
  }
}

class BootsItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Boots, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "There's a time and place for everything, and it's now (SPD +++)"

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.apply(List[(Statistic, Float)]((Statistic.MovementSpeed, 0.3f))), this.getDesc)
  }
}

class BFSwordItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.BFSword, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "It's dangerous to go alone, take this! (DMG +++)"

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.apply(List[(Statistic, Float)]((Statistic.Strength, 50f))), this.getDesc)
  }
}

class KeyItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Key, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "Boss Unlocked"

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.empty, this.getDesc)
  }
}

class SkeletonKeyItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.SkeletonKey, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "Opens the unopenable (Boss is always unlocked)"

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.empty, this.getDesc)
  }
}

class BowItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Bow, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "New Ranged weapon"

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.empty, this.getDesc)
  }

  override def getScore: Int = super.getScore * 2
}

class ShieldItem(private val entityType: EntityType, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Shield, entityBody, size) {
  override def update(): Unit = {}

  override def getDesc: String = "It should block projectiles, i guess (DEF ++)"

  override def collect(): (Option[List[(Statistic, Float)]], String) = {
    this.destroyEntity()
    (Option.apply(List[(Statistic, Float)]((Statistic.Defence, 10f))), this.getDesc)
  }
}
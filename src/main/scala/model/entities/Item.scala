package model.entities

import model.EntityBody
import model.entities.EntityId.EntityId
import model.entities.Items.Items

object Items extends Enumeration {
  type Items = Value
  val Cake, Wrench, Map, PotionS, PotionM, PotionL, PotionXL, Armor, Boots, BFSword, Key, SkeletonKey, Bow, Shield = Value
}

object ItemPools extends Enumeration {
  type ItemPools = Value
  val Keys, Enemy_Drops, Level_1, Level_2, Boss = Value
}

trait Item extends Entity {

  def collect(): Unit
  def getEnumVal: Items
}

abstract class ItemImpl(private val entityType:EntityId,
                        private val itemName: Items,
                        private var entityBody: EntityBody,
                        private val size: (Float, Float))
      extends ImmobileEntity(entityType, entityBody, size) with Item {

  def collect(): Unit

  override def getEnumVal: Items = itemName
}

class CakeItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(EntityId.Immobile, Items.Cake, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class WrenchItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(EntityId.Immobile, Items.Wrench, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class MapItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(EntityId.Immobile, Items.Map, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class SmallPotionItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(EntityId.Immobile, Items.PotionS, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class PotionItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(EntityId.Immobile, Items.PotionM, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class LargePotionItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(EntityId.Immobile, Items.PotionL, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class HugePotionItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(EntityId.Immobile, Items.PotionXL, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class ArmorItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(EntityId.Immobile, Items.Armor, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class BootsItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(EntityId.Immobile, Items.Boots, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class BFSwordItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(EntityId.Immobile, Items.BFSword, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class KeyItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(EntityId.Immobile, Items.Key, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class SkeletonKeyItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(EntityId.Immobile, Items.SkeletonKey, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class BowItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(EntityId.Immobile, Items.Bow, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class ShieldItem(private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(EntityId.Immobile, Items.Shield, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}
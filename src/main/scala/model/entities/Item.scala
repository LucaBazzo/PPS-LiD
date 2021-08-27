package model.entities

import model.EntityBody
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

abstract class ItemImpl(private val entityType: Short, private val itemName: Items, private var entityBody: EntityBody, private val size: (Float, Float))
      extends ImmobileEntity(entityType, entityBody, size) with Item {

  def collect(): Unit

  override def getEnumVal: Items = itemName
}

class CakeItem(private val entityType: Short, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Cake, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class WrenchItem(private val entityType: Short, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Wrench, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class MapItem(private val entityType: Short, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Map, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class SmallPotionItem(private val entityType: Short, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.PotionS, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class PotionItem(private val entityType: Short, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.PotionM, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class LargePotionItem(private val entityType: Short, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.PotionL, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class HugePotionItem(private val entityType: Short, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.PotionXL, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class ArmorItem(private val entityType: Short, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Armor, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class BootsItem(private val entityType: Short, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Boots, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class BFSwordItem(private val entityType: Short, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.BFSword, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class KeyItem(private val entityType: Short, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Key, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class SkeletonKeyItem(private val entityType: Short, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.SkeletonKey, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class BowItem(private val entityType: Short, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Bow, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class ShieldItem(private val entityType: Short, private var entityBody: EntityBody, private val size: (Float, Float)) extends ItemImpl(entityType, Items.Shield, entityBody, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}
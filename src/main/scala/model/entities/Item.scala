package model.entities

import com.badlogic.gdx.physics.box2d.Body
import model.entities.Items.{Items}

object Items extends Enumeration {
  type Items = Value
  val Cake, Wrench, Map, PotionS, PotionM, PotionL, PotionXL, Armor, Boots, BFSword, Key, SkeletonKey, Bow, Shield = Value
}

object ItemPools extends Enumeration {
  type ItemPools = Value
  val Keys, Enemy_Drops, Level_1, Level_2, Boss = Value
}

trait Item {

  def collect()
  def getEnumVal(): Items
}

abstract class ItemImpl(private val itemName: Items, private var body: Body, private val size: (Float, Float))
      extends EntityImpl(body, size) with Item {

  def collect()

  override def getEnumVal(): Items = itemName
}

class CakeItem(private var body: Body, private val size: (Float, Float)) extends ItemImpl(Items.Cake, body, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class WrenchItem(private var body: Body, private val size: (Float, Float)) extends ItemImpl(Items.Wrench, body, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class MapItem(private var body: Body, private val size: (Float, Float)) extends ItemImpl(Items.Map, body, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class SmallPotionItem(private var body: Body, private val size: (Float, Float)) extends ItemImpl(Items.PotionS, body, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class PotionItem(private var body: Body, private val size: (Float, Float)) extends ItemImpl(Items.PotionM, body, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class LargePotionItem(private var body: Body, private val size: (Float, Float)) extends ItemImpl(Items.PotionL, body, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class HugePotionItem(private var body: Body, private val size: (Float, Float)) extends ItemImpl(Items.PotionXL, body, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class ArmorItem(private var body: Body, private val size: (Float, Float)) extends ItemImpl(Items.Armor, body, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class BootsItem(private var body: Body, private val size: (Float, Float)) extends ItemImpl(Items.Boots, body, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class BFSwordItem(private var body: Body, private val size: (Float, Float)) extends ItemImpl(Items.BFSword, body, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class KeyItem(private var body: Body, private val size: (Float, Float)) extends ItemImpl(Items.Key, body, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class SkeletonKeyItem(private var body: Body, private val size: (Float, Float)) extends ItemImpl(Items.SkeletonKey, body, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class BowItem(private var body: Body, private val size: (Float, Float)) extends ItemImpl(Items.Bow, body, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

class ShieldItem(private var body: Body, private val size: (Float, Float)) extends ItemImpl(Items.Shield, body, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}
package model.entities

import com.badlogic.gdx.physics.box2d.Body
import model.entities.ItemList.ItemList

object ItemList extends Enumeration {
  type ItemList = Value
  val Cake = Value
}

trait Item {

  def collect()
  def getEnumVal(): ItemList
}

abstract class ItemImpl(private val itemName: ItemList, private var body: Body, private val size: (Float, Float))
      extends EntityImpl(body, size) with Item {

  def collect()

  override def getEnumVal(): ItemList = itemName
}

class CakeItem(private var body: Body, private val size: (Float, Float)) extends ItemImpl(ItemList.Cake, body, size) {
  override def update(): Unit = {}

  override def collect(): Unit = {}
}

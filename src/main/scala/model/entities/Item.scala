package model.entities

import model.EntityBody

trait Item {

  def collect()
}

abstract class ItemImpl(private var entityBody: EntityBody, private val size: (Float, Float))
      extends EntityImpl(entityBody, size) with Item {

  def collect()
}

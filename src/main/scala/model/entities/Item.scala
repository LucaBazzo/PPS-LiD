package model.entities

import com.badlogic.gdx.physics.box2d.Body

trait Item {

  def collect()
}

abstract class ItemImpl(private var body: Body, private val size: (Float, Float))
      extends EntityImpl(body, size) with Item {

  def collect()
}

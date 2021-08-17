package model.entities

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body

trait MobileEntity {

  def setMovementStrategy()
  def move()
  def getDirection()
}

class MobileEntityImpl(private var body: Body, private val size: (Float, Float)) extends EntityImpl(body, size) with MobileEntity {

  override def update(): Unit = {}

  override def setMovementStrategy(): Unit = ???

  override def move(): Unit = {}

  override def getDirection(): Unit = ???
}
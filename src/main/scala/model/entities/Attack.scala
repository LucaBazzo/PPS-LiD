package model.entities

import model.EntityBody
import model.helpers.EntitiesFactoryImpl

class TimedAttack(private var entityBody: EntityBody, private val size: (Float, Float), private val duration:Long) extends MobileEntityImpl(entityBody, size) {

  private val startTime:Long = System.currentTimeMillis()

  override def update(): Unit = {
    if (System.currentTimeMillis() - startTime > duration) {
      EntitiesFactoryImpl.destroyBody(this.entityBody.getBody)
      EntitiesFactoryImpl.removeEntity(this)
    } else {
      super.update()
    }
  }
}

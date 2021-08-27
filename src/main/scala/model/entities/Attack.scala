package model.entities

import model.EntityBody
import model.entities.Statistic.Statistic
import model.helpers.EntitiesFactoryImpl

class TimedAttack(private val entityType: Short, private var entityBody: EntityBody, private val size: (Float, Float), private val duration:Long, private val statistics:Map[Statistic, Float]) extends MobileEntityImpl(entityType, entityBody, size, statistics) {

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

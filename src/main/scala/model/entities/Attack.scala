package model.entities

import model.EntityBody
import model.entities.Statistic.Statistic
import model.collisions.{DoNothingOnCollision}
import model.entities.EntityId.EntityId
import model.helpers.EntitiesFactoryImpl
import model.movement.StopMoving

class Attack(private val entityType: EntityId,
             private var entityBody: EntityBody,
             private val size: (Float, Float),
             private val stats: Map[Statistic, Float],
             private val lifeTime: Option[Long] = Option.empty,
             private val dyingStateDuration:Long = 1000)
  extends MobileEntityImpl(entityType, entityBody, size, stats) {

  private val spawnTime:Long = System.currentTimeMillis()
  private var dyingStateTimer: Long = 0

  this.state = State.Running

  override def update(): Unit = {
    if (lifeTime.isDefined && System.currentTimeMillis() - spawnTime > lifeTime.get) {
      this.state = State.Dying
    }

    if (this.state == State.Dying) {
      this.movementStrategy = new StopMoving(this)
      this.collisionStrategy = new DoNothingOnCollision()

      if (dyingStateTimer == 0) {
        this.dyingStateTimer = System.currentTimeMillis()
      } else if (System.currentTimeMillis() - dyingStateTimer > dyingStateDuration) {
        EntitiesFactoryImpl.destroyBody(this.getBody)
        EntitiesFactoryImpl.removeEntity(this)
      }
    }
  }
}
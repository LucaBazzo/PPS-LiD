package model.entities

import model.EntityBody
import model.collisions.DoNotCollide
import model.entities.AttackType.AttackType
import model.helpers.EntitiesFactoryImpl
import model.movement.StopMoving

object AttackType extends Enumeration {
  type AttackType = Value
  val FireBallAttack, ArrowAttack, Undefined = Value
}

trait Attack extends MobileEntity {
  def getAttackType: AttackType
}

class AttackImpl(private val entityType: Short,
                 private var entityBody: EntityBody,
                 private val size: (Float, Float),
                 private val attackType: AttackType,
                 private val lifeTime: Option[Long] = Option.empty,
                 private val dyingStateDuration:Long = 1000)
  extends MobileEntityImpl(entityType, entityBody, size) with Attack {

  private val spawnTime:Long = System.currentTimeMillis()
  private var dyingStateTimer: Long = 0

  this.state = State.Running

  override def update(): Unit = {
    if (lifeTime.isDefined && System.currentTimeMillis() - spawnTime > lifeTime.get) {
      this.state = State.Dying
    }

    if (this.state == State.Dying) {
      this.movementStrategy = new StopMoving(this)
      this.collisionStrategy = new DoNotCollide()

      if (dyingStateTimer == 0) {
        this.dyingStateTimer = System.currentTimeMillis()
      } else if (System.currentTimeMillis() - dyingStateTimer > dyingStateDuration) {
        EntitiesFactoryImpl.destroyBody(this.getBody)
        EntitiesFactoryImpl.removeEntity(this)
      }
    }
  }

  override def getAttackType: AttackType = this.attackType
}
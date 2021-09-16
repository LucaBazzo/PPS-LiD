package model.entities

import model.EntityBody
import model.attack.{AttackStrategy, DoNothingAttackStrategy}
import model.entities.EntityType.EntityType
import model.entities.Statistic.Statistic

trait LivingEntity extends MobileEntity {

  def healLife(heal: Float)
  def sufferDamage(damage: Float)
  def getLife: Float
  def setAttackStrategy(strategy: AttackStrategy)
}

class LivingEntityImpl(private val entityType: EntityType,
                       private var entityBody: EntityBody,
                       private val size: (Float, Float),
                       private var stats: Map[Statistic, Float])
  extends MobileEntityImpl(entityType, entityBody, size, stats) with LivingEntity {

  protected var attackStrategy: AttackStrategy = DoNothingAttackStrategy()

  protected var dyingStateTimer:Long = 0
  protected val dyingStateDuration:Long = 1000

  override def update(): Unit = {
    if (this.state == State.Dying) {
      if (dyingStateTimer == 0) {
        this.dyingStateTimer = System.currentTimeMillis()
      } else if (System.currentTimeMillis() - dyingStateTimer > dyingStateDuration) {
        this.destroyEntity()
      }
    }
  }

  override def healLife(heal: Float): Unit = {
    if(this.getStatistic(Statistic.Health).nonEmpty && this.getStatistic(Statistic.CurrentHealth).nonEmpty){
      val currentHealth = this.getStatistic(Statistic.CurrentHealth).get
      val totalHealth = this.getStatistic(Statistic.Health).get

      val maxHeal = totalHealth - currentHealth
      if(heal > maxHeal)
        this.alterStatistics(Statistic.CurrentHealth, maxHeal)
      else
        this.alterStatistics(Statistic.CurrentHealth, heal)
    }
  }

  override def sufferDamage(damage: Float): Unit = {
    if(this.getStatistic(Statistic.Defence).nonEmpty && this.getStatistic(Statistic.CurrentHealth).nonEmpty){
      val currentHealth = this.getStatistic(Statistic.CurrentHealth).get
      val defence = this.getStatistic(Statistic.Defence).get

      var trueDamage = damage - defence
      if(trueDamage > 0) {
        if(currentHealth < trueDamage) {
          trueDamage = currentHealth
        }
        this.alterStatistics(Statistic.CurrentHealth, -trueDamage)
      }
      if (this.getStatistic(Statistic.CurrentHealth).get <= 0) {
        this.setState(State.Dying)
      }
    }
  }

  override def getLife: Float = this.getStatistic(Statistic.CurrentHealth).get

  override def setAttackStrategy(strategy: AttackStrategy): Unit = this.attackStrategy = strategy

  override def alterStatistics(statistic: Statistic, alteration: Float): Unit = {
    super.alterStatistics(statistic, alteration)

    statistic match {
      case Statistic.Strength => this.attackStrategy.alterStrength(alteration)
      case _ =>
    }
  }
}

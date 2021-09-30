package model.entity

import model.EntityBody
import model.entity.attack._
import model.entity.EntityType.EntityType
import model.entity.State._
import model.entity.Statistic.Statistic
import utils.EnemiesConstants.ENEMIES_DYING_STATE_DURATION

/** A mobile entity that can attack and die. The attack behaviour is extracted
 * and managed by a AttackStrategy class object.
 *
 * @see [[model.entity.movement.MovementStrategy]]
 */
trait LivingEntity extends MobileEntity {

  /** Cure the life of the living entity. The life is maintained by a
   * corresponding statistic.
   *
   * @param heal the amount of life to be healed
   */
  def healLife(heal: Float): Unit

  /** Apply a damage to the living entity. The damage is applyed to
   * the life statistic.
   *
   * @param damage the amount of life to subtract
   */
  def sufferDamage(damage: Float): Unit

  /** Returns the current amount of life of an entity. A living entity is
   * considered dead when it's life is 0
   *
   * @return a float value
   */
  def getLife: Float = if(this.getStatistic(Statistic.CurrentHealth).nonEmpty)
    this.getStatistic(Statistic.CurrentHealth).get else 0

  /** Defines the new attack strategy to use.
   *
   * @see [[model.entity.attack.AttackStrategy]]
   *
   * @param strategy the new attack strategy to apply
   */
  def setAttackStrategy(strategy: AttackStrategy): Unit
}

/** Implementation of the LivingEntity interface. This class is defined by
 * mixing in the MobileEntity implementation with the LivingEntity required
 * behaviours.
 *
 * @param entityType the texture that will be attached to this Entity by the View
 * @param entityBody the body of this entity that is affected by physics and collisions
 * @param size the size of the entity
 * @param stats the statistics of this entity
 *
 */
class LivingEntityImpl(private val entityType: EntityType,
                       private var entityBody: EntityBody,
                       private val size: (Float, Float),
                       private var stats: Map[Statistic, Float])
  extends MobileEntityImpl(entityType, entityBody, size, stats) with LivingEntity {

  protected var attackStrategy: AttackStrategy = DoNothingAttackStrategy()

  protected var dyingStateTimer:Long = 0

  override def update(): Unit = {
    if (this is Dying) {
      if (dyingStateTimer == 0) {
        this.dyingStateTimer = System.currentTimeMillis()
      } else if (System.currentTimeMillis() - dyingStateTimer > ENEMIES_DYING_STATE_DURATION) {
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

  override def setAttackStrategy(strategy: AttackStrategy): Unit = this.attackStrategy = strategy

  override def alterStatistics(statistic: Statistic, alteration: Float): Unit = {
    super.alterStatistics(statistic, alteration)

    statistic match {
      case Statistic.Strength => this.attackStrategy.alterStrength(alteration)
      case _ =>
    }
  }
}

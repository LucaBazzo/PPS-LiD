package model.entities

import model.behaviour.EnemyBehaviours
import model.collisions.EntityCollisionBit
import model.collisions.ImplicitConversions._
import model.entities.EntityType.EntityType
import model.entities.Items.Items
import model.entities.State._
import model.entities.Statistic.Statistic
import utils.ApplicationConstants.RANDOM
import model.helpers.{EntitiesFactoryImpl, ItemPools}
import model.{EntityBody, Score}
import utils.EnemiesConstants.{ENEMY_BOSS_TYPES, ENEMY_TYPES}

trait Enemy {
  // TODO: rifattorizzare a livello di living entity?
  def setBehaviour(enemyBehaviour: EnemyBehaviours): Unit
}

class EnemyImpl(private val entityType: EntityType,
                private var entityBody: EntityBody,
                private val size: (Float, Float),
                private val stats: Map[Statistic, Float],
                private val score: Int = 100,
                private val heroEntity: Hero) extends LivingEntityImpl(entityType, entityBody, size, stats)
          with LivingEntity with Score with Enemy {

  var behaviours:Option[EnemyBehaviours] = None

  override def getScore: Int = this.score

  override def update(): Unit = {
    super.update()
    if (this isNot Dying) {
      if (this.behaviours.isDefined) {
        this.behaviours.get.update

        this.movementStrategy = this.behaviours.get.getMovementStrategy
        this.attackStrategy = this.behaviours.get.getAttackStrategy
      }
      this.movementStrategy.apply()
      this.attackStrategy.apply()
    }
  }

  override def setBehaviour(behaviours: EnemyBehaviours): Unit = this.behaviours = Option(behaviours)

  override def sufferDamage(damage: Float): Unit = {
    super.sufferDamage(damage)
    if (this is Dying) {
      this.behaviours.get.getAttackStrategy.stopAttack()
    }
  }

  override def destroyEntity(): Unit = {
    super.destroyEntity()

    if (ENEMY_TYPES.contains(this.entityType)) {
      if(RANDOM.nextInt(10) <= 2)
      Item(ItemPools.Enemy_Drops, EntitiesFactoryImpl.getItemPool, EntitiesFactoryImpl.getEntitiesContainerMonitor,
        position=(this.getPosition._1, this.getPosition._2).MPP)
    }

    if (ENEMY_BOSS_TYPES.contains(this.getType)) {
      if (this.heroEntity.getItemsPicked.contains((i:Items) => i == Items.Bow)) {
        Item(ItemPools.Default, EntitiesFactoryImpl.getItemPool, EntitiesFactoryImpl.getEntitiesContainerMonitor,
          position=(this.getPosition._1, this.getPosition._2).MPP)
      } else
        Item(ItemPools.Boss, EntitiesFactoryImpl.getItemPool, EntitiesFactoryImpl.getEntitiesContainerMonitor,
          position=(this.getPosition._1, this.getPosition._2).MPP)
    }
  }
}


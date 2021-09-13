package model.entities

import model.collisions.EntityCollisionBit
import model.collisions.ImplicitConversions._
import model.entities.EntityType.EntityType
import model.entities.Items.Items
import model.entities.Statistic.Statistic
import model.helpers.EntitiesFactoryImpl
import model.{EnemyBehaviour, EntityBody, Score}
import utils.EnemiesConstants.{ENEMY_BOSS_TYPES, ENEMY_TYPES}

import scala.util.Random

trait Enemy {
  // TODO: rifattorizzare a livello di living entity?
  def setBehaviour(enemyBehaviour: EnemyBehaviour): Unit
}

class EnemyImpl(private val entityType: EntityType,
                private var entityBody: EntityBody,
                private val size: (Float, Float),
                private val stats: Map[Statistic, Float],
                private val score: Int = 100,
                private val heroEntity: Hero) extends LivingEntityImpl(entityType, entityBody, size, stats)
          with LivingEntity with Score with Enemy {

  var enemyBehaviour:Option[EnemyBehaviour] = None
  private val rand = new Random

  override def getScore: Int = this.score

  override def update(): Unit = {
    super.update()
    if (state != State.Dying) {
      if (this.enemyBehaviour.isDefined) {
        this.enemyBehaviour.get.apply()

        this.movementStrategy = this.enemyBehaviour.get.getMovementStrategy
        this.attackStrategy = this.enemyBehaviour.get.getAttackStrategy
      }
      this.movementStrategy.apply()
      this.attackStrategy.apply()
    }

  }

  override def setBehaviour(enemyBehaviour: EnemyBehaviour): Unit = this.enemyBehaviour = Option(enemyBehaviour)

  override def sufferDamage(damage: Float): Unit = {
    super.sufferDamage(damage)
    if (this.state == State.Dying) {
      this.enemyBehaviour.get.getAttackStrategy.stopAttack()
    }
  }

  override def destroyEntity(): Unit = {
    super.destroyEntity()

    if (ENEMY_TYPES.contains(this.entityType)) {
      if(rand.nextInt(10) <= 2)
      EntitiesFactoryImpl.createItem(ItemPools.Enemy_Drops,
        position=(this.getPosition._1, this.getPosition._2).MPP,
        collisions = EntityCollisionBit.Hero)
    }

    if (ENEMY_BOSS_TYPES.contains(this.getType)) {
      if (this.heroEntity.getItemsPicked.contains((i:Items) => i == Items.Bow)) {
        EntitiesFactoryImpl.createItem(ItemPools.Enemy_Drops,
          position=(this.getPosition._1, this.getPosition._2).MPP,
          collisions = EntityCollisionBit.Hero)
      } else
        EntitiesFactoryImpl.createItem(ItemPools.Boss,
          position=(this.getPosition._1, this.getPosition._2).MPP,
          collisions = EntityCollisionBit.Hero)
    }
  }
}


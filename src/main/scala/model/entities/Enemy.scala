package model.entities

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import model.behaviour._
import model.collisions.EntityCollisionBit
import model.collisions.ImplicitConversions._
import model.entities.EntityType.EntityType
import model.entities.Items.Items
import model.entities.State._
import model.entities.Statistic.Statistic
import model.helpers.EntitiesFactoryImpl._
import model.helpers.ItemPools
import model.{EntityBody, Score}
import utils.ApplicationConstants.RANDOM
import utils.CollisionConstants.ENEMY_COLLISIONS
import utils.EnemiesConstants._

trait Enemy {
  // TODO: rifattorizzare a livello di living entity?
  def setBehaviour(enemyBehaviour: EnemyBehaviours): Unit
}

object Enemy {
  private val heroEntity: () => Hero =
    () => getEntitiesContainerMonitor.getHero.getOrElse(throw new IllegalArgumentException())

  private val levelNumber: () => Int =
    () => getEntitiesContainerMonitor.getLevelNumber

  private def createEnemyEntity(position: (Float, Float),
                        size: (Float, Float),
                        stats: Map[Statistic, Float],
                        statsModifiers: Map[Statistic, Float],
                        score: Int,
                        entityId: EntityType): EnemyImpl = {

    val spawnPoint = (position._1, position._2 + size._2)
    val levelBasedStats =
      stats.map { case (key, value) => (key, value + levelNumber() * statsModifiers.getOrElse(key, 0f)) }

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityCollisionBit.Enemy,
      ENEMY_COLLISIONS, createPolygonalShape(size.PPM, rounder = true), spawnPoint.PPM)

    val enemy: EnemyImpl = new EnemyImpl(entityId, entityBody, size.PPM, levelBasedStats, score, heroEntity())
    addEntity(enemy)
    enemy
  }

  def createSkeletonEnemy(position: (Float, Float)): EnemyImpl = {
    val enemy: EnemyImpl = createEnemyEntity(position, SKELETON_SIZE,
      SKELETON_STATS, STATS_MODIFIER, SKELETON_SCORE, EntityType.EnemySkeleton)

    enemy.setBehaviour(SkeletonEnemyBehaviour(enemy, heroEntity()))
    enemy
  }

  def createSlimeEnemy(position: (Float, Float)): EnemyImpl = {
    // easter egg: a slime could rarely be displayed as Pacman with a 5% chance
    val enemyType = if (RANDOM.nextInt(100) <= 5) EntityType.EnemyPacman else EntityType.EnemySlime

    val enemy: EnemyImpl = createEnemyEntity(position,
      SLIME_SIZE, SLIME_STATS, STATS_MODIFIER, SLIME_SCORE, enemyType)

    enemy.setBehaviour(SlimeEnemyBehaviour(enemy, heroEntity()))
    enemy
  }

  def createWormEnemy(position: (Float, Float)): EnemyImpl = {
    val enemy: EnemyImpl = createEnemyEntity(position, WORM_SIZE,
      WORM_STATS, STATS_MODIFIER, WORM_SCORE, EntityType.EnemyWorm)

    enemy.setBehaviour(WormEnemyBehaviour(enemy, heroEntity()))
    enemy
  }

  def createWizardBossEnemy(position: (Float, Float)): EnemyImpl = {
    val enemy: EnemyImpl = createEnemyEntity(position, WIZARD_BOSS_SIZE, WIZARD_BOSS_STATS, STATS_MODIFIER,
      WIZARD_BOSS_SCORE, EntityType.EnemyBossWizard)

    enemy.setBehaviour(WizardEnemyBehaviour(enemy, heroEntity()))
    enemy
  }
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
      Item(ItemPools.Enemy_Drops, getItemPool, getEntitiesContainerMonitor,
        position=(this.getPosition._1, this.getPosition._2).MPP)
    }

    if (ENEMY_BOSS_TYPES.contains(this.getType)) {
      if (this.heroEntity.getItemsPicked.contains((i:Items) => i == Items.Bow)) {
        Item(ItemPools.Default, getItemPool, getEntitiesContainerMonitor,
          position=(this.getPosition._1, this.getPosition._2).MPP)
      } else
        Item(ItemPools.Boss, getItemPool, getEntitiesContainerMonitor,
          position=(this.getPosition._1, this.getPosition._2).MPP)
    }
  }
}


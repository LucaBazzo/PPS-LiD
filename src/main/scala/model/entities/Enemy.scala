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
import utils.EnemiesConstants
import utils.EnemiesConstants._
import utils.HeroConstants.SHORT_WAIT_TIME

trait Enemy {
  def setBehaviour(enemyBehaviour: EnemyBehavioursImpl): Unit
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
      SKELETON_STATS, STATS_MODIFIER, ENEMY_SCORE, EntityType.EnemySkeleton)

    enemy.setBehaviour(SkeletonEnemyBehaviour(enemy, heroEntity()))
    enemy
  }

  def createSlimeEnemy(position: (Float, Float)): EnemyImpl = {
    // easter egg: a slime could rarely be displayed as Pacman with a 5% chance
    val enemyType = if (RANDOM.nextInt(100) <= 5) EntityType.EnemyPacman else EntityType.EnemySlime

    val enemy: EnemyImpl = createEnemyEntity(position,
      SLIME_SIZE, SLIME_STATS, STATS_MODIFIER, ENEMY_SCORE, enemyType)

    enemy.setBehaviour(SlimeEnemyBehaviour(enemy, heroEntity()))
    enemy
  }

  def createWormEnemy(position: (Float, Float)): EnemyImpl = {
    val enemy: EnemyImpl = createEnemyEntity(position, WORM_SIZE,
      WORM_STATS, STATS_MODIFIER, ENEMY_SCORE, EntityType.EnemyWorm)

    enemy.setBehaviour(WormEnemyBehaviour(enemy, heroEntity()))
    enemy
  }

  def createWizardBossEnemy(position: (Float, Float)): EnemyImpl = {
    val enemy: EnemyImpl = createEnemyEntity(position, WIZARD_BOSS_SIZE, WIZARD_BOSS_STATS, STATS_MODIFIER,
      BOSS_SCORE, EntityType.EnemyBossWizard)

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

  var behaviours:Option[EnemyBehavioursImpl] = None
  var timer: Long = 0


  override def getScore: Int = this.score

  override def update(): Unit = {
    super.update()
    if ((this isNot Dying) && (this isNot Hurt)) {
      this.behaviours.get.update

      this.movementStrategy = this.behaviours.get.getMovementStrategy
      this.attackStrategy = this.behaviours.get.getAttackStrategy

      this.movementStrategy.apply()
      this.attackStrategy.apply()
    }

    if (this.timer != 0 && System.currentTimeMillis() - this.timer > SHORT_WAIT_TIME) {
      this setState State.Standing
      this.timer = 0
    }
  }

  override def setBehaviour(behaviours: EnemyBehavioursImpl): Unit = this.behaviours = Option(behaviours)

  override def sufferDamage(damage: Float): Unit = {
    super.sufferDamage(damage)
    if (this is Dying) {
      this.behaviours.get.getAttackStrategy.stopAttack()
    } else if (damage > 0) {
      this.setState(State.Hurt)
      this.timer = System.currentTimeMillis()
    }
  }

  override def destroyEntity(): Unit = {
    super.destroyEntity()

    if (ENEMY_TYPES.contains(this.entityType))
      if(RANDOM.nextFloat() <= EnemiesConstants.ENEMIES_DROP_RATE)
        Item(ItemPools.Enemy_Drops, getItemPool, getEntitiesContainerMonitor,
          position=(this.getPosition._1, this.getPosition._2).MPP)

    if (ENEMY_BOSS_TYPES.contains(this.getType))
      if (this.heroEntity.getItemsPicked.contains((i:Items) => i == Items.Bow))
        Item(ItemPools.Default, getItemPool, getEntitiesContainerMonitor,
          position=(this.getPosition._1, this.getPosition._2).MPP)
      else
        Item(ItemPools.Boss, getItemPool, getEntitiesContainerMonitor,
          position=(this.getPosition._1, this.getPosition._2).MPP)
  }
}


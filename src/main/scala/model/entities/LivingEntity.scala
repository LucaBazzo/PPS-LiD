package model.entities

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import model.EntityBody
import model.attack._
import model.behaviour.RichPredicates._
import model.behaviour.{EnemyBehaviours, EnemyBehavioursImpl, NotPredicate, RandomTruePredicate}
import model.collisions.ImplicitConversions._
import model.collisions.{DoNothingCollisionStrategy, EntityCollisionBit}
import model.entities.EntityType.EntityType
import model.entities.State._
import model.entities.Statistic.Statistic
import model.helpers.EntitiesFactoryImpl.{addEntity, createPolygonalShape, defineEntityBody, getEntitiesContainerMonitor}
import model.helpers.EntitiesUtilities.getEntitiesDistance
import model.movement.{ChaseTarget, DoNothingMovementStrategy, EnemyMovementStrategy, FaceTarget}
import utils.ApplicationConstants.RANDOM
import utils.CollisionConstants.ENEMY_COLLISIONS
import utils.EnemiesConstants._

trait LivingEntity extends MobileEntity {

  def healLife(heal: Float)

  def sufferDamage(damage: Float)

  def getLife: Float = if(this.getStatistic(Statistic.CurrentHealth).nonEmpty)
    this.getStatistic(Statistic.CurrentHealth).get else 0

  def setAttackStrategy(strategy: AttackStrategy)
}

object LivingEntity {
  private val heroEntity: () => Hero =
    () => getEntitiesContainerMonitor.getHero.getOrElse(throw new IllegalArgumentException())

  private val levelNumber: () => Int =
    () => getEntitiesContainerMonitor.getLevelNumber

  def createEnemyEntity(position: (Float, Float),
                                 size: (Float, Float),
                                 stats: Map[Statistic, Float],
                                 statsModifiers: Map[Statistic, Float],
                                 score: Int,
                                 entityId: EntityType): EnemyImpl = {

    val spawnPoint = (position._1, position._2+size._2)
    val levelBasedStats =
      stats.map {case (key, value) => (key, value + levelNumber() * statsModifiers.getOrElse(key, 0f))}

    val entityBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityCollisionBit.Enemy,
      ENEMY_COLLISIONS, createPolygonalShape(size.PPM, rounder = true), spawnPoint.PPM)

    val enemy:EnemyImpl = new EnemyImpl(entityId, entityBody, size.PPM, levelBasedStats, score, heroEntity())
    addEntity(enemy)
    enemy
  }

  def createSkeletonEnemy(position: (Float, Float)): EnemyImpl = {
    val enemy:EnemyImpl = createEnemyEntity(position, SKELETON_SIZE,
      SKELETON_STATS, STATS_MODIFIER, SKELETON_SCORE, EntityType.EnemySkeleton)

    val behaviours:EnemyBehaviours = new EnemyBehavioursImpl()
    behaviours.addBehaviour((DoNothingCollisionStrategy(),
      EnemyMovementStrategy(enemy, heroEntity()),
      new SkeletonAttack(enemy, heroEntity())))
    enemy.setBehaviour(behaviours)
    enemy
  }

  def createSlimeEnemy(position: (Float, Float)): EnemyImpl = {
    // easter egg: a slime could rarely be displayed as Pacman with a 5% chance
    val enemyType =  if (RANDOM.nextInt(100) <= 5) EntityType.EnemyPacman else EntityType.EnemySlime

    val enemy:EnemyImpl = createEnemyEntity(position,
      SLIME_SIZE, SLIME_STATS, STATS_MODIFIER, SLIME_SCORE, enemyType)

    val behaviours:EnemyBehaviours = new EnemyBehavioursImpl()
    behaviours.addBehaviour((DoNothingCollisionStrategy(),
      EnemyMovementStrategy(enemy, heroEntity()),
      new SlimeAttack(enemy, heroEntity())))
    enemy.setBehaviour(behaviours)
    enemy
  }

  def createWormEnemy(position: (Float, Float)): EnemyImpl = {
    val enemy:EnemyImpl = createEnemyEntity(position, WORM_SIZE,
      WORM_STATS, STATS_MODIFIER, WORM_SCORE, EntityType.EnemyWorm)

    val behaviours:EnemyBehaviours = new EnemyBehavioursImpl()
    behaviours.addBehaviour((DoNothingCollisionStrategy(),
      EnemyMovementStrategy(enemy,  heroEntity()),
      new WormFireballAttack(enemy, heroEntity())))
    enemy.setBehaviour(behaviours)
    enemy
  }

  def createWizardBossEnemy(position: (Float, Float)): EnemyImpl = {
    val enemy:EnemyImpl = createEnemyEntity(position, WIZARD_BOSS_SIZE, WIZARD_BOSS_STATS, STATS_MODIFIER,
      WIZARD_BOSS_SCORE, EntityType.EnemyBossWizard)
    val behaviours:EnemyBehaviours = new EnemyBehavioursImpl()

    // first behaviour - do nothing for some time
    val b1 = behaviours.addBehaviour((DoNothingCollisionStrategy(), DoNothingMovementStrategy(), DoNothingAttackStrategy()))

    // second behaviour - attack hero if near
    val p2AttackStrategy = new WizardFirstAttack(enemy, heroEntity())
    val b2 = behaviours.addBehaviour((DoNothingCollisionStrategy(), ChaseTarget(enemy, heroEntity()), p2AttackStrategy))

    // third behaviour - attack hero if near (with another attack)
    val p3AttackStrategy = new WizardSecondAttack(enemy, heroEntity())
    val b3 = behaviours.addBehaviour((DoNothingCollisionStrategy(), ChaseTarget(enemy, heroEntity()), p3AttackStrategy))

    // fourth behaviour - attack hero with ranged attacks
    val p4AttackStrategy = new WizardEnergyBallAttack(enemy, heroEntity())
    val b4 = behaviours.addBehaviour((DoNothingCollisionStrategy(), FaceTarget(enemy, heroEntity()), p4AttackStrategy))

    // add conditional transitions between behaviours
    behaviours.addTransition(b1, b2, () => getEntitiesDistance(enemy, heroEntity()) <= 100f.PPM)
    behaviours.addTransition(b1, b3, () => getEntitiesDistance(enemy, heroEntity()) <= 100f.PPM)

    behaviours.addTransition(b2, b3, RandomTruePredicate(0.5f))
    behaviours.addTransition(b2, b4, NotPredicate(() => getEntitiesDistance(enemy, heroEntity()) <= 100f.PPM))

    behaviours.addTransition(b3, b2, RandomTruePredicate(0.5f))
    behaviours.addTransition(b3, b4, NotPredicate(() => getEntitiesDistance(enemy, heroEntity()) <= 100f.PPM))

    behaviours.addTransition(b4, b2, () => getEntitiesDistance(enemy, heroEntity()) <= 100f.PPM)
    behaviours.addTransition(b4, b3, () => getEntitiesDistance(enemy, heroEntity()) <= 100f.PPM)

    enemy.setBehaviour(behaviours)
    enemy
  }

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
    if (this is Dying) {
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

  override def setAttackStrategy(strategy: AttackStrategy): Unit = this.attackStrategy = strategy

  override def alterStatistics(statistic: Statistic, alteration: Float): Unit = {
    super.alterStatistics(statistic, alteration)

    statistic match {
      case Statistic.Strength => this.attackStrategy.alterStrength(alteration)
      case _ =>
    }
  }
}

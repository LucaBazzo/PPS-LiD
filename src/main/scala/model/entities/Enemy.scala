package model.entities

import model.attack._
import model.behaviour.RichTransitions._
import model.behaviour._
import model.collisions.ImplicitConversions._
import model.collisions.{CollisionStrategy, DoNothingCollisionStrategy, EntityCollisionBit}
import model.entities.EntityType.EntityType
import model.entities.Items.Items
import model.entities.State._
import model.entities.Statistic.Statistic
import model.helpers.EntitiesFactoryImpl._
import model.helpers.ItemPools
import model.movement.{FaceTarget, MovementStrategy}
import model.{EntityBody, Score}
import utils.ApplicationConstants.RANDOM
import utils.EnemiesConstants
import utils.EnemiesConstants._
import utils.HeroConstants.SHORT_WAIT_TIME

trait Enemy {
  def setBehaviour(enemyBehaviour: EnemyBehavioursImpl): Unit
}

object SkeletonEnemy {
  def apply(position: (Float, Float)): EnemyImpl = {
    val enemy: EnemyImpl = createEnemyEntity(position, SKELETON_SIZE,
      SKELETON_STATS, STATS_MODIFIER, ENEMY_SCORE, EntityType.EnemySkeleton)

    val behaviours = new EnemyBehavioursImpl()
    behaviours.addBehaviour((DoNothingCollisionStrategy(),
      GroundEnemyMovementStrategy(enemy, getEntitiesContainerMonitor.getHero.get, SKELETON_VISION_DISTANCE),
      SkeletonAttack(enemy)))

    enemy.setBehaviour(behaviours)
    enemy
  }
}

object WormEnemy {
  def apply(position: (Float, Float)): EnemyImpl = {
    val enemy: EnemyImpl = createEnemyEntity(position, WORM_SIZE,
      WORM_STATS, STATS_MODIFIER, ENEMY_SCORE, EntityType.EnemyWorm)

    val behaviours = new EnemyBehavioursImpl()
    behaviours.addBehaviour((DoNothingCollisionStrategy(),
      GroundEnemyMovementStrategy(enemy, getEntitiesContainerMonitor.getHero.get, WORM_VISION_DISTANCE),
      WormFireballAttack(enemy)))

    enemy.setBehaviour(behaviours)
    enemy
  }
}

object SlimeEnemy {
  def apply(position: (Float, Float)): EnemyImpl = {
    // easter egg: a slime could rarely be displayed as Pacman with a 5% chance
    val enemyType = if (RANDOM.nextFloat() <= PACMAN_SPAWN_RATE) EntityType.EnemyPacman else EntityType.EnemySlime

    val enemy: EnemyImpl = createEnemyEntity(position,
      SLIME_SIZE, SLIME_STATS, STATS_MODIFIER, ENEMY_SCORE, enemyType)

    val behaviours = new EnemyBehavioursImpl()
    behaviours.addBehaviour((DoNothingCollisionStrategy(),
      GroundEnemyMovementStrategy(enemy, getEntitiesContainerMonitor.getHero.get, SLIME_VISION_DISTANCE),
      SlimeAttack(enemy)))

    enemy.setBehaviour(behaviours)
    enemy
  }
}

object BatEnemy {
  def apply(position: (Float, Float)): EnemyImpl = {
    val enemy: EnemyImpl = createEnemyEntity(position,
      BAT_SIZE, BAT_STATS, STATS_MODIFIER, ENEMY_SCORE, EntityType.EnemyBat,
      collisions = EntityCollisionBit.Sword | EntityCollisionBit.Arrow)

    val behaviours = new EnemyBehavioursImpl()
    behaviours.addBehaviour((DoNothingCollisionStrategy(),
      FlyingEnemyMovementStrategy(enemy, getEntitiesContainerMonitor.getHero.get, BAT_VISION_DISTANCE),
      BatAttack(enemy)))

    enemy.setBehaviour(behaviours)
    enemy
  }
}

object WizardEnemy {
  val ATTACK_SWITCH_PROBABILITY: Float = 0.5f
  val BOSS_BATTLE_ACTIVATION_DISTANCE: Float = 150

  def apply(position: (Float, Float)): EnemyImpl = {
    val enemy: EnemyImpl = createEnemyEntity(position, WIZARD_BOSS_SIZE, WIZARD_BOSS_STATS, STATS_MODIFIER,
      BOSS_SCORE, EntityType.EnemyBossWizard)
    val hero = getEntitiesContainerMonitor.getHero.get

    val behaviours = new EnemyBehavioursImpl()
    val b1: (CollisionStrategy, MovementStrategy, AttackStrategy) = behaviours.addBehaviour(
      (DoNothingCollisionStrategy(), FaceTarget(enemy, hero), DoNothingAttackStrategy()))
    val b2: (CollisionStrategy, MovementStrategy, AttackStrategy) = behaviours.addBehaviour(
      (DoNothingCollisionStrategy(), ChaseMovementStrategy(enemy, hero), DoNothingAttackStrategy()))
    val b3: (CollisionStrategy, MovementStrategy, AttackStrategy) = behaviours.addBehaviour(
      (DoNothingCollisionStrategy(), FaceTarget(enemy, hero), WizardFirstAttack(enemy)))
    val b4: (CollisionStrategy, MovementStrategy, AttackStrategy) = behaviours.addBehaviour(
      (DoNothingCollisionStrategy(), FaceTarget(enemy, hero), WizardSecondAttack(enemy)))
    val b5: (CollisionStrategy, MovementStrategy, AttackStrategy) = behaviours.addBehaviour(
      (DoNothingCollisionStrategy(), FaceTarget(enemy, hero), WizardEnergyBallAttack(enemy)))

    behaviours.addTransition(b1, b2, IsTargetNearby(enemy, hero, BOSS_BATTLE_ACTIVATION_DISTANCE.PPM))
    behaviours.addTransition(b2, b3, IsTargetNearby(enemy, hero, WIZARD_ATTACK1_SIZE._1.PPM))
    behaviours.addTransition(b2, b4, IsTargetNearby(enemy, hero, WIZARD_ATTACK2_SIZE._1.PPM))
    behaviours.addTransition(b2, b5, Not(IsTargetVisible(enemy, hero)) || Not(IsPathWalkable(enemy, hero)))
    behaviours.addTransition(b3, b2, Not(IsEntityAttacking(enemy)) &&
      Not(IsTargetNearby(enemy, hero, WIZARD_ATTACK1_SIZE._1.PPM)))
    behaviours.addTransition(b3, b4, Not(IsEntityAttacking(enemy)) &&
      RandomlyTrue(ATTACK_SWITCH_PROBABILITY))
    behaviours.addTransition(b4, b2, Not(IsEntityAttacking(enemy)) &&
      Not(IsTargetNearby(enemy, hero, WIZARD_ATTACK2_SIZE._1.PPM)))
    behaviours.addTransition(b4, b3, Not(IsEntityAttacking(enemy)) &&
      RandomlyTrue(ATTACK_SWITCH_PROBABILITY))
    behaviours.addTransition(b5, b2, IsTargetVisible(enemy, hero) &&
      IsPathWalkable(enemy, hero) && Not(IsEntityAttacking(enemy)))

    enemy.setBehaviour(behaviours)
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
      this.behaviours.get.update()

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
    } else if (damage > 0 && !List(State.Attack01, State.Attack02, State.Attack03).contains(this.getState)) {
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


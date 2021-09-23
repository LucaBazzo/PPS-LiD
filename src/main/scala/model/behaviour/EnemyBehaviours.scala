package model.behaviour

import model.attack._
import model.collisions.ImplicitConversions.RichFloat
import model.collisions.{CollisionStrategy, DoNothingCollisionStrategy}
import model.entities.{Hero, LivingEntity}
import model.helpers.EntitiesUtilities.getEntitiesDistance
import model.movement.{DoNothingMovementStrategy, FaceTarget, MovementStrategy}
import utils.EnemiesConstants.{WIZARD_BOSS_ATTACK1_SIZE, WIZARD_BOSS_ATTACK2_SIZE, WIZARD_BOSS_ATTACK3_DISTANCE}

trait EnemyBehaviours  {
  def getCollisionStrategy: CollisionStrategy
  def getMovementStrategy: MovementStrategy
  def getAttackStrategy: AttackStrategy
}

class EnemyBehavioursImpl extends BehavioursImpl with EnemyBehaviours  {

  override type Behaviour = (CollisionStrategy, MovementStrategy, AttackStrategy)

  override def getCollisionStrategy: CollisionStrategy = this.getCurrentBehaviour._1

  override def getMovementStrategy: MovementStrategy = this.getCurrentBehaviour._2

  override def getAttackStrategy: AttackStrategy = this.getCurrentBehaviour._3

  override def onBehaviourBegin(): Unit = { }

  override def onBehaviourEnd(): Unit = { }
}

case class WizardEnemyBehaviour(enemy:LivingEntity, hero:Hero) extends EnemyBehavioursImpl {

  val isHeroNear: Transition = () => getEntitiesDistance(enemy, hero) <= WIZARD_BOSS_ATTACK3_DISTANCE.PPM
  val ATTACK_SWITCH_PROBABILITY: Float = 0.5f

  val b1: (CollisionStrategy, MovementStrategy, AttackStrategy) =
    addBehaviour((DoNothingCollisionStrategy(), DoNothingMovementStrategy(), DoNothingAttackStrategy()))

  val b2: (CollisionStrategy, MovementStrategy, AttackStrategy) =
    addBehaviour((DoNothingCollisionStrategy(), BossMovementStrategy(enemy, hero, WIZARD_BOSS_ATTACK1_SIZE._1.PPM),
      WizardFirstAttack(enemy, hero)))

  val b3: (CollisionStrategy, MovementStrategy, AttackStrategy) =
    addBehaviour((DoNothingCollisionStrategy(), BossMovementStrategy(enemy, hero, WIZARD_BOSS_ATTACK2_SIZE._1.PPM),
      WizardSecondAttack(enemy, hero)))

  val b4: (CollisionStrategy, MovementStrategy, AttackStrategy) =
    addBehaviour((DoNothingCollisionStrategy(), FaceTarget(enemy, hero), WizardEnergyBallAttack(enemy, hero)))

  addTransition(b1, b2, isHeroNear)
  addTransition(b1, b3, isHeroNear)

  addTransition(b2, b3, RandomlyTrue(ATTACK_SWITCH_PROBABILITY))
  addTransition(b2, b4, Not(isHeroNear))

  addTransition(b3, b2, RandomlyTrue(ATTACK_SWITCH_PROBABILITY))
  addTransition(b3, b4, Not(isHeroNear))

  addTransition(b4, b2, isHeroNear)
  addTransition(b4, b3, isHeroNear)

}

case class WormEnemyBehaviour(enemy:LivingEntity, hero:Hero) extends EnemyBehavioursImpl {
  addBehaviour((
    DoNothingCollisionStrategy(), GroundEnemyMovementStrategy(enemy, hero), WormFireballAttack(enemy, hero)))
}

case class SlimeEnemyBehaviour(enemy:LivingEntity, hero:Hero) extends EnemyBehavioursImpl {
  addBehaviour((
    DoNothingCollisionStrategy(), GroundEnemyMovementStrategy(enemy, hero), SlimeAttack(enemy, hero)))
}

case class SkeletonEnemyBehaviour(enemy:LivingEntity, hero:Hero) extends EnemyBehavioursImpl {
  addBehaviour((
    DoNothingCollisionStrategy(), GroundEnemyMovementStrategy(enemy, hero), SkeletonAttack(enemy, hero)))
}
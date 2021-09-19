package model.behaviour

import model.attack._
import model.collisions.ImplicitConversions.RichFloat
import model.collisions.{CollisionStrategy, DoNothingCollisionStrategy}
import model.entities.{Hero, LivingEntity}
import model.helpers.EntitiesUtilities.getEntitiesDistance
import model.movement.{DoNothingMovementStrategy, FaceTarget, MovementStrategy}
import utils.EnemiesConstants.{WIZARD_BOSS_ATTACK1_SIZE, WIZARD_BOSS_ATTACK2_SIZE, WIZARD_BOSS_ATTACK3_DISTANCE}
import RichPredicates._

trait EnemyBehaviours extends BehavioursImpl {
  override type Behaviour = (CollisionStrategy, MovementStrategy, AttackStrategy)

  def getCollisionStrategy: CollisionStrategy
  def getMovementStrategy: MovementStrategy
  def getAttackStrategy: AttackStrategy
}

class EnemyBehavioursImpl extends EnemyBehaviours {
  override def getCollisionStrategy: CollisionStrategy = this.getCurrentBehaviour._1

  override def getMovementStrategy: MovementStrategy = this.getCurrentBehaviour._2

  override def getAttackStrategy: AttackStrategy = this.getCurrentBehaviour._3

  override def onBehaviourBegin(): Unit = { }

  override def onBehaviourEnd(): Unit = { }
}

case class WizardEnemyBehaviour(enemy:LivingEntity, hero:Hero) extends EnemyBehavioursImpl {

  val isHeroNear: () => Boolean = () => getEntitiesDistance(enemy, hero) <= WIZARD_BOSS_ATTACK3_DISTANCE.PPM
  val ATTACK_SWITCH_PROBABILITY: Float = 0.5f

  // first behaviour - do nothing for some time
  val b1: (CollisionStrategy, MovementStrategy, AttackStrategy) =
    addBehaviour((DoNothingCollisionStrategy(), DoNothingMovementStrategy(), DoNothingAttackStrategy()))

  // second behaviour - attack hero if near
  val b2: (CollisionStrategy, MovementStrategy, AttackStrategy) =
    addBehaviour((DoNothingCollisionStrategy(), BossMovementStrategy(enemy, hero, WIZARD_BOSS_ATTACK1_SIZE._1.PPM),
      new WizardFirstAttack(enemy, hero)))

  // third behaviour - attack hero if near (with another attack)
  val b3: (CollisionStrategy, MovementStrategy, AttackStrategy) =
    addBehaviour((DoNothingCollisionStrategy(), BossMovementStrategy(enemy, hero, WIZARD_BOSS_ATTACK2_SIZE._1.PPM),
      new WizardSecondAttack(enemy, hero)))

  // fourth behaviour - attack hero with ranged attacks
  val b4: (CollisionStrategy, MovementStrategy, AttackStrategy) =
    addBehaviour((DoNothingCollisionStrategy(), FaceTarget(enemy, hero), new WizardEnergyBallAttack(enemy, hero)))

  // add conditional transitions between behaviours
  addTransition(b1, b2, isHeroNear)
  addTransition(b1, b3, isHeroNear)

  addTransition(b2, b3, RandomTruePredicate(ATTACK_SWITCH_PROBABILITY))
  addTransition(b2, b4, NotPredicate(isHeroNear))

  addTransition(b3, b2, RandomTruePredicate(ATTACK_SWITCH_PROBABILITY))
  addTransition(b3, b4, NotPredicate(isHeroNear))

  addTransition(b4, b2, isHeroNear)
  addTransition(b4, b3, isHeroNear)

}

case class WormEnemyBehaviour(enemy:LivingEntity, hero:Hero) extends EnemyBehavioursImpl {
  addBehaviour((
    DoNothingCollisionStrategy(),
    GroundEnemyMovementStrategy(enemy, hero),
    new WormFireballAttack(enemy, hero)))
}

case class SlimeEnemyBehaviour(enemy:LivingEntity, hero:Hero) extends EnemyBehavioursImpl {
  addBehaviour((
    DoNothingCollisionStrategy(),
    GroundEnemyMovementStrategy(enemy, hero),
    new SlimeAttack(enemy, hero)))
}

case class SkeletonEnemyBehaviour(enemy:LivingEntity, hero:Hero) extends EnemyBehavioursImpl {
  addBehaviour((
    DoNothingCollisionStrategy(),
    GroundEnemyMovementStrategy(enemy, hero),
    new SkeletonAttack(enemy, hero)))
}
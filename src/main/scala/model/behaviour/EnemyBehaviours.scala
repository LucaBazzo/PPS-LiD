package model.behaviour

import model.attack._
import model.behaviour.RichPredicates._
import model.collisions.ImplicitConversions.RichFloat
import model.collisions.{CollisionStrategy, DoNothingCollisionStrategy}
import model.entities.{Hero, LivingEntity}
import model.helpers.EntitiesUtilities.getEntitiesDistance
import model.movement.{DoNothingMovementStrategy, FaceTarget, MovementStrategy}

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
  // first behaviour - do nothing for some time
  val b1: (CollisionStrategy, MovementStrategy, AttackStrategy) =
    addBehaviour((DoNothingCollisionStrategy(), DoNothingMovementStrategy(), DoNothingAttackStrategy()))

  // second behaviour - attack hero if near
  val b2: (CollisionStrategy, MovementStrategy, AttackStrategy) =
    addBehaviour((DoNothingCollisionStrategy(), ChaseTarget(enemy, hero), new WizardFirstAttack(enemy, hero)))

  // third behaviour - attack hero if near (with another attack)
  val b3: (CollisionStrategy, MovementStrategy, AttackStrategy) =
    addBehaviour((DoNothingCollisionStrategy(), ChaseTarget(enemy, hero), new WizardSecondAttack(enemy, hero)))

  // fourth behaviour - attack hero with ranged attacks
  val b4: (CollisionStrategy, MovementStrategy, AttackStrategy) =
    addBehaviour((DoNothingCollisionStrategy(), FaceTarget(enemy, hero), new WizardEnergyBallAttack(enemy, hero)))

  // add conditional transitions between behaviours
  addTransition(b1, b2, () => getEntitiesDistance(enemy, hero) <= 100f.PPM)
  addTransition(b1, b3, () => getEntitiesDistance(enemy, hero) <= 100f.PPM)

  addTransition(b2, b3, RandomTruePredicate(0.5f))
  addTransition(b2, b4, NotPredicate(() => getEntitiesDistance(enemy, hero) <= 100f.PPM))

  addTransition(b3, b2, RandomTruePredicate(0.5f))
  addTransition(b3, b4, NotPredicate(() => getEntitiesDistance(enemy, hero) <= 100f.PPM))

  addTransition(b4, b2, () => getEntitiesDistance(enemy, hero) <= 100f.PPM)
  addTransition(b4, b3, () => getEntitiesDistance(enemy, hero) <= 100f.PPM)

}

case class WormEnemyBehaviour(enemy:LivingEntity, hero:Hero) extends EnemyBehavioursImpl {
  addBehaviour((
    DoNothingCollisionStrategy(),
    EnemyMovementStrategy(enemy, hero),
    new WormFireballAttack(enemy, hero)))
}

case class SlimeEnemyBehaviour(enemy:LivingEntity, hero:Hero) extends EnemyBehavioursImpl {
  addBehaviour((
    DoNothingCollisionStrategy(),
    EnemyMovementStrategy(enemy, hero),
    new SlimeAttack(enemy, hero)))
}

case class SkeletonEnemyBehaviour(enemy:LivingEntity, hero:Hero) extends EnemyBehavioursImpl {
  addBehaviour((
    DoNothingCollisionStrategy(),
    EnemyMovementStrategy(enemy, hero),
    new SkeletonAttack(enemy, hero)))
}
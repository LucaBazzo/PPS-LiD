import controller.GameEvent._
import controller.ModelResources
import model.LevelImpl
import model.entity.State._
import model.entity._
import model.helpers.{EntitiesFactoryImpl, ItemPoolImpl}
import org.scalatest.flatspec.AnyFlatSpec
import utils.HeroConstants._

class TestHeroAttacks extends AnyFlatSpec{

  private var hero: Hero = _

  private def initialize(): Unit = {
    val entitiesContainer: ModelResources = new ModelResources
    EntitiesFactoryImpl.setEntitiesContainerMonitor(entitiesContainer)
    new LevelImpl(null, entitiesContainer, new ItemPoolImpl())
    hero = entitiesContainer.getHero.get
  }

  "A hero" should "perform 3 types of normal sword attacks" in {
    initialize()

    hero.setState(Sliding)
    hero.notifyCommand(Attack)
    assert(hero is Sliding)

    hero.setState(Running)
    hero.notifyCommand(Attack)
    assert(hero is Attack01)

    val countFirstAttack: Int =
      (FIRST_SWORD_ATTACK_DURATION - WAIT_FOR_ANOTHER_CONSECUTIVE_ATTACK) / ATTACK_STRATEGY_TIMER_DECREMENT + 1

    for(_ <- 0 to countFirstAttack)
      hero.update()

    hero.notifyCommand(Attack)
    assert(hero is Attack02)

    val countSecondAttack: Int =
      (SECOND_SWORD_ATTACK_DURATION - WAIT_FOR_ANOTHER_CONSECUTIVE_ATTACK) / ATTACK_STRATEGY_TIMER_DECREMENT + 1

    for(_ <- 0 to countSecondAttack)
      hero.update()

    hero.notifyCommand(Attack)
    assert(hero is Attack03)

    val finishThirdAttack: Int = THIRD_SWORD_ATTACK_DURATION / ATTACK_STRATEGY_TIMER_DECREMENT + 1

    for(_ <- 0 to finishThirdAttack)
      hero.update()

    assert(hero is Standing)
  }

  "A hero" should "perform an air attack" in {
    initialize()

    hero.setState(Jumping)
    hero.notifyCommand(Attack)
    assert(hero is AirDownAttacking)

    hero.update()

    hero.getFeet.get.collisionDetected(EntitiesFactoryImpl.createImmobileEntity())
    hero.setVelocityY(0)
    hero.update()

    assert(hero.isTouchingGround)
    assert(hero is AirDownAttackingEnd)

    val finishAirAttack: Int = LONG_WAIT_TIME / WAIT_TIME_DECREMENT + 1

    for(_ <- 0 to finishAirAttack)
      hero.update()

    assert(hero is Standing)
  }

  "A hero" should "perform a bow attack if he has a bow" in {
    initialize()

    hero.notifyCommand(BowAttack)
    assert(hero is Standing)

    hero.itemPicked(Items.Bow)
    assert(hero.isItemPicked(Items.Bow))

    val finishPick: Int = LONG_WAIT_TIME / WAIT_TIME_DECREMENT + 1

    for(_ <- 0 to finishPick)
      hero.update()

    hero.notifyCommand(BowAttack)
    assert(hero is BowAttacking)

    val finishBowAttack: Int = BOW_ATTACK_DURATION / ATTACK_STRATEGY_TIMER_DECREMENT + 1

    for(_ <- 0 to finishBowAttack)
      hero.update()

    assert(hero is Standing)
  }

}


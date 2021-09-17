import controller.GameEvent._
import model.LevelImpl
import model.entities.State._
import model.entities.Statistic._
import model.entities._
import model.helpers.{EntitiesContainerMonitor, EntitiesFactoryImpl, ItemPoolImpl}
import org.scalatest.flatspec.AnyFlatSpec
import utils.HeroConstants._

class TestHero extends AnyFlatSpec{

  private var hero: Hero = _

  private def initialize(): Unit = {
    val entitiesContainer: EntitiesContainerMonitor = new EntitiesContainerMonitor
    EntitiesFactoryImpl.setEntitiesContainerMonitor(entitiesContainer)
    new LevelImpl(null, entitiesContainer, new ItemPoolImpl())
    hero = entitiesContainer.getHero.get
  }


  "A hero" should "change his behaviour based on the commands received" in {
    initialize()
    assertResult(State.Standing)(hero.getState)

    hero.notifyCommand(MoveRight)
    assertResult(State.Running)(hero.getState)

    hero.notifyCommand(Up)
    assertResult(State.Jumping)(hero.getState)

    hero.notifyCommand(MoveRight)
    assertResult(State.Jumping)(hero.getState)

    hero.notifyCommand(Up)
    assertResult(State.Somersault)(hero.getState)
  }

  "A hero" should "update himself based on the physics and state he is" in {
    initialize()
    assertResult(State.Standing)(hero.getState)

    hero.notifyCommand(MoveRight)
    hero.update()

    assertResult(State.Running)(hero.getState)

    hero.setVelocityY(-JUMP_VELOCITY)
    assertResult(State.Running)(hero.getState)
    hero.update()
    assertResult(State.Falling)(hero.getState)

    hero.stopMovement()
    hero.update()
    assertResult(State.Standing)(hero.getState)
  }

  "A hero" should "jumps two times until he touches the ground" in {
    initialize()
    hero.notifyCommand(Up)
    hero.update()
    assertResult(State.Jumping)(hero.getState)

    hero.setVelocityY(-JUMP_VELOCITY)
    hero.update()
    assertResult(State.Jumping)(hero.getState)

    //the hero touches the ground
    hero.setVelocityY(0)
    val platform: Entity = EntitiesFactoryImpl.createImmobileEntity()
    hero.getFeet.get.collisionDetected(platform)

    hero.update()

    assert(hero.getFeet.get.isColliding)
    assert(hero.isTouchingGround)

    assertResult(State.Standing)(hero.getState)

    hero.notifyCommand(Up)
    hero.getFeet.get.collisionReleased(platform)
    hero.update()
    assertResult(State.Jumping)(hero.getState)

    //second jump
    hero.notifyCommand(Up)
    hero.update()
    assertResult(State.Somersault)(hero.getState)

    //third time with the command UP
    val velocityY: Float = hero.getVelocity._2
    hero.notifyCommand(Up)
    hero.update()
    assert(velocityY equals hero.getVelocity._2)

    //is falling
    hero.setVelocityY(-JUMP_VELOCITY)
    hero.update()
    assertResult(State.Falling)(hero.getState)

    //the hero touches the ground
    hero.setVelocityY(0)
    hero.getFeet.get.collisionDetected(platform)

    hero.update()

    assert(hero.getFeet.get.isColliding)
    assert(hero.isTouchingGround)
  }

  "A hero" should "have statistics that can be altered" in {
    initialize()
    var statistics: Map[Statistic, Float] = hero.getStatistics

    assert(hero.getStatistics.nonEmpty)

    hero.alterStatistics(Health, 100)
    hero.alterStatistics(Strength, -10)
    hero.alterStatistics(Strength, -50)
    hero.alterStatistics(Strength, 10)

    assertResult(false)(statistics equals hero.getStatistics)

    assertResult(statistics(Health) + 100)(hero.getStatistic(Health).get)
    assertResult(statistics(Strength) - 50)(hero.getStatistic(Strength).get)

    assertResult(Option.empty)(hero.getStatistic(VisionAngle))

    statistics = hero.getStatistics
    hero.alterStatistics(Statistic.VisionAngle, 100)
    assert(statistics equals hero.getStatistics)
  }

  "A hero" should "suffer damage when is not sliding" in {
    initialize()

    assert(hero.getStatistic(Health).nonEmpty)
    val health: Float = hero.getStatistic(Health).get

    hero.sufferDamage(health / 2)
    assertResult(health / 2)(hero.getStatistic(CurrentHealth).get)
    assert(hero is Hurt)

    hero.setState(LadderClimbing)
    hero.sufferDamage(health / 4)
    assertResult(health / 4)(hero.getStatistic(CurrentHealth).get)
    assert(hero is Hurt)

    hero.setState(Sliding)
    hero.sufferDamage(1000)
    assertResult(health / 4)(hero.getStatistic(CurrentHealth).get)
    assert(hero is Sliding)

    hero.setState(Standing)
    hero.sufferDamage(health)
    assertResult(0)(hero.getStatistic(CurrentHealth).get)
    assert(hero is Dying)
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


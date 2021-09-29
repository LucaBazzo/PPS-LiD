import controller.GameEvent._
import controller.ModelResources
import model.LevelImpl
import model.entity.State._
import model.entity.Statistic._
import model.entity._
import model.helpers.{EntitiesFactoryImpl, ItemPoolImpl}
import org.scalatest.flatspec.AnyFlatSpec
import utils.HeroConstants._

class TestHero extends AnyFlatSpec{

  private var hero: Hero = _

  private def initialize(): Unit = {
    val entitiesContainer: ModelResources = new ModelResources
    EntitiesFactoryImpl.setEntitiesContainerMonitor(entitiesContainer)
    new LevelImpl(null, entitiesContainer, new ItemPoolImpl())
    hero = entitiesContainer.getHero.get
  }


  "A hero" should "change his behaviour based on the commands received" in {
    initialize()
    assert(hero is Standing)

    hero.notifyCommand(MoveRight)
    assert(hero is Running)

    hero.notifyCommand(Up)
    assert(hero is Jumping)

    hero.notifyCommand(MoveRight)
    assert(hero is Jumping)

    hero.notifyCommand(Up)
    assert(hero is Somersault)
  }

  "A hero" should "update himself based on the physics and state he is" in {
    initialize()
    assert(hero is Standing)

    hero.notifyCommand(MoveRight)
    hero.update()

    assert(hero is Running)

    hero.setVelocityY(-JUMP_VELOCITY)
    assert(hero is Running)
    hero.update()
    assert(hero is Falling)

    hero.stopMovement()
    hero.update()
    assert(hero is Standing)
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

}


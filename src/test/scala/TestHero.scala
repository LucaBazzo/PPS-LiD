import controller.GameEvent._
import model.LevelImpl
import model.entities.Statistic._
import model.entities._
import model.helpers.{EntitiesContainerMonitor, EntitiesFactoryImpl, ItemPoolImpl}
import org.scalatest.flatspec.AnyFlatSpec
import utils.HeroConstants.JUMP_VELOCITY

class TestHero extends AnyFlatSpec{

  private var hero: Hero = _

  private def initialize(): Unit = {
    //TODO null temporaneo
    new LevelImpl(null, new EntitiesContainerMonitor, new ItemPoolImpl())
    hero = EntitiesFactoryImpl.createHeroEntity(Option.empty)
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
    hero.getFeet.get.collisionDetected(Option.apply(platform))

    hero.update()

    assert(hero.getFeet.get.isColliding)
    assert(hero.isTouchingGround)

    assertResult(State.Standing)(hero.getState)

    hero.notifyCommand(Up)
    hero.getFeet.get.collisionReleased(Option.apply(platform))
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
    hero.getFeet.get.collisionDetected(Option.apply(platform))

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


}


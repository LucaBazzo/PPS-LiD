import controller.GameEvent._
import model.LevelImpl
import model.entities.State._
import model.entities._
import model.helpers.{EntitiesContainerMonitor, EntitiesFactoryImpl, ItemPoolImpl}
import org.scalatest.flatspec.AnyFlatSpec
import utils.HeroConstants._

class TestHeroMovements extends AnyFlatSpec{

  private var hero: Hero = _

  private def initialize(): Unit = {
    val entitiesContainer: EntitiesContainerMonitor = new EntitiesContainerMonitor
    EntitiesFactoryImpl.setEntitiesContainerMonitor(entitiesContainer)
    new LevelImpl(null, entitiesContainer, new ItemPoolImpl())
    hero = entitiesContainer.getHero.get
  }

  "A hero" should "not move when is crouch" in {
    initialize()

    hero.notifyCommand(Down)
    hero.update()
    assert(hero is Crouching)

    hero.notifyCommand(MoveRight)
    hero.update()
    assert(hero is Crouching)

    hero.notifyCommand(MoveLeft)
    hero.update()
    assert(hero is Crouching)

    assertResult((0,0))(hero.getVelocity)

    hero.notifyCommand(DownReleased)
    hero.update()
    assert(hero is Standing)
  }


  "A hero" should "jumps two times until he touches the ground" in {
    initialize()
    hero.notifyCommand(Up)
    hero.update()
    assert(hero is Jumping)

    hero.setVelocityY(-JUMP_VELOCITY)
    hero.update()
    assert(hero is Jumping)

    //the hero touches the ground
    hero.setVelocityY(0)
    val ground: Entity = EntitiesFactoryImpl.createImmobileEntity()
    hero.getFeet.get.collisionDetected(ground)

    hero.update()

    assert(hero.getFeet.get.isColliding)
    assert(hero.isTouchingGround)

    assert(hero is Standing)

    hero.notifyCommand(Up)
    hero.getFeet.get.collisionReleased(ground)
    hero.update()
    assert(hero is Jumping)

    //second jump
    hero.notifyCommand(Up)
    hero.update()
    assert(hero is Somersault)

    //third time with the command UP
    val velocityY: Float = hero.getVelocity._2
    hero.notifyCommand(Up)
    hero.update()
    assert(velocityY equals hero.getVelocity._2)

    //is falling
    hero.setVelocityY(-JUMP_VELOCITY)
    hero.update()
    assert(hero is Falling)

    //the hero touches the ground
    hero.setVelocityY(0)
    hero.getFeet.get.collisionDetected(ground)

    hero.update()

    assert(hero.getFeet.get.isColliding)
    assert(hero.isTouchingGround)
  }

  "A hero" should "fall when the ground ends" in {
    initialize()

    val ground: Entity = EntitiesFactoryImpl.createImmobileEntity()

    hero.getFeet.get.collisionDetected(ground)
    assert(hero.isTouchingGround)

    hero.notifyCommand(MoveRight)
    hero.update()
    assert(hero is Running)
    assertResult((RUN_VELOCITY, 0))(hero.getVelocity)

    hero.getFeet.get.collisionReleased(ground)
    hero.setVelocityY(-JUMP_VELOCITY)
    assert(!hero.isTouchingGround)

    hero.update()

    assert(hero is Falling)
  }


}


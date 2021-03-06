import controller.GameEvent._
import controller.ModelResources
import model.entity._
import model.helpers.{EntitiesFactoryImpl, ItemPoolImpl}
import model.{HeroInteraction, LadderInteraction, LevelImpl}
import org.scalatest.flatspec.AnyFlatSpec

class TestHeroEnvironmentInteractions extends AnyFlatSpec{

  private var hero: Hero = _

  private val interactionCommand = Interaction
  private var heroInteraction: Option[HeroInteraction] = Option.empty

  private def initialize(): Unit = {
    val entitiesContainer: ModelResources = new ModelResources
    EntitiesFactoryImpl.setEntitiesContainerMonitor(entitiesContainer)
    new LevelImpl(null, entitiesContainer, new ItemPoolImpl())
    hero = entitiesContainer.getHero.get
    heroInteraction = Option.apply(HeroInteraction(interactionCommand, new LadderInteraction(hero)))
  }


  "A hero" should "change his interaction with the environment" in {
    initialize()

    hero.notifyCommand(Up)
    hero.update()
    assertResult(State.Jumping)(hero.getState)

    hero.notifyCommand(interactionCommand)
    hero.update()
    assertResult(State.Jumping)(hero.getState)

    //an environment interaction is available
    hero.setEnvironmentInteraction(heroInteraction)

    hero.notifyCommand(interactionCommand)
    hero.update()
    assertResult(State.LadderIdle)(hero.getState)
  }

  "A hero" should "move up and down when he interacts with a ladder" in {
    initialize()

    //an environment interaction is available
    hero.setEnvironmentInteraction(heroInteraction)

    //interact with the ladder
    hero.notifyCommand(interactionCommand)
    hero.update()
    assertResult(State.LadderIdle)(hero.getState)

    hero.notifyCommand(Up)
    hero.update()
    assertResult(State.LadderClimbing)(hero.getState)

    hero.notifyCommand(Down)
    hero.update()
    assertResult(State.LadderDescending)(hero.getState)

    //stop interaction
    hero.notifyCommand(interactionCommand)
    hero.update()
    assertResult(State.Falling)(hero.getState)
  }

  "A hero" should "add and remove an environment interaction" in {
    initialize()

    hero.setEnvironmentInteraction(heroInteraction)

    //interact with the ladder
    hero.notifyCommand(interactionCommand)
    hero.update()
    hero.notifyCommand(Up)
    hero.update()
    assertResult(State.LadderClimbing)(hero.getState)

    hero.setEnvironmentInteraction(Option.empty)
    assertResult(State.Falling)(hero.getState)
  }
}


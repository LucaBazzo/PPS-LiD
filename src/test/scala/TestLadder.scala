import controller.GameEvent
import model.entities.{Entity, EntityType, Hero, State}
import model.{Level, LevelImpl}
import model.helpers.{EntitiesContainerMonitor, EntitiesFactoryImpl}
import org.scalatest.flatspec.AnyFlatSpec

class TestLadder extends AnyFlatSpec {

  "The Hero" should "be able to interact with a ladder" in {
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    //TODO null temporaneo
    /*val level: Level = new LevelImpl(null, monitor)
    val ladder: Entity = monitor.getEntities(x => x.getType == EntityType.Ladder).get.head
    val hero: Hero = monitor.getEntities(x => x.getType == EntityType.Hero).get.head.asInstanceOf[Hero]
    ladder.collisionDetected(Option.apply(hero))
    hero.notifyCommand(GameEvent.Interaction)
    assert(hero.getState == State.LadderIdle)
    hero.notifyCommand(GameEvent.Interaction)
    assert(hero.getState == State.Falling)*/
  }

}

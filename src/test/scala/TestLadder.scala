import controller.GameEvent
import controller.GameEvent.GameEvent
import model.entities.{Entity, EntityType, State}
import model.{Level, LevelImpl}
import model.helpers.EntitiesContainerMonitor
import org.scalatest.flatspec.AnyFlatSpec

class TestLadder extends AnyFlatSpec {

  "The Hero" should "be able to interact with a ladder" in {
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    val level: Level = new LevelImpl(monitor)
    val ladder: Entity = monitor.getEntities(x => x.getType == EntityType.Ladder).get.head
    val hero: Entity = monitor.getEntities(x => x.getType == EntityType.Hero).get.head
    ladder.collisionDetected(hero)
    level.updateEntities(List[GameEvent](GameEvent.Interaction))
    assert(hero.getState == State.LadderIdle)
    level.updateEntities(List[GameEvent](GameEvent.Interaction))
    assert(hero.getState == State.Falling)
  }

}

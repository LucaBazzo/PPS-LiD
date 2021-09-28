import controller.{GameEvent, ModelResources}
import model.entity.{Entity, EntityType, Hero, Ladder, State}
import model.{Level, LevelImpl}
import model.helpers.{EntitiesFactoryImpl, ItemPoolImpl}
import org.scalatest.flatspec.AnyFlatSpec

class TestLadder extends AnyFlatSpec {

  private def initialize(): ModelResources = {
    val monitor: ModelResources = new ModelResources
    EntitiesFactoryImpl.setEntitiesContainerMonitor(monitor)
    //TODO null temporaneo
    val _: Level = new LevelImpl(null, monitor, new ItemPoolImpl())
    monitor
  }

  "The Hero" should "be able to interact with a ladder" in {
    val monitor: ModelResources = this.initialize()
    val ladder: Entity = Ladder((10, 10), (10, 100))
    val hero: Hero = monitor.getEntities(x => x.getType == EntityType.Hero).head.asInstanceOf[Hero]
    ladder.collisionDetected(hero)
    hero.notifyCommand(GameEvent.Interaction)
    assert(hero.getState == State.LadderIdle)
    hero.notifyCommand(GameEvent.Interaction)
    assert(hero.getState == State.Falling)
  }

}

import controller.GameEvent
import model.LevelImpl
import model.entities.{EntityType, Hero, ImmobileEntity, State}
import model.helpers.{EntitiesContainerMonitor, EntitiesFactoryImpl, ItemPoolImpl}
import org.scalatest.flatspec.AnyFlatSpec

class TestChest extends AnyFlatSpec {
  "A chest" should "open when the hero interact with it" in {
    val monitor: EntitiesContainerMonitor = this.initialize()
    val chest: ImmobileEntity = monitor.getEntities(x => x.isInstanceOf[ImmobileEntity] && x.getType == EntityType.Chest).get.head.asInstanceOf[ImmobileEntity]
    val hero: Hero = monitor.getEntities(x => x.isInstanceOf[Hero]).get.head.asInstanceOf[Hero]
    chest.collisionDetected(hero)
    hero.notifyCommand(GameEvent.Interaction)
    assert(monitor.getEntities(x => x.isInstanceOf[ImmobileEntity] && x.getType == EntityType.Chest && x.getState == State.Opening).get.nonEmpty)
  }

  private def initialize(): EntitiesContainerMonitor = {
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    //TODO null temporaneo
    new LevelImpl(null, monitor, new ItemPoolImpl())
    EntitiesFactoryImpl.createChest((10,10), (480,150))
    monitor
  }
}

import controller.{GameEvent, ModelResources}
import model.LevelImpl
import model.entity.collision.EntityCollisionBit
import model.entity._
import model.helpers.{EntitiesFactoryImpl, ItemPoolImpl}
import org.scalatest.flatspec.AnyFlatSpec

class TestDoor extends AnyFlatSpec {
  "A door" should "open when the hero interact with it" in {
    val monitor: ModelResources = this.initialize()
    val door: ImmobileEntity = monitor.getEntities(x => x.isInstanceOf[ImmobileEntity] && x.getEntityBody.getEntityCollisionBit() == EntityCollisionBit.Door).head.asInstanceOf[ImmobileEntity]
    val hero: Hero = monitor.getEntities(x => x.isInstanceOf[Hero]).head.asInstanceOf[Hero]
    door.collisionDetected(hero)
    hero.notifyCommand(GameEvent.Interaction)
    assert(monitor.getEntities(x => x.isInstanceOf[ImmobileEntity] && x.getState == State.Opening).nonEmpty)
  }


  private def initialize(): ModelResources = {
    val monitor: ModelResources = new ModelResources
    EntitiesFactoryImpl.setEntitiesContainerMonitor(monitor)
    //TODO null temporaneo
    new LevelImpl(null, monitor, new ItemPoolImpl())
    Door()
    monitor
  }
}

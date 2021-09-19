import controller.GameEvent
import model.LevelImpl
import model.collisions.EntityCollisionBit
import model.entities._
import model.helpers.{EntitiesContainerMonitor, EntitiesFactoryImpl, ItemPoolImpl}
import org.scalatest.flatspec.AnyFlatSpec

class TestDoor extends AnyFlatSpec {
  "A door" should "open when the hero interact with it" in {
    val monitor: EntitiesContainerMonitor = this.initialize()
    val door: ImmobileEntity = monitor.getEntities(x => x.isInstanceOf[ImmobileEntity] && x.getEntityBody.getEntityCollisionBit() == EntityCollisionBit.Door).get.head.asInstanceOf[ImmobileEntity]
    val hero: Hero = monitor.getEntities(x => x.isInstanceOf[Hero]).get.head.asInstanceOf[Hero]
    door.collisionDetected(hero)
    hero.notifyCommand(GameEvent.Interaction)
    assert(monitor.getEntities(x => x.isInstanceOf[ImmobileEntity] && x.getState == State.Opening).get.nonEmpty)
  }


  private def initialize(): EntitiesContainerMonitor = {
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    EntitiesFactoryImpl.setEntitiesContainerMonitor(monitor)
    //TODO null temporaneo
    new LevelImpl(null, monitor, new ItemPoolImpl())
    EntitiesFactoryImpl.createDoor()
    monitor
  }
}

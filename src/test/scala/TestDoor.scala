import controller.GameEvent
import model.collisions.EntityType
import model.{Level, LevelImpl}
import model.entities._
import model.helpers.EntitiesContainerMonitor
import org.scalatest.flatspec.AnyFlatSpec

class TestDoor extends AnyFlatSpec {
  "A door" should "open when the hero touches it" in {
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    val level: Level = new LevelImpl(monitor)
    val door: ImmobileEntity = level.getEntity(x => x.isInstanceOf[ImmobileEntity] && x.getEntityBody.getEntityType() == EntityType.Door).asInstanceOf[ImmobileEntity]
    val hero: Hero = monitor.getEntities(x => x.isInstanceOf[Hero]).get.head.asInstanceOf[Hero]
    level.updateEntities(List(GameEvent.SetMap))
    door.collisionDetected(hero)
    level.updateEntities(List.empty)
    assert(monitor.getEntities(x => x.isInstanceOf[ImmobileEntity] && x.getEntityBody.getEntityCollisions() == EntityType.OpenedDoor).get.nonEmpty)
  }
}

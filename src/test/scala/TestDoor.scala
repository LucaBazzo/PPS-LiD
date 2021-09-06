import controller.GameEvent
import model.collisions.EntityCollisionBit
import model.{Level, LevelImpl}
import model.entities._
import model.helpers.EntitiesContainerMonitor
import org.scalatest.flatspec.AnyFlatSpec

class TestDoor extends AnyFlatSpec {
  "A door" should "open when the hero touches it" in {
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    val level: Level = new LevelImpl(monitor)
    val door: ImmobileEntity = level.getEntity(x => x.isInstanceOf[ImmobileEntity] && x.getEntityBody.getEntityCollisionBit() == EntityCollisionBit.Door).asInstanceOf[ImmobileEntity]
    val hero: Hero = monitor.getEntities(x => x.isInstanceOf[Hero]).get.head.asInstanceOf[Hero]
    level.updateEntities(List(GameEvent.SetMap))
    door.collisionDetected(Option.apply(hero))
    level.updateEntities(List(GameEvent.Interaction))
    assert(monitor.getEntities(x => x.isInstanceOf[ImmobileEntity] && x.getEntityBody.getEntityCollisions() == EntityCollisionBit.OpenedDoor).get.nonEmpty)
  }
}

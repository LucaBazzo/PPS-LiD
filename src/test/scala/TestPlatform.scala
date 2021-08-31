import model.entities.{Entity, EntityType}
import model.{Level, LevelImpl}
import model.helpers.EntitiesContainerMonitor
import org.scalatest.flatspec.AnyFlatSpec

class TestPlatform extends AnyFlatSpec {

  "A platform" should "generate collisions" in {
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    val level: Level = new LevelImpl(monitor)
    val platform: Entity = monitor.getEntities(x => x.getType == EntityType.Platform).get.head
    val hero: Entity = monitor.getEntities(x => x.getType == EntityType.Hero).get.head
    val pBody = platform.getBody
    val ppBody = level.getEntity(x => x.getBody == pBody)
  }
}

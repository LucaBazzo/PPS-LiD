import controller.GameEvent
import model.collisions.EntityCollisionBit
import model.{Level, LevelImpl}
import model.entities._
import model.helpers.EntitiesContainerMonitor
import org.scalatest.flatspec.AnyFlatSpec

class TestChest extends AnyFlatSpec {
  /*"A chest" should "open when the hero interact with it" in {
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    val level: Level = new LevelImpl(monitor)
    val chest: ImmobileEntity = level.getEntity(x => x.isInstanceOf[ImmobileEntity] && x.getType == EntityType.Chest).asInstanceOf[ImmobileEntity]
    val hero: Hero = monitor.getEntities(x => x.isInstanceOf[Hero]).get.head.asInstanceOf[Hero]
    chest.collisionDetected(Option.apply(hero))
    hero.notifyCommand(GameEvent.Interaction)
    assert(monitor.getEntities(x => x.isInstanceOf[ImmobileEntity] && x.getType == EntityType.Chest && x.getState == State.Opening).get.nonEmpty)
  }*/
}

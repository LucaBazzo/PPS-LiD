import model.helpers.EntitiesContainerMonitor
import org.scalatest.flatspec.AnyFlatSpec

class TestDoor extends AnyFlatSpec {
  "A door" should "open when the hero touches it" in {
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    //TODO null temporaneo
/*    val level: Level = new LevelImpl(null, monitor)
    val door: ImmobileEntity = level.getEntity(x => x.isInstanceOf[ImmobileEntity] && x.getEntityBody.getEntityCollisionBit() == EntityCollisionBit.Door).asInstanceOf[ImmobileEntity]
    val hero: Hero = monitor.getEntities(x => x.isInstanceOf[Hero]).get.head.asInstanceOf[Hero]
    door.collisionDetected(Option.apply(hero))
    hero.notifyCommand(GameEvent.Interaction)
    assert(monitor.getEntities(x => x.isInstanceOf[ImmobileEntity] && x.getState == State.Opening).get.nonEmpty)*/
  }
}

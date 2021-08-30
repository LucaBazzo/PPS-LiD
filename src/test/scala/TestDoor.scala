package scala

import model.collisions.EntityType
import model.{Level, LevelImpl}
import model.entities._
import model.helpers.{EntitiesContainerMonitor}
import org.scalatest.flatspec.AnyFlatSpec

class TestDoor extends AnyFlatSpec {
  "A door" should "open when the hero touches it" in {
    val level: Level = new LevelImpl(new EntitiesContainerMonitor)
    val door: ImmobileEntity = level.getEntity(x => x.isInstanceOf[ImmobileEntity] && x.getEntityBody.getEntityType() == EntityType.Door).asInstanceOf[ImmobileEntity]
    val hero: Hero = level.getEntity(x => x.isInstanceOf[Hero]).asInstanceOf[Hero]
    door.collisionDetected(hero)
    level.updateEntities(List.empty)
    assert(level.getEntity(x => x.isInstanceOf[ImmobileEntity] && x.getEntityBody.getEntityCollisions() == EntityType.OpenedDoor) != null)
  }
}

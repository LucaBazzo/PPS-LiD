
import com.badlogic.gdx.physics.box2d.World
import controller.GameEvent
import controller.GameEvent.GameEvent
import model.{Level, LevelImpl}
import model.entities._
import model.helpers.{EntitiesContainerMonitor, EntitiesSetter}

import collection.mutable.Stack
import org.scalatest.flatspec.AnyFlatSpec

class ItemTest extends AnyFlatSpec {
  "A Hero" should "be able to move right" in {
    val level: Level = new LevelImpl(new EntitiesContainerMonitor)
    val hero: Entity = level.getEntity(x => x.isInstanceOf[HeroImpl])
    for(n <- List.range(0, 1000)) level.updateEntities(List[GameEvent]())
    //for(n <- List.range(0, 1)) level.updateEntities(List[GameEvent](GameEvent.MoveRight))
    val heroPos: (Float, Float) = hero.getPosition
    assert(heroPos._1 > 1.0)
  }
}
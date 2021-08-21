
import model.entities.{Entity, Hero}
import model.helpers.EntitiesContainerMonitor
import model.{Level, LevelImpl}
import org.junit.Assert.assertEquals
import org.junit.Test

import scala.collection.mutable.Stack

class TestJUnitTest {
  @Test
  def stackPopTest(): Unit = {
    val stack = new Stack[Int]
    stack.push(1)
    stack.push(2)
    assertEquals(stack.pop(), 2)
    assertEquals(stack.pop(), 1)
  }

  @Test
  def newLevelTest(): Unit = {
    val entitiesContainer: EntitiesContainerMonitor = new EntitiesContainerMonitor()
    assertEquals(List.empty, entitiesContainer.getEntities(_ => true).get)

    val level: Level = new LevelImpl(entitiesContainer)

    assertEquals(1, entitiesContainer.getEntities((x: Entity) => x.isInstanceOf[Hero]).get.count(_ => true))

    val hero: Entity = entitiesContainer.getEntities((x: Entity) => x.isInstanceOf[Hero]).get.head
    level.removeEntity(hero)
    assertEquals(0, entitiesContainer.getEntities((x: Entity) => x.isInstanceOf[Hero]).get.count(_ => true))

  }
}

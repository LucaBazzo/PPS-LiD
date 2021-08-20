package scala

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
}

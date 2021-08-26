package scala

import org.junit.{Ignore, Test}
import org.junit.Assert.assertEquals

class FailingTest {

  @Ignore
  @Test
  def failingTest(): Unit = {
    assertEquals(1,2)
  }

}

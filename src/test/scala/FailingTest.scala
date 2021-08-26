package scala

import org.junit.Test
import org.junit.Assert.assertEquals

class FailingTest {

  @Test
  def failingTest(): Unit = {
    assertEquals(1,2)
  }

}

package scala

import collection.mutable.Stack
import org.scalatest.flatspec.AnyFlatSpec

class TestScalaTest extends AnyFlatSpec {
  "A Stack" should "pop values in last-in-first-out order" in {
    val stack = new Stack[Int]
    stack.push(1)
    stack.push(2)
    assert(stack.pop() === 2)
    assert(stack.pop() === 1)
  }
}
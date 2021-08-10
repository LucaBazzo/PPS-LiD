package controller

import scala.collection.mutable.Queue

object ScreenQueue {

  val queue: Queue[Int] = Queue.empty

  def setScreen(screen: Int) = synchronized {
    queue.addOne(screen)
  }

  def getScreen(): Int = synchronized {
    queue.dequeue()
  }

}

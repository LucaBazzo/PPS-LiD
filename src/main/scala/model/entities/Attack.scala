package model.entities

import com.badlogic.gdx.physics.box2d.Body
import model.Level

class TimedAttack(private var body: Body, private val size: (Float, Float), private val duration:Long, private val level:Level) extends MobileEntityImpl(body, size) {

  private val startTime:Long = System.currentTimeMillis()

  override def update(): Unit = {
    if (System.currentTimeMillis() - startTime > duration) {
      level.removeEntity(this)
    } else {
      super.update()
    }
  }

  override def collisionDetected(entity: Entity): Unit = {
//    level.removeEntity(this)
  }
}

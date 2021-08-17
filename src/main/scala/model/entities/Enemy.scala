package model.entities

import com.badlogic.gdx.physics.box2d.Body

trait Enemy {

  def getType()
}

trait Score {
  def getScore: Int
}

case class EnemyImpl(private var body: Body, private val size: (Float, Float), private val score: Int)
                  extends LivingEntityImpl(body, size) with Enemy with Score {

  override def getType(): Unit = {}

  override def getScore: Int = this.score
}
package model

import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef
import com.badlogic.gdx.physics.box2d.{Body, Joint, World}

trait AttackPattern {

  def attack()
  def stopAttack()
  def destroyAttack()
}

class AttackPatternImpl(private val world: World,
                        private val pivotBody: Body,
                        private val rotatingBody: Body,
                        private val angularVelocity: Float) extends AttackPattern{

  private val joint: Joint = this.revoluteJoint(this.pivotBody, this.rotatingBody)

  override def attack(): Unit = this.rotatingBody.setAngularVelocity(angularVelocity)

  override def stopAttack(): Unit = this.rotatingBody.setAngularVelocity(0)

  override def destroyAttack(): Unit = {
    this.world.destroyJoint(this.joint)
    this.world.destroyBody(this.pivotBody)
    this.world.destroyBody(this.rotatingBody)
  }

  private def revoluteJoint(pivotBody: Body, rotatingBody: Body): Joint = {
    val rjd: RevoluteJointDef = new RevoluteJointDef()

    rjd.initialize(pivotBody, rotatingBody, pivotBody.getWorldCenter)
    /*rjd.motorSpeed = 3.14f * 2    //how fast
    rjd.maxMotorTorque = 1000.0f  //how powerful
    rjd.enableMotor = false*/

    world.createJoint(rjd)
  }


}

package view.screens.sprites

import com.badlogic.gdx.graphics.g2d.{Animation, Sprite, TextureRegion}
import model.entities.State.State

trait EntitySprite extends Sprite {

  def addAnimation(state: State, animation: Animation[TextureRegion], loop: Boolean = false)

  override def setPosition(x: Float, y: Float): Unit = {
    super.setPosition(x - this.getWidth / 2, y - this.getHeight / 2 + 8f)
  }

  def update(dt: Float, state: State, previousState: State, velocityX: Float)

  def getIntWidth: Int = super.getWidth.asInstanceOf[Int]

  def getIntHeight: Int = super.getHeight.asInstanceOf[Int]
}

class EntitySpriteImpl extends EntitySprite {

  private var animations: Map[State, Animation[TextureRegion]] = Map()
  private var loops: Map[State, Boolean] = Map()

  private var stateTimer: Float = 0
  private var isFacingRight: Boolean = true

  override def addAnimation(state: State, animation: Animation[TextureRegion], loop: Boolean = false): Unit = {
    this.animations += (state -> animation)
    this.loops += (state -> loop)
  }

  override def update(dt: Float, state: State, previousState: State, velocityX: Float): Unit = {
    var region: TextureRegion = getFrame(state)
    region = checkFlip(region, velocityX)
    this.setRegion(region)
    if(state == previousState)
      stateTimer += dt
    else
      stateTimer = 0
    //println(state)
  }

  private def getFrame(state: State): TextureRegion = animations(state).getKeyFrame(stateTimer, loops(state))

  private def checkFlip(region: TextureRegion, velocityX: Float): TextureRegion = {
    //facing to the right and running to the left
    if((velocityX < 0 || !isFacingRight) && !region.isFlipX) {
      region.flip(true, false)
      this.isFacingRight = false
    }
    else if((velocityX > 0 || isFacingRight) && region.isFlipX) {
      region.flip(true, false)
      this.isFacingRight = true
    }
    region
  }

}

package view.screens.sprites

import com.badlogic.gdx.graphics.g2d.{Animation, Sprite, TextureRegion}
import model.entities.State.State

trait EntitySprite extends Sprite {

  def addAnimation(state: State, animation: Animation[TextureRegion], loop: Boolean = false)

  def setPosition(x: Float, y: Float, isLittle: Boolean): Unit = {
    if(isLittle)
      super.setPosition(x - this.getWidth / 2, y - this.getHeight / 2 + 9.3f)
    else
      super.setPosition(x - this.getWidth / 2, y - this.getHeight / 2 + 4.3f)
  }

  def update(dt: Float, state: State, previousState: State, isFacingRight: Boolean)

  def getIntWidth: Int = super.getWidth.asInstanceOf[Int]

  def getIntHeight: Int = super.getHeight.asInstanceOf[Int]
}

class EntitySpriteImpl extends EntitySprite {

  private var animations: Map[State, Animation[TextureRegion]] = Map()
  private var loops: Map[State, Boolean] = Map()

  private var stateTimer: Float = 0

  override def addAnimation(state: State, animation: Animation[TextureRegion], loop: Boolean = false): Unit = {
    this.animations += (state -> animation)
    this.loops += (state -> loop)
  }

  override def update(dt: Float, state: State, previousState: State, isFacingRight: Boolean): Unit = {
    var region: TextureRegion = getFrame(state)
    region = checkFlip(region, isFacingRight)
    this.setRegion(region)
    if(state == previousState)
      stateTimer += dt
    else
      stateTimer = 0
    //println(state)
  }

  private def getFrame(state: State): TextureRegion = animations(state).getKeyFrame(stateTimer, loops(state))

  private def checkFlip(region: TextureRegion, isFacingRight: Boolean): TextureRegion = {
    //facing to the left and region not flipped
    if(!isFacingRight && !region.isFlipX) {
      region.flip(true, false)
    }
    else if(isFacingRight && region.isFlipX) {
      region.flip(true, false)
    }
    region
  }

}

package view.screens.sprites

import com.badlogic.gdx.graphics.g2d.{Animation, Batch, Sprite, TextureRegion}
import model.collisions.ImplicitConversions.RichFloat
import model.entities.State.State
import model.entities.{Entity, Hero, MobileEntity, State}

trait EntitySprite extends Sprite {

  def addAnimation(state: State, animation: Animation[TextureRegion], loop: Boolean = false)

  def update(dt: Float, entity: Entity)

  def setPosition(entity: Entity)

  def getIntWidth: Int = super.getWidth.asInstanceOf[Int]

  def getIntHeight: Int = super.getHeight.asInstanceOf[Int]
}

class EntitySpriteImpl(regionName: String, width: Float, height: Float) extends EntitySprite {

  private var animations: Map[State, Animation[TextureRegion]] = Map()
  private var loops: Map[State, Boolean] = Map()

  private var stateTimer: Float = 0
  private var previousState: State = State.Standing

  /*private val atlas: TextureAtlas = new TextureAtlas(SPRITES_PACK_LOCATION)
  this.setRegion(this.atlas.findRegion(regionName))
  this.setBounds(0, 0, width, height)*/

  override def addAnimation(state: State, animation: Animation[TextureRegion], loop: Boolean = false): Unit = {
    this.animations += (state -> animation)
    this.loops += (state -> loop)
  }

  override def update(dt: Float, entity: Entity): Unit = {
    val state: State = entity.getState
    var region: TextureRegion = getFrame(state)
    entity match {
      case me: MobileEntity => region = checkFlip(region, me.isFacingRight)
      case _ =>
    }
    this.setRegion(region)
    if(state == this.previousState)
      stateTimer += dt
    else
      stateTimer = 0
    this.previousState = state

    this.setPosition(entity)
    //println(state)
  }

  override def setPosition(entity: Entity): Unit = {
    super.setPosition(entity.getPosition._1 - this.getWidth / 2, entity.getPosition._2 - this.getHeight / 2)
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

  override def draw(batch: Batch): Unit = {
    this.setSize(this.width.PPM, this.height.PPM)
    super.draw(batch)
  }

}

class HeroEntitySprite(regionName: String, width: Float, height: Float) extends EntitySpriteImpl(regionName, width, height) {

  override def setPosition(entity: Entity): Unit = {
    if(entity.asInstanceOf[Hero].isLittle)
      super.setPosition(entity.getPosition._1 - this.getWidth / 2,
        entity.getPosition._2 - this.getHeight / 2 + 9.3f.PPM)
    else
      super.setPosition(entity.getPosition._1 - this.getWidth / 2,
        entity.getPosition._2 - this.getHeight / 2 + 4f.PPM)
  }
}

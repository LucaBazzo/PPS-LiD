package view.screens.game

import com.badlogic.gdx.graphics.g2d._
import com.badlogic.gdx.graphics.{GL20, OrthographicCamera}
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.viewport.{FitViewport, Viewport}
import com.badlogic.gdx.{Gdx, Input, ScreenAdapter}
import controller.ObserverManager
import model._
import model.entities.State.{Falling, Jumping, Running, Standing, State}
import model.entities.{Entity, Hero}
import utils.ApplicationConstants._
import view.screens.helpers.TileMapHelper

class GameScreen(private val entitiesGetter: EntitiesGetter,
                 private val observerManager: ObserverManager) extends ScreenAdapter{

  private val camera: OrthographicCamera = new OrthographicCamera()
  private val batch: SpriteBatch = new SpriteBatch()

  private val box2DDebugRenderer: Box2DDebugRenderer = new Box2DDebugRenderer()

  private val viewPort: Viewport = new FitViewport(WIDTH_SCREEN / 10 , HEIGHT_SCREEN / 10, camera)

  private val orthogonalTiledMapRenderer: OrthogonalTiledMapRenderer = TileMapHelper.getMap("assets/maps/map0.tmx")

  private val hud: Hud = new Hud(WIDTH_SCREEN, HEIGHT_SCREEN, batch)

  this.camera.setToOrtho(false, Gdx.graphics.getWidth / 2, Gdx.graphics.getHeight / 2)


  private val heroSprite: HeroSprite = new HeroSprite()

  private def update(deltaTime: Float): Unit = {
    this.handleInput(deltaTime)

    //old world step

    //it will render only what the camera can see
    this.orthogonalTiledMapRenderer.setView(camera)
  }

  private def handleInput(deltaTime : Float): Unit = {
    if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE))
      Gdx.app.exit()

    if (Gdx.input.isKeyJustPressed(Input.Keys.UP))
      this.observerManager.notifyEvent(0)

    if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
      this.observerManager.notifyEvent(1)

    if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
      this.observerManager.notifyEvent(2)
  }

  override def render(delta: Float): Unit = {
    this.update(delta)

    //clears the screen
    Gdx.gl.glClearColor(0,0,0,1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    super.render(delta)

    val entities: Option[List[Entity]] = entitiesGetter.getEntities((x: Entity) => x.isInstanceOf[Hero])
    if(entities.nonEmpty) {
      val player: Entity = entities.get.head
      this.camera.position.x = player.getPosition._1
      this.camera.position.y = player.getPosition._2

      this.heroSprite.update(delta, player.getState(), player.asInstanceOf[Hero].getPreviousState(),
        player.getPosition, player.getSize, player.asInstanceOf[Hero].getLinearVelocityX())
      player.asInstanceOf[Hero].updatePreviousState(player.getState())
    }

    this.camera.update()

    // render the map
    orthogonalTiledMapRenderer.render()

    //what will be shown by the camera
    batch.setProjectionMatrix(hud.getStage().getCamera.combined)
    hud.getStage().draw()
    //batch.setProjectionMatrix(camera.combined)

    batch.begin()
    // render objects inside
    this.heroSprite.draw(batch)

    batch.end()

    //for debug purpose
    box2DDebugRenderer.render(this.entitiesGetter.getWorld, camera.combined)
  }

  override def resize(width: Int, height: Int): Unit = {
    viewPort.update(width, height)
  }

  override def dispose(): Unit = {
    orthogonalTiledMapRenderer.dispose()
    this.entitiesGetter.getWorld.dispose()
    box2DDebugRenderer.dispose()
    hud.dispose()
  }
}


class HeroSprite extends Sprite {

  //SPRITES
  val atlas: TextureAtlas = new TextureAtlas("assets/sprites/Hero.pack")

  this.setRegion(this.atlas.findRegion("hero"))

  //private val heroStand: TextureRegion = new TextureRegion(getTexture, 0, 0, 50, 37)
  this.setBounds(0, 0, 50, 37)
  //setRegion(heroStand)


  private var heroStand: Animation[TextureRegion] = _
  private var heroRun: Animation[TextureRegion] = _
  private var heroJump: Animation[TextureRegion] = _
  private var heroFall: Animation[TextureRegion] = _

  private var stateTimer: Float = 0
  private var runningRight: Boolean = true

  //array from gdx.utils.Array
  private val frames: Array[TextureRegion] = new Array[TextureRegion]()

  //STANDING
  for(i <- 0 to 3) frames.add(new TextureRegion(getTexture, i * 50, 0 , 50, 37))
  this.heroStand = new Animation(0.18f, frames)
  frames.clear()

  //RUNNING
  for(i <- 1 to 6) frames.add(new TextureRegion(getTexture, i * 50, 37 , 50, 37))
  this.heroRun = new Animation(0.10f, frames)
  frames.clear()

  //JUMPING
  for(i <- 0 to 3) frames.add(new TextureRegion(getTexture, i * 50, 37 * 2 , 50, 37))
  this.heroJump = new Animation(0.10f, frames)
  frames.clear()

  //FALLING
  for(i <- 0 to 1) frames.add(new TextureRegion(getTexture, i * 50, 37 * 3 , 50, 37))
  this.heroFall = new Animation(0.10f, frames)
  frames.clear()

  def update(dt: Float, state: State, previousState: State,
             position: (Float, Float), size: (Float, Float), velocityX: Float): Unit = {
    this.setPosition(position._1 - size._1 , position._2 - size._2)
    var region: TextureRegion = getFrame(state)
    region = checkFlip(region, velocityX)
    this.setRegion(region)
    if(state == previousState)
      stateTimer += dt
    else
      stateTimer = 0
    println(state)
  }

  private def getFrame(state: State): TextureRegion = state match {
    case Jumping => heroJump.getKeyFrame(stateTimer)
    case Running => heroRun.getKeyFrame(stateTimer, true)
    case Falling => heroFall.getKeyFrame(stateTimer)
    case Standing => heroStand.getKeyFrame(stateTimer, true)
  }

  private def checkFlip(region: TextureRegion, velocityX: Float): TextureRegion = {
    //facing to the right and running to the left
    if((velocityX < 0 || !runningRight) && !region.isFlipX) {
      region.flip(true, false)
      this.runningRight = false
    }
    else if((velocityX > 0 || runningRight) && region.isFlipX) {
      region.flip(true, false)
      this.runningRight = true
    }
    region
  }
}

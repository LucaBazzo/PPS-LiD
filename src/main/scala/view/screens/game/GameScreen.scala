package view.screens.game

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.{GL20, OrthographicCamera}
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.{Vector2, Vector3}
import com.badlogic.gdx.physics.box2d.{Box2DDebugRenderer, World}
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.{FitViewport, Viewport}
import com.badlogic.gdx.{Gdx, Input, ScreenAdapter}
import utils.GameConstants._
import view.entities.Hero
import view.screens.helpers.{TileMapHelper, WorldCreator}
import view.screens.menu.{ObservableScreen, ScreenObserver}

class GameScreen() extends ScreenAdapter with ObservableScreen with TheGame{

  private var screenObserver: ScreenObserver = _

  private val camera: OrthographicCamera = new OrthographicCamera()
  private val batch: SpriteBatch = new SpriteBatch()
  private val world: World = new World(new Vector2(0, -10), true)
  private val box2DDebugRenderer: Box2DDebugRenderer = new Box2DDebugRenderer()

  private val viewPort: Viewport = new FitViewport(WIDTH_SCREEN / PIXEL_PER_METER, HEIGHT_SCREEN / PIXEL_PER_METER, camera)

  private val orthogonalTiledMapRenderer: OrthogonalTiledMapRenderer = TileMapHelper.getMap("assets/maps/map0.tmx")

  private val hud: Hud = new Hud(WIDTH_SCREEN, HEIGHT_SCREEN, batch)

  this.camera.setToOrtho(false, WIDTH_SCREEN, HEIGHT_SCREEN)
  this.camera.position.set(new Vector3(viewPort.getWorldWidth / 2, viewPort.getWorldHeight / 2,0))

  private val player: Hero = new Hero(this.world)

  private var currentScore: Int = 0

  new WorldCreator(this.world)

  private def update(deltaTime: Float): Unit = {
    this.handleInput(deltaTime)

    this.world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS)
    this.cameraUpdate()

    //it will render only what the camera can see
    this.orthogonalTiledMapRenderer.setView(camera)
  }

  private def handleInput(deltaTime : Float): Unit ={
    if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE))
      Gdx.app.exit()

    if (Gdx.input.isKeyJustPressed(Input.Keys.UP))
      player.jump()

    if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
      player.moveRight()

    if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
      player.moveLeft()
  }

  private def cameraUpdate(): Unit = {
    this.camera.position.x = player.getBody.getPosition.x
    this.camera.position.y = player.getBody.getPosition.y
    this.camera.update()
  }

  override def render(delta: Float): Unit = {
    this.update(delta)

    //clears the screen
    Gdx.gl.glClearColor(0,0,0,1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    // render the map
    orthogonalTiledMapRenderer.render()

    //what will be shown by the camera
    batch.setProjectionMatrix(hud.getStage().getCamera.combined)
    hud.getStage().draw()
    //batch.setProjectionMatrix(camera.combined)

    batch.begin()
    // render objects inside

    batch.end()

    //for debug purpose
    box2DDebugRenderer.render(world, camera.combined.scl(PIXEL_PER_METER))
  }

  override def resize(width: Int, height: Int): Unit = {
    viewPort.update(width, height)
  }

  override def dispose(): Unit = {
    orthogonalTiledMapRenderer.dispose()
    world.dispose()
    box2DDebugRenderer.dispose()
    hud.dispose()
  }

  override def getStage(): Stage = ???

  override def setObserver(screenObserver: ScreenObserver): Unit = this.screenObserver = screenObserver

  override def sendScore(currentScore: Int): Unit = this.currentScore = currentScore
}

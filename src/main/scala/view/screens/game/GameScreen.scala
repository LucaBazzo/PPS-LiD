package view.screens.game

import com.badlogic.gdx.graphics.g2d._
import com.badlogic.gdx.graphics.{GL20, OrthographicCamera}
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.utils.viewport.{FitViewport, Viewport}
import com.badlogic.gdx.{Gdx, Input, ScreenAdapter}
import controller.{GameEvent, ObserverManager}
import model.entities.{Entity, Hero, State}
import model.helpers.EntitiesGetter
import utils.ApplicationConstants._
import view.screens.helpers.TileMapHelper
import view.screens.sprites.{EntitySprite, SpriteFactory, SpriteFactoryImpl}

class GameScreen(private val entitiesGetter: EntitiesGetter,
                 private val observerManager: ObserverManager) extends ScreenAdapter{

  private val camera: OrthographicCamera = new OrthographicCamera()
  private val batch: SpriteBatch = new SpriteBatch()

  private val box2DDebugRenderer: Box2DDebugRenderer = new Box2DDebugRenderer()

  private val viewPort: Viewport = new FitViewport(WIDTH_SCREEN / 10 , HEIGHT_SCREEN / 10, camera)

  private val orthogonalTiledMapRenderer: OrthogonalTiledMapRenderer = TileMapHelper.getMap("assets/maps/map0.tmx")

  private val hud: Hud = new Hud(WIDTH_SCREEN, HEIGHT_SCREEN, batch)

  this.camera.setToOrtho(false, Gdx.graphics.getWidth / 2, Gdx.graphics.getHeight / 2)

  private val spriteFactory: SpriteFactory = new SpriteFactoryImpl()
  private val heroSprite: EntitySprite = spriteFactory.createEntitySprite("hero", 50, 37)
  this.heroSprite.addAnimation(State.Standing,
    spriteFactory.createSpriteAnimation(heroSprite, 0, 0, 3, 0.18f),
    true)
  this.heroSprite.addAnimation(State.Running,
    spriteFactory.createSpriteAnimation(heroSprite, 1, 1, 6),
    true)
  this.heroSprite.addAnimation(State.Jumping,
    spriteFactory.createSpriteAnimation(heroSprite, 2, 0, 3))
  this.heroSprite.addAnimation(State.Falling,
    spriteFactory.createSpriteAnimation(heroSprite, 3, 1, 2),
    true)

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
      this.observerManager.notifyEvent(GameEvent.MoveUp)

    if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
      this.observerManager.notifyEvent(GameEvent.MoveRight)

    if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
      this.observerManager.notifyEvent(GameEvent.MoveLeft)
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

      this.heroSprite.setPosition(WIDTH_SCREEN / 2, HEIGHT_SCREEN / 2)
      this.heroSprite.update(delta, player.getState(), player.asInstanceOf[Hero].getPreviousState(),
        player.asInstanceOf[Hero].getLinearVelocityX())
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

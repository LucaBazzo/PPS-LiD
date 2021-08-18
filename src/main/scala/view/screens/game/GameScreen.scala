package view.screens.game

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.g2d._
import com.badlogic.gdx.graphics.{GL20, OrthographicCamera}
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.utils.viewport.{FitViewport, Viewport}
import com.badlogic.gdx.{Gdx, ScreenAdapter}
import controller.{GameEvent, ObserverManager}
import model.entities.{Entity, Hero, State}
import model.helpers.EntitiesGetter
import utils.ApplicationConstants._
import view.inputs.GameInputProcessor
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
    loop = true)
  this.heroSprite.addAnimation(State.Running,
    spriteFactory.createSpriteAnimation(heroSprite, 1, 1, 6),
    loop = true)
  this.heroSprite.addAnimation(State.Jumping,
    spriteFactory.createSpriteAnimation(heroSprite, 2, 0, 3))
  this.heroSprite.addAnimation(State.Falling,
    spriteFactory.createSpriteAnimation(heroSprite, 3, 1, 2),
    loop = true)
  this.heroSprite.addAnimation(State.Sliding,
    spriteFactory.createSpriteAnimation(heroSprite, 3, 3, 6))
  this.heroSprite.addAnimation(State.Crouch,
    spriteFactory.createSpriteAnimation(heroSprite, 0, 4, 6, 0.18f),
    loop = true)
  this.heroSprite.addAnimation(State.Attack01,
    spriteFactory.createSpriteAnimation(heroSprite, 6, 0, 6))
  this.heroSprite.addAnimation(State.Attack02,
    spriteFactory.createSpriteAnimation(heroSprite, 7, 0, 3, 0.20f))
  //TODO aggiungere le animazioni della riga sotto
  this.heroSprite.addAnimation(State.Attack03,
    spriteFactory.createSpriteAnimation(heroSprite, 7, 4, 6, 0.20f))

  Gdx.input.setInputProcessor(new GameInputProcessor(this.observerManager))

  private def update(deltaTime: Float): Unit = {
    this.handleHoldingInput()

    //old world step

    //it will render only what the camera can see
    this.orthogonalTiledMapRenderer.setView(camera)
  }

  private def handleHoldingInput(): Unit = {

    if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT))
      this.observerManager.notifyEvent(GameEvent.MoveRight)

    if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT))
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
      val player: Hero = entities.get.head.asInstanceOf[Hero]
      this.camera.position.x = player.getPosition._1
      this.camera.position.y = player.getPosition._2

      this.heroSprite.setPosition(WIDTH_SCREEN / 2, HEIGHT_SCREEN / 2, player.isLittle)
      this.heroSprite.update(delta, player.getState, player.isFacingRight)
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

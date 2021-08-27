package view.screens.game

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.g2d._
import com.badlogic.gdx.graphics.{GL20, OrthographicCamera}
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.utils.viewport.{FitViewport, Viewport}
import com.badlogic.gdx.{Gdx, ScreenAdapter}
import controller.{GameEvent, ObserverManager}
import model.collisions.ImplicitConversions.RichInt
import model.entities.{Entity, Hero, Statistic}
import model.helpers.EntitiesGetter
import utils.ApplicationConstants._
import view.inputs.GameInputProcessor
import view.screens.helpers.TileMapHelper
import view.screens.sprites.{SpriteViewer, SpriteViewerImpl}

class GameScreen(private val entitiesGetter: EntitiesGetter,
                 private val observerManager: ObserverManager) extends ScreenAdapter{

  private val camera: OrthographicCamera = new OrthographicCamera()
  private val batch: SpriteBatch = new SpriteBatch()

  private val box2DDebugRenderer: Box2DDebugRenderer = new Box2DDebugRenderer()

  private val viewPort: Viewport = new FitViewport(WIDTH_SCREEN.PPM, HEIGHT_SCREEN.PPM, camera)

  private val orthogonalTiledMapRenderer: OrthogonalTiledMapRenderer = TileMapHelper.getMap("assets/maps/map0.tmx")

  private val hud: Hud = new Hud(WIDTH_SCREEN, HEIGHT_SCREEN, batch)

  //this.camera.setToOrtho(false, Gdx.graphics.getWidth / 2, Gdx.graphics.getHeight / 2)

  private val spriteViewer: SpriteViewer = new SpriteViewerImpl(this.batch)

  Gdx.input.setInputProcessor(new GameInputProcessor(this.observerManager))

  private def update(deltaTime: Float): Unit = {
    this.handleHoldingInput()

    //TODO add the changeHealth method when the get of the hero is done correctly
    this.hud.setCurrentScore(this.entitiesGetter.getScore)

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

    val heroEntity: Option[List[Entity]] = entitiesGetter.getEntities((x: Entity) => x.isInstanceOf[Hero])
    if(heroEntity.nonEmpty) {
      val hero: Hero = heroEntity.get.head.asInstanceOf[Hero]
      this.camera.position.x = hero.getPosition._1
      this.camera.position.y = hero.getPosition._2

      this.hud.changeHealth(hero.getStatistics(Statistic.CurrentHealth), hero.getStatistics(Statistic.Health))
    }

    val entities: Option[List[Entity]] = entitiesGetter.getEntities(_ => true)
    if(entities.nonEmpty) {
      this.spriteViewer.loadSprites(entities.get)
      this.spriteViewer.updateSprites(delta)
    }

    this.camera.update()

    // render the map
    orthogonalTiledMapRenderer.render()

    //what will be shown by the camera

    batch.setProjectionMatrix(camera.combined)



    batch.begin()
    // render objects inside
    this.spriteViewer.drawSprites()
    this.hud.drawHealthBar(batch)

    batch.end()

    //for debug purpose
    box2DDebugRenderer.render(this.entitiesGetter.getWorld, camera.combined)

    batch.setProjectionMatrix(hud.getStage.getCamera.combined)
    hud.getStage.draw()
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

package view.screens.menu

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.g2d._
import com.badlogic.gdx.graphics.{GL20, OrthographicCamera}
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.physics.box2d.{Box2DDebugRenderer, World}
import com.badlogic.gdx.utils.viewport.{FitViewport, Viewport}
import com.badlogic.gdx.{Gdx, ScreenAdapter}
import controller.{GameEvent, ObserverManager}
import model.collisions.ImplicitConversions.{RichFloat, RichInt}
import model.entities.Items.Items
import model.entities._
import model.helpers.{EntitiesGetter, EntitiesUtilities}
import utils.ApplicationConstants._
import view.inputs.GameInputProcessor
import model.world.TileMapManager
import view.screens.sprites.{SpriteViewer, SpriteViewerImpl}

class GameScreen(private val entitiesGetter: EntitiesGetter,
                 private val observerManager: ObserverManager,
                 private val tileMapManager: TileMapManager) extends ScreenAdapter{

  private val camera: OrthographicCamera = new OrthographicCamera()
  camera.translate(300f, 300f)
  private val batch: SpriteBatch = new SpriteBatch()

  private val box2DDebugRenderer: Box2DDebugRenderer = new Box2DDebugRenderer()

  private val viewPort: Viewport = new FitViewport(WIDTH_SCREEN.PPM, HEIGHT_SCREEN.PPM, camera)

  private val orthogonalTiledMapRenderer: OrthogonalTiledMapRenderer = tileMapManager.getMapRenderer(null)

  private val hud: Hud = new Hud(WIDTH_SCREEN, HEIGHT_SCREEN, batch)

  this.camera.setToOrtho(false, Gdx.graphics.getWidth / 2, Gdx.graphics.getHeight / 2)

  private val spriteViewer: SpriteViewer = new SpriteViewerImpl(this.batch)

  Gdx.input.setInputProcessor(new GameInputProcessor(this.observerManager))

  private var removeLoadingScreen: Boolean = true

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
    if(this.entitiesGetter.isLevelReady) {
      if(this.removeLoadingScreen) {
        this.hud.loadingFinished()
        this.removeLoadingScreen = false
      }

      this.update(delta)

      //clears the screen
      Gdx.gl.glClearColor(0,0,0,1)
      Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
      super.render(delta)

      this.hud.setLevelNumber(this.entitiesGetter.getLevelNumber)

      if(entitiesGetter.getHero.nonEmpty) {
        val hero: Hero = entitiesGetter.getHero.get
        this.camera.position.x = hero.getPosition._1
        this.camera.position.y = hero.getPosition._2
        this.hud.changeHealth(hero.getStatistics(Statistic.CurrentHealth), hero.getStatistics(Statistic.Health))
        val itemPicked: Option[Items] = entitiesGetter.hasHeroPickedUpItem
        if(itemPicked.nonEmpty)
          this.hud.addNewItem(itemPicked.get)
      }

      val message: Option[String] = entitiesGetter.getMessage
      if(message.nonEmpty)
        this.hud.setItemText(message.get)

      val bossEntity: Option[List[Entity]] = entitiesGetter.getEntities(e => e.getType match {
        case EntityType.EnemyBossWizard => true
        case _ => false
      })
      // TODO: prevenire chiamate di show e hide quando la barra della vita è già visibile o invisibile
      if (entitiesGetter.getBoss.nonEmpty &&
        EntitiesUtilities.getEntitiesDistance(entitiesGetter.getHero.get,
          entitiesGetter.getBoss.get) <= HEALTH_BAR_BOSS_VISIBILITY_DISTANCE.PPM) {
        hud.showBossHealthBar()
        val boss: LivingEntity = bossEntity.get.head.asInstanceOf[LivingEntity]
        this.hud.changeBossHealth(boss.getStatistics(Statistic.CurrentHealth), boss.getStatistics(Statistic.Health))
      } else {
        hud.hideBossHealthBar()
      }

      val entities: Option[List[Entity]] = entitiesGetter.getEntities(_ => true)
      if(entities.nonEmpty) {
        this.spriteViewer.loadSprites(entities.get)
        this.spriteViewer.updateSprites(delta)
      }

      this.camera.update()

      this.tileMapManager.renderWorld(orthogonalTiledMapRenderer)

      //what will be shown by the camera
      batch.setProjectionMatrix(camera.combined)

      batch.begin()
      // render objects inside
      this.spriteViewer.drawSprites()
      this.hud.drawHealthBar(batch)
      batch.end()

      //for debug purpose
      val world: Option[World] = this.entitiesGetter.getWorld
      if(world.nonEmpty) {
        box2DDebugRenderer.render(world.get, camera.combined)
      }

      batch.setProjectionMatrix(hud.getStage.getCamera.combined)
      hud.getStage.draw()
    }
    else {
      hud.getStage.draw()

      if(!this.removeLoadingScreen)
        this.removeLoadingScreen = true
    }
  }

  override def resize(width: Int, height: Int): Unit = {
    viewPort.update(width, height)
  }

  override def dispose(): Unit = {
    orthogonalTiledMapRenderer.dispose()
    box2DDebugRenderer.dispose()
    hud.dispose()
    this.observerManager.notifyEvent(GameEvent.CloseApplication)
  }
}

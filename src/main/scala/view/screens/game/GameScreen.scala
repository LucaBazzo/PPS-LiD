package view.screens.game

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.g2d._
import com.badlogic.gdx.graphics.{GL20, OrthographicCamera}
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.utils.viewport.{FitViewport, Viewport}
import com.badlogic.gdx.{Gdx, ScreenAdapter}
import controller.{GameEvent, ObserverManager}
import model.collisions.EntityType
import model.collisions.ImplicitConversions.RichInt
import model.entities.{Entity, Hero, Item, MobileEntity, State, Statistic}
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

  private val viewPort: Viewport = new FitViewport(WIDTH_SCREEN.PPM, HEIGHT_SCREEN.PPM, camera)

  private val orthogonalTiledMapRenderer: OrthogonalTiledMapRenderer = TileMapHelper.getMap("assets/maps/map0.tmx")

  private val hud: Hud = new Hud(WIDTH_SCREEN, HEIGHT_SCREEN, batch)

  //this.camera.setToOrtho(false, Gdx.graphics.getWidth / 2, Gdx.graphics.getHeight / 2)

  private val spriteFactory: SpriteFactory = new SpriteFactoryImpl()
  private val itemSprite: EntitySprite = spriteFactory.createEntitySprite("items", 32,
    32, 10, 10, 2)
  this.itemSprite.addAnimation(State.Standing,
    spriteFactory.createSpriteAnimation(itemSprite, 0, 0, 0, 0.20f))

  private val heroSprite: EntitySprite = spriteFactory.createHeroSprite("hero", 50, 37)
  this.defineHeroSpriteAnimations()

  private val arrowSprite: EntitySprite = spriteFactory.createEntitySprite("arrow", 40,
    5, 10, 1, 2)
  this.arrowSprite.addAnimation(State.Standing,
    spriteFactory.createSpriteAnimation(arrowSprite, 0, 0, 0))


  Gdx.input.setInputProcessor(new GameInputProcessor(this.observerManager))

  private def defineHeroSpriteAnimations(): Unit = {
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
      spriteFactory.createSpriteAnimationFromTwoRows(heroSprite, 0, 4, 6,
        1,0,0,0.18f),
      loop = true)
    this.heroSprite.addAnimation(State.Attack01,
      spriteFactory.createSpriteAnimation(heroSprite, 6, 0, 6))
    this.heroSprite.addAnimation(State.Attack02,
      spriteFactory.createSpriteAnimation(heroSprite, 7, 0, 3, 0.20f))
    this.heroSprite.addAnimation(State.Attack03,
      spriteFactory.createSpriteAnimationFromTwoRows(heroSprite, 7, 4, 6,
        8, 0, 2))
    this.heroSprite.addAnimation(State.Somersault,
      spriteFactory.createSpriteAnimationFromTwoRows(heroSprite, 2, 4, 6,
        3, 0, 0), loop = true)
    this.heroSprite.addAnimation(State.BowAttack,
      spriteFactory.createSpriteAnimationFromTwoRows(heroSprite, 16, 0, 6,
        17, 0, 1))

  }

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

    val entities: Option[List[Entity]] = entitiesGetter.getEntities((x: Entity) => x.isInstanceOf[Hero])
    if(entities.nonEmpty) {
      val hero: Hero = entities.get.head.asInstanceOf[Hero]
      this.camera.position.x = hero.getPosition._1
      this.camera.position.y = hero.getPosition._2

      this.heroSprite.update(delta, hero)

      this.hud.changeHealth(hero.getStatistics(Statistic.CurrentHealth), hero.getStatistics(Statistic.Health))
    }

    val items: Option[List[Entity]] = entitiesGetter.getEntities((x: Entity) => x.isInstanceOf[Item])
    if(items.nonEmpty) {
      val item: Item = items.get.head.asInstanceOf[Item]
      this.itemSprite.update(delta, item)
    }

    val mobileEntities: Option[List[Entity]] = entitiesGetter.getEntities((x: Entity) => x.isInstanceOf[MobileEntity])
    if(mobileEntities.nonEmpty){
      if(mobileEntities.get.asInstanceOf[List[MobileEntity]].exists((x: MobileEntity) => x.getEntityType == EntityType.Arrow)){
        val arrow: MobileEntity = mobileEntities.get.asInstanceOf[List[MobileEntity]].filter((x: MobileEntity) => x.getEntityType == EntityType.Arrow).head
        this.arrowSprite.update(delta, arrow)
      }


    }


    this.camera.update()

    // render the map
    orthogonalTiledMapRenderer.render()

    //what will be shown by the camera

    batch.setProjectionMatrix(camera.combined)


    batch.begin()
    // render objects inside
    this.heroSprite.draw(batch)
    this.itemSprite.draw(batch)
    this.arrowSprite.draw(batch)
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

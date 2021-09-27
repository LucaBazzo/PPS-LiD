package view.screens.menu

import com.badlogic.gdx.graphics._
import com.badlogic.gdx.graphics.g2d.{BitmapFont, TextureRegion}
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.{Table, TextButton}
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.{FitViewport, Viewport}
import com.badlogic.gdx.{Gdx, Screen}
import controller.{GameEvent, ObserverManager}
import utils.ApplicationConstants._

/** Represents an interactive screen with buttons and images
 */
trait MenuScreen extends Screen {

  /** Add a button to the container of the screen
   *
   *  @param button the button that will be added
   */
  def addButton(button: TextButton): Unit

  /** Return the container that contains the graphics components
   *
   *  @return the stage
   */
  def getStage: Stage
}


/** Abstract class containing the components for the menu creation.
 *
 *  @param observerManager observer for the messages from View to Controller
 *  @param backgroundImagePath the path to the background image
 *  @param distanceFromTop an empty space between the top and the buttons
 *  @param distanceButtonDefault the distance between buttons
 */
abstract class MenuScreenImpl(private val observerManager: ObserverManager,
                              private val backgroundImagePath: String,
                              private val distanceFromTop: Float = DEFAULT_DISTANCE_FROM_TOP,
                              private val distanceButtonDefault: Float = DISTANCE_BUTTONS_DEFAULT) extends MenuScreen {

  // Setting the view
  private val camera: Camera = new OrthographicCamera()
  private val viewport: Viewport = new FitViewport(WIDTH_SCREEN, HEIGHT_SCREEN, camera)
  private val stage: Stage = new Stage(this.viewport)

  private val backgroundTexture: Texture  = GUIFactory.createTexture(backgroundImagePath)
  private val bitmapFont: BitmapFont = GUIFactory.createBitmapFont(FONT_PATH_LABEL)
  protected val mainTable: Table  = new Table()

  this.viewport.apply()
  this.defineMenuContent()

  // add table to stage
  this.stage.addActor(mainTable)

  // enables mouse click on the stage (the screen container)
  Gdx.input.setInputProcessor(stage)

  private def defineMenuContent(): Unit ={
    // Set table to fill stage
    mainTable.setFillParent(true)
    // Set alignment of contents in the table.
    mainTable.top()
    // Set distance between buttons
    mainTable.defaults().pad(distanceButtonDefault)

    // Set background
    mainTable.background(new TextureRegionDrawable(new TextureRegion(backgroundTexture)))

    // putting empty spacing on the table
    mainTable.add(GUIFactory.createLabel("", bitmapFont, Color.WHITE)).size(distanceFromTop)
    mainTable.row()
  }

  override def getStage: Stage = this.stage

  override def render(delta: Float): Unit = {
    Gdx.gl.glClearColor(0, 0, 0, 1) // black color
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    this.stage.act()
    this.stage.draw()
  }

  override def resize(width: Int, height: Int): Unit = this.viewport.update(width, height)

  override def dispose(): Unit = observerManager.notifyEvent(GameEvent.CloseApplication)

  //the methods below are required for Screen implementation, not useful for these types of menus
  override def show(): Unit = {}

  override def pause(): Unit = {}

  override def resume(): Unit = {}

  override def hide(): Unit = {}
}

/** Implementation for the Main Menu of the application.
 *
 *  @param observerManager observer for the messages from View to Controller
 *  @param backgroundImagePath the path to the background image
 *  @param distanceFromTop an empty space between the top and the buttons
 *  @param distanceButtonDefault the distance between buttons
 */
case class MainMenuScreen(private val observerManager: ObserverManager,
                          private val backgroundImagePath: String,
                          private val distanceFromTop: Float = DEFAULT_DISTANCE_FROM_TOP,
                          private val distanceButtonDefault: Float = DISTANCE_BUTTONS_DEFAULT)
  extends MenuScreenImpl(observerManager, backgroundImagePath, distanceFromTop, distanceButtonDefault) {

  override def addButton(button: TextButton): Unit = {
    this.mainTable.row()
    this.mainTable.add(button)
  }
}

/** Implementation for the Game Over Menu of the application.
 *
 *  @param observerManager observer for the messages from View to Controller
 *  @param backgroundImagePath the path to the background image
 *  @param distanceFromTop an empty space between the top and the buttons
 *  @param distanceButtonDefault the distance between buttons
 */
case class GameOverScreen(private val observerManager: ObserverManager,
                          private val backgroundImagePath: String,
                          private val distanceFromTop: Float = DEFAULT_DISTANCE_FROM_TOP,
                          private val distanceButtonDefault: Float = DISTANCE_BUTTONS_DEFAULT)
  extends MenuScreenImpl(observerManager, backgroundImagePath, distanceFromTop, distanceButtonDefault) {

  override def addButton(button: TextButton): Unit = this.mainTable.add(button)
}


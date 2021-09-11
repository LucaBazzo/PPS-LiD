package view.screens.menu

import com.badlogic.gdx.graphics._
import com.badlogic.gdx.graphics.g2d.{BitmapFont, TextureRegion}
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle
import com.badlogic.gdx.scenes.scene2d.ui.{Skin, Table, TextButton}
import com.badlogic.gdx.scenes.scene2d.utils.{ClickListener, TextureRegionDrawable}
import com.badlogic.gdx.scenes.scene2d.{InputEvent, Stage}
import com.badlogic.gdx.utils.viewport.{FitViewport, Viewport}
import com.badlogic.gdx.{Gdx, Screen}
import controller.{GameEvent, ObserverManager}
import utils.ApplicationConstants._
import view.screens.game.GUIFactory


/**
 * Class containing the components for the main menu creation.
 */
class GameOverScreen(private val observerManager: ObserverManager) extends Screen {

  // Setting the view
  val camera: Camera = new OrthographicCamera()
  val viewport: Viewport = new FitViewport(WIDTH_SCREEN, HEIGHT_SCREEN, camera)
  this.viewport.apply()
  val stage: Stage = new Stage(this.viewport)

  var buttons: List[TextButton] = List.empty

  // Create Table
  val mainTable: Table  = new Table()
  // Set table to fill stage
  mainTable.setFillParent(true)
  // Set alignment of contents in the table.
  mainTable.top()
  // Set distance between buttons
  mainTable.defaults().pad(DISTANCE_BUTTONS_DEFAULT + 20)

  // Set background
  val backgroundTexture: Texture  = GUIFactory.createTexture("assets/backgrounds/background_game_over.png")
  mainTable.background(new TextureRegionDrawable(new TextureRegion(backgroundTexture)))

  // creating font
  val bitmapFont: BitmapFont = GUIFactory.createBitmapFont(FONT_PATH_LABEL)

  // putting empty spacing on the table
  mainTable.add(GUIFactory.createLabel("", bitmapFont, Color.WHITE)).size(DEFAULT_DISTANCE_FROM_TOP - 20)
  mainTable.row()

  // the button's textures
  val skin: Skin = GUIFactory.createSkin("assets/buttons/buttons.pack")

  // creating buttons
  val exitStyle: TextButtonStyle =
    GUIFactory.createTextButtonStyle("exit_button_inactive", "exit_button_active", skin, bitmapFont)
  buttons = this.addButtonListener(GUIFactory.createTextButton("No", exitStyle)) :: buttons

  val playStyle: TextButtonStyle =
    GUIFactory.createTextButtonStyle("play_button_inactive", "play_button_active", skin, bitmapFont)

  buttons = this.addButtonListener(GUIFactory.createTextButton("Yes", playStyle)) :: buttons

  // add buttons to table
  buttons.foreach(button => {
    mainTable.add(button)//.size(BUTTONS_WIDTH, BUTTONS_HEIGHT).row()
  })

  // add table to stage
  this.stage.addActor(mainTable)

  // enables mouse click on the stage (the screen container)
  Gdx.input.setInputProcessor(stage)

  override def render(delta: Float): Unit = {
    Gdx.gl.glClearColor(0, 0, 0, 1) // black color
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    this.stage.act()
    this.stage.draw()
  }

  override def resize(width: Int, height: Int): Unit = this.viewport.update(width, height)

  def getStage: Stage = this.stage

  private def addButtonListener(button: TextButton): TextButton = button.getText.toString match {
    case "Yes" =>
      button.getLabel.setFontScale(BUTTONS_FONT_SCALE)
      this.addPlayListener(button)
      button
    case "No" =>
      button.getLabel.setFontScale(BUTTONS_FONT_SCALE)
      this.addReturnToMenuListener(button)
      button
    case _ => throw new IllegalArgumentException()
  }

  private def addPlayListener(button: TextButton): Unit = {
    button.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = observerManager.notifyEvent(GameEvent.StartGame)
    })
  }

  private def addReturnToMenuListener(button: TextButton): Unit = {
    button.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = observerManager.notifyEvent(GameEvent.ReturnToMenu)
    })
  }

  override def show(): Unit = {}

  override def pause(): Unit = {}

  override def resume(): Unit = {}

  override def hide(): Unit = {}

  override def dispose(): Unit = observerManager.notifyEvent(GameEvent.CloseApplication)
}


package view.screens.game

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.{Color, OrthographicCamera, Texture}
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.{Image, Label, Table}
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.viewport.{FitViewport, Viewport}

import java.util.concurrent.{ExecutorService, Executors}

class Hud(width: Int, height: Int, spriteBatch: SpriteBatch) extends Disposable{

  private val viewPort: Viewport = new FitViewport(width, height, new OrthographicCamera())
  val stage: Stage = new Stage(viewPort, spriteBatch)

  private val FONT_PATH_LABEL = "assets/fonts/arial.fnt"
  private val HEALTH_BAR_PATH: String = "assets/textures/health_bar.png"

  private var score: Int = 0

  private var healthPercentage: Float = 1

  private val healthImage: Image = new Image(new Texture(HEALTH_BAR_PATH))
  healthImage.setWidth(80)
  val healthImageWidth: Float = healthImage.getWidth

  val healthTable: Table = new Table()
  healthTable.add(healthImage)//.expandX().fill(true, false)

  val levelLabel: Label = GUIFactoryImpl.createLabel("", GUIFactoryImpl.createBitmapFont(FONT_PATH_LABEL), Color.WHITE)
  val scoreLabel: Label = GUIFactoryImpl.createLabel("", GUIFactoryImpl.createBitmapFont(FONT_PATH_LABEL), Color.WHITE)
  val itemsTextLabel: Label = GUIFactoryImpl.createLabel("", GUIFactoryImpl.createBitmapFont(FONT_PATH_LABEL), Color.WHITE)
  levelLabel.setText("LEVEL 01")
  scoreLabel.setText("SCORE " + String.format("%06d", score))
  scoreLabel.setFontScale(0.2f)
  levelLabel.setFontScale(0.2f)
  itemsTextLabel.setFontScale(0.2f)

  var tableTop: Table = new Table()
  tableTop.top()
  tableTop.setFillParent(true)
  tableTop.add(healthTable).expandX().fill(0.8f, 0).padTop(10).padLeft(20)
  tableTop.add(levelLabel).expandX().center().padTop(10)
  tableTop.add(scoreLabel).expandX().padTop(10)

  var table = new Table()
  //it will display at the top
  table.top()
  table.padTop(40)
  //the table has the size of the stage
  table.setFillParent(true)

  table.add(tableTop)
  table.row()
  table.add(itemsTextLabel)


  //adds the table to the stage
  stage.addActor(tableTop)
  stage.addActor(table)


  def getStage: Stage = this.stage

  override def dispose(): Unit = stage.dispose()

  def changeHealth(currentHealth: Float, maxHealth: Float): Unit = {
    this.healthPercentage = currentHealth / maxHealth
    healthImage.setWidth(healthImageWidth * healthPercentage)
  }

  def drawHealthBar(batch: SpriteBatch): Unit = {
    if(healthPercentage > 0.6f)
      healthImage.setColor(Color.GREEN)
    else if(healthPercentage > 0.2f)
      healthImage.setColor(Color.ORANGE)
    else
      healthImage.setColor(Color.RED)

    healthImage.draw(batch, 0)
  }

  def setCurrentScore(score: Int): Unit = {
    scoreLabel.setText("SCORE " + String.format("%06d", score))
  }

  def setItemText(text: String): Unit = {
    val executorService: ExecutorService = Executors.newSingleThreadExecutor()

    val task: Runnable = () => {
      itemsTextLabel.setText(text)
      Thread.sleep(2500)
      itemsTextLabel.setText("")
    }

    executorService.submit(task)
  }
}



















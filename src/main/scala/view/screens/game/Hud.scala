package view.screens.game

import com.badlogic.gdx.graphics.g2d.{BitmapFont, SpriteBatch}
import com.badlogic.gdx.graphics.{Color, OrthographicCamera}
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.{Label, Table}
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.viewport.{FitViewport, Viewport}

class Hud(width: Int, height: Int, spriteBatch: SpriteBatch) extends Disposable{

  private val viewPort: Viewport = new FitViewport(width, height, new OrthographicCamera())
  val stage: Stage = new Stage(viewPort, spriteBatch)

  private val worldTimer: Int = 300
  private val timeCount: Float = 0
  private val score: Int = 0

  val countdownLabel: Label = createNewLabel("TIME " + String.format("%03d", worldTimer))
  val scoreLabel: Label = createNewLabel("SCORE " + String.format("%06d", score))
  val levelLabel: Label = createNewLabel("LEVEL 1")

  var table: Table = new Table()
  //it will display at the top
  table.top()
  //the table has the size of the stage
  table.setFillParent(true)

  table.add(scoreLabel).expandX().padTop(10)
  table.add(levelLabel).expandX().padTop(10)
  table.add(countdownLabel).expandX().padTop(10)

  //adds the table to the stage
  stage.addActor(table)

  private def createNewLabel(format: String) = {
    new Label(format, new Label.LabelStyle(new BitmapFont(), Color.WHITE))
  }

  def getStage(): Stage = this.stage

  override def dispose(): Unit = stage.dispose()
}



















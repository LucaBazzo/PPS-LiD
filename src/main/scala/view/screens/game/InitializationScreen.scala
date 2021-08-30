package view.screens.game

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.{GL20, OrthographicCamera}
import com.badlogic.gdx.scenes.scene2d.ui.{Table, TextButton}
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.{InputEvent, Stage}
import com.badlogic.gdx.utils.viewport.{FitViewport, Viewport}
import com.badlogic.gdx.{Gdx, ScreenAdapter}
import utils.ApplicationConstants

class InitializationScreen extends ScreenAdapter {

  private var batch:SpriteBatch = new SpriteBatch
  private var viewport:Viewport = new FitViewport(ApplicationConstants.WIDTH_SCREEN, ApplicationConstants.HEIGHT_SCREEN, camera)
  protected var stage: Stage = new Stage(viewport, batch)
  private var camera:OrthographicCamera = new OrthographicCamera

  override def show(): Unit = { //Stage should controll input:
    Gdx.input.setInputProcessor(stage)
    //Create Table
    val mainTable = new Table
    //Set table to fill stage
    mainTable.setFillParent(true)
    //Set alignment of contents in the table.
    mainTable.top
    //Create buttons
    val playButton = new TextButton("Play", new TextButton.TextButtonStyle())
    val optionsButton = new TextButton("Options", new TextButton.TextButtonStyle())
    val exitButton = new TextButton("Exit", new TextButton.TextButtonStyle())
    //Add listeners to buttons
    playButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        println("PLAY")
      }
    })
    exitButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        println("EXTI")
      }
    })
    //Add buttons to table
    mainTable.add(playButton)
    mainTable.row
    mainTable.add(optionsButton)
    mainTable.row
    mainTable.add(exitButton)
    //Add table to stage
    stage.addActor(mainTable)
  }

  override def render(delta: Float): Unit = {
    Gdx.gl.glClearColor(.1f, .12f, .16f, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    stage.act
    stage.draw

    super.render(delta)
    camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0)
    camera.update()

  }

  override def resize(width: Int, height: Int): Unit = {
    viewport.update(width, height)
    camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0)
    camera.update()
  }

  override def pause(): Unit = {
  }

  override def resume(): Unit = {
  }

  override def hide(): Unit = {
  }

  override def dispose(): Unit = {
  }
}
package view.screens.game

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.{Color, OrthographicCamera, Texture}
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.{Image, Label, Stack, Table}
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.viewport.{FitViewport, Viewport}
import model.entities.Items
import model.entities.Items.Items

import java.util.concurrent.{ExecutorService, Executors}

class Hud(width: Int, height: Int, spriteBatch: SpriteBatch) extends Disposable {

  private val viewPort: Viewport = new FitViewport(width, height, new OrthographicCamera())
  val stage: Stage = new Stage(viewPort, spriteBatch)

  private val FONT_PATH_LABEL = "assets/fonts/arial.fnt"
  private val HEALTH_BAR_PATH: String = "assets/textures/health_bar.png"
  private val HEALTH_BORDER_PATH: String = "assets/textures/health_bar_border.png"
  private val HEALTH_BAR_BOSS_PATH: String = "assets/textures/health_bar_boss.png"
  private val HEALTH_BORDER_BOSS_PATH: String = "assets/textures/health_bar_border_boss.png"

  private var score: Int = 0

  private var itemsPicked: List[Items] = List.empty

  private var healthPercentage: Float = 1


  val heroHealthImage: Image = new Image(new Texture(HEALTH_BAR_PATH))
  val heroHealthImageWidth: Float = heroHealthImage.getWidth
  val heroHealthBorder: Image = new Image(new Texture(HEALTH_BORDER_PATH))
  val heroHealthTable: Table = createHealthTable(heroHealthImage, heroHealthBorder)

  val bossHealthImage: Image = new Image(new Texture(HEALTH_BAR_BOSS_PATH))
  bossHealthImage.setColor(Color.RED)
  val bossHealthImageWidth: Float = bossHealthImage.getWidth
  val bossHealthBorder: Image = new Image(new Texture(HEALTH_BORDER_BOSS_PATH))
  val bossHealthTable: Table = createHealthTable(bossHealthImage, bossHealthBorder)

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
  tableTop.add(heroHealthTable).expandX().padTop(10).padLeft(20)
  tableTop.add(levelLabel).expandX().center().padTop(10)
  tableTop.add(scoreLabel).expandX().padTop(10).padRight(20)

  val bossTable: Table = new Table()
  bossTable.top()
  bossTable.setFillParent(true)
  bossTable.add(bossHealthTable).expandX().center().padTop(35)//.expandX().fill(0.6f,0)

  var itemsTable: Table = new Table()
  itemsTable.bottom().left()
  itemsTable.setFillParent(true)

  /*val bossHealthImage: Image = new Image(new Texture(HEALTH_BAR_PATH))
  val bossHealthImageWidth: Float = 200
  val bossHealthImageHeight: Float = 10
  bossHealthImage.setColor(Color.PURPLE)*/

  var table = new Table()
  //it will display at the top
  table.top()
  table.padTop(40)
  //the table has the size of the stage
  table.setFillParent(true)

  table.add(tableTop)
  table.row()
  table.add(itemsTextLabel)
  table.row()
  table.add(itemsTable)
  table.row()
  table.add(bossTable)

  //adds the table to the stage
  stage.addActor(tableTop)
  stage.addActor(table)
  stage.addActor(itemsTable)
  stage.addActor(bossTable)


  def getStage: Stage = this.stage

  override def dispose(): Unit = stage.dispose()

  def changeBossHealth(currentHealth: Float, maxHealth: Float): Unit = {
    /*val bossHealthPercentage = currentHealth / maxHealth
    bossHealthImage.setWidth(bossHealthImageWidth * bossHealthPercentage)*/
    //bossHealthImage.setHeight(bossHealthImageHeight)
  }
  def drawBossHealthBar(batch: SpriteBatch): Unit = {
    bossHealthImage.draw(batch, 0)
    println(bossHealthImage.getWidth)
  }

  def showBossHealthBar(): Unit = {
    this.bossHealthImage.setVisible(true)
  }

  def hideBossHealthBar(): Unit = {
    //    bossHealthTable.getColor.a = 0
    //this.bossHealthImage.setVisible(false)
  }

  def changeHealth(currentHealth: Float, maxHealth: Float): Unit = {
    this.healthPercentage = currentHealth / maxHealth
    heroHealthImage.setWidth(heroHealthImageWidth * healthPercentage)

    val bossHealthPercentage = currentHealth / maxHealth
    bossHealthImage.setWidth(bossHealthImageWidth * bossHealthPercentage)
  }

  def drawHealthBar(batch: SpriteBatch): Unit = {
    if (healthPercentage > 0.6f) {
      heroHealthImage.setColor(Color.GREEN)
      heroHealthBorder.setColor(Color.GREEN)
    } else if (healthPercentage > 0.2f) {
      heroHealthImage.setColor(Color.ORANGE)
      heroHealthBorder.setColor(Color.ORANGE)
    } else {
      heroHealthImage.setColor(Color.RED)
      heroHealthBorder.setColor(Color.RED)
    }

    heroHealthImage.draw(batch, 0)
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

  def addNewItem(item: Items): Unit = {
    if(! this.itemsPicked.contains(item)) {
      item match {
        case Items.BFSword =>
          addItemToTable("assets/textures/sword.png")
          this.itemsPicked = item :: this.itemsPicked
        case Items.Key =>
          addItemToTable("assets/textures/key.png")
          this.itemsPicked = item :: this.itemsPicked
        case Items.Boots =>
          addItemToTable("assets/textures/boots.png")
          this.itemsPicked = item :: this.itemsPicked
        case Items.Cake =>
          addItemToTable("assets/textures/Cake.png")
          this.itemsPicked = item :: this.itemsPicked
        case Items.Map =>
          addItemToTable("assets/textures/map.png")
          this.itemsPicked = item :: this.itemsPicked
        case Items.Shield =>
          addItemToTable("assets/textures/shield.png")
          this.itemsPicked = item :: this.itemsPicked
        case Items.SkeletonKey =>
          addItemToTable("assets/textures/SkeletonKey.png")
          this.itemsPicked = item :: this.itemsPicked
        case Items.Wrench =>
          addItemToTable("assets/textures/wrench.png")
          this.itemsPicked = item :: this.itemsPicked
        case _ => throw new UnsupportedOperationException
      }
    }
  }

  private def addItemToTable(path: String): Unit = {
    val itemImage = new Image(new Texture(path))
    itemImage.setScale(0.75f, 0.75f)
    itemsTable.add(itemImage).padLeft(5).padBottom(2)
  }

  private def createHealthTable(healthImage: Image, healthBorder: Image): Table = {

    val diamondBox: Stack = new Stack()
    diamondBox.addActor(healthImage)
    diamondBox.addActor(healthBorder)

    val healthTable: Table = new Table()
    healthTable.add(diamondBox)

    healthTable
  }
}


















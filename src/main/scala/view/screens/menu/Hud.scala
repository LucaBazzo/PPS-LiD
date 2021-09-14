package view.screens.menu

import com.badlogic.gdx.graphics.g2d.{SpriteBatch, TextureRegion}
import com.badlogic.gdx.graphics.{Color, OrthographicCamera, Texture}
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.{Image, Label, Table}
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.viewport.{FitViewport, Viewport}
import model.entities.Items
import model.entities.Items.Items
import utils.ApplicationConstants._

import java.util.concurrent.{ExecutorService, Executors}

class Hud(width: Int, height: Int, spriteBatch: SpriteBatch) extends Disposable {

  private val viewPort: Viewport = new FitViewport(width, height, new OrthographicCamera())
  private val stage: Stage = new Stage(viewPort, spriteBatch)

  private var itemsPicked: List[Items] = List.empty

  private var healthPercentage: Float = 1

  val heroHealthImage: Image = GUIFactory.createImage(HEALTH_BAR_PATH)
  val heroHealthImageWidth: Float = heroHealthImage.getWidth
  val heroHealthBorder: Image = GUIFactory.createImage(HEALTH_BORDER_PATH)
  val heroHealthTable: Table = GUIFactory.createHealthTable(heroHealthImage, heroHealthBorder)

  val bossHealthImage: Image = GUIFactory.createImage(HEALTH_BAR_BOSS_PATH)
  val bossHealthImageWidth: Float = bossHealthImage.getWidth
  val bossHealthBorder: Image = GUIFactory.createImage(HEALTH_BORDER_BOSS_PATH)
  val bossHealthTable: Table = GUIFactory.createHealthTable(bossHealthImage, bossHealthBorder)
  bossHealthImage.setColor(Color.RED)

  val levelLabel: Label = GUIFactory.createLabel("LEVEL 01",
    GUIFactory.createBitmapFont(FONT_PATH_LABEL), Color.WHITE)

  val scoreLabel: Label = GUIFactory.createLabel("SCORE " + String.format("%06d", 0),
    GUIFactory.createBitmapFont(FONT_PATH_LABEL), Color.WHITE)

  val itemsTextLabel: Label = GUIFactory.createLabel("",
    GUIFactory.createBitmapFont(FONT_PATH_LABEL), Color.WHITE)

  var tableTop: Table = new Table()
  //it will display at the top
  tableTop.top()
  //the table has the size of the stage
  tableTop.setFillParent(true)
  tableTop.add(heroHealthTable).expandX().padTop(HUD_FIRST_ROW_PADDING_TOP).padLeft(HUD_FIRST_ROW_PADDING_SIDE)
  tableTop.add(levelLabel).expandX().center().padTop(HUD_FIRST_ROW_PADDING_TOP)
  tableTop.add(scoreLabel).expandX().padTop(HUD_FIRST_ROW_PADDING_TOP).padRight(HUD_FIRST_ROW_PADDING_SIDE)

  val bossTable: Table = new Table()
  bossTable.top()
  bossTable.setFillParent(true)
  bossTable.add(bossHealthTable).expandX().center().padTop(HUD_BOSS_HEALTH_BAR_PADDING)

  var itemsTable: Table = new Table()
  itemsTable.bottom().left()
  itemsTable.setFillParent(true)

  var table = new Table()
  table.top()
  table.padTop(HUD_PADDING_TOP)
  table.setFillParent(true)

  table.add(tableTop)
  table.row()
  table.add(itemsTextLabel)
  table.row()
  table.add(itemsTable)
  table.row()
  table.add(bossTable)

  val t = table.getBackground

  // Set background
  val backgroundTexture: Texture  = GUIFactory.createTexture("assets/backgrounds/background_loading.png")
  table.background(new TextureRegionDrawable(new TextureRegion(backgroundTexture)))

  //adds the table to the stage
  stage.addActor(tableTop)
  stage.addActor(table)
  stage.addActor(itemsTable)
  stage.addActor(bossTable)

  this.hideBossHealthBar()

  override def dispose(): Unit = stage.dispose()

  def getStage: Stage = this.stage

  def changeBossHealth(currentHealth: Float, maxHealth: Float): Unit = {
    val bossHealthPercentage = currentHealth / maxHealth
    bossHealthImage.setWidth(bossHealthImageWidth * bossHealthPercentage)
  }

  def drawBossHealthBar(batch: SpriteBatch): Unit = {
    bossHealthImage.draw(batch, 0)
  }

  def showBossHealthBar(): Unit = {
    this.bossHealthBorder.setVisible(true)
    this.bossHealthImage.setVisible(true)
  }

  def hideBossHealthBar(): Unit = {
    this.bossHealthBorder.setVisible(false)
    this.bossHealthImage.setVisible(false)
  }

  def changeHealth(currentHealth: Float, maxHealth: Float): Unit = {
    this.healthPercentage = currentHealth / maxHealth
    heroHealthImage.setWidth(heroHealthImageWidth * healthPercentage)
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
    if(! this.itemsPicked.contains(item) && ! (item == Items.PotionS || item == Items.PotionM ||
      item == Items.PotionL || item == Items.PotionXL)) {
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
        case Items.Armor =>
          addItemToTable("assets/textures/Armor.png")
          this.itemsPicked = item :: this.itemsPicked
        case Items.Bow =>
          addItemToTable("assets/textures/greatbow.png")
          this.itemsPicked = item :: this.itemsPicked
        case _ => throw new UnsupportedOperationException
      }
    }
  }

  def setLevelNumber(levelNumber: Int): Unit = {
    var levelText = "LEVEL "
    if(levelNumber < 10) levelText += "0"
    levelText += levelNumber

    this.levelLabel.setText(levelText)
  }

  def loadingFinished(): Unit ={
    table.background(t)
  }

  private def addItemToTable(path: String): Unit = {
    val itemImage = new Image(new Texture(path))
    itemImage.setScale(0.75f, 0.75f)
    itemsTable.add(itemImage).padLeft(5).padBottom(2)
  }
}


















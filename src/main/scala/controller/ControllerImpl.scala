package controller

import com.badlogic.gdx.physics.box2d._
import controller.GameEvent.GameEvent
import model._
import model.helpers.EntitiesContainerMonitor
import _root_.utils.ApplicationConstants.{GAME_LOOP_STEP, GRAVITY_FORCE}
import com.badlogic.gdx.math.Vector2
import view._

import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

/** Handles almost every aspect that allows the game to run such as starting and
 * stopping the game loop, initializing view and model, saving existing game run
 * and load it, handling inputs and closing the application.
 */
trait Controller {

  def stopExecutorService()
  def gameOver()
  def newLevel()
}


/** This class represent the Controller of the all game.
 */
class ControllerImpl extends Controller with Observer {

  private val entitiesContainer: EntitiesContainerMonitor = new EntitiesContainerMonitor()
  private val observerManager: ObserverManager = new ObserverManagerImpl()
  this.observerManager.addObserver(this)

  private val vector2: Vector2 = new Vector2(0f,-5f)
  private val world: World = new World(vector2, true)
  //private val world: World = new World(GRAVITY_FORCE, true)

  private var level: Level = new LevelImpl(entitiesContainer, world)

  private val view: View = new ViewImpl(entitiesContainer, observerManager, world, level)
  private val model: Model = new ModelImpl(entitiesContainer, level)

  private val executorService: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
  private val gameLoop: GameLoop = new GameLoopImpl(model)

  this.executorService.scheduleAtFixedRate(gameLoop, 0, GAME_LOOP_STEP, TimeUnit.NANOSECONDS)

  //TODO only when the main menu is ready
  //this.view.startGame()

  override def handleEvent(event: GameEvent): Unit = event match {
    case GameEvent.CloseApplication => {
      // terminate game loop executor
      this.executorService.shutdown()

      // let view terminate itself
      this.view.terminate()
    }
    case _ => this.gameLoop.addAction(event)
  }

  override def gameOver(): Unit = ???

  override def newLevel(): Unit = ???

  override def stopExecutorService(): Unit = this.executorService.shutdown()
}

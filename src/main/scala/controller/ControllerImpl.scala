package controller

import _root_.utils.ApplicationConstants.GAME_LOOP_STEP
import _root_.utils.MapConstants._
import controller.GameEvent.GameEvent
import model._
import model.helpers.EntitiesContainerMonitor
import view._

import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
import scala.util.Random

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

  //scelgo casualmente 6 stanze da mettere nel world (le stanze non devono ripetersi)
  private var innerRooms: Array[String] = Array()
  while (innerRooms.length < 6){
    val room: String = INNER_ROOM_MAP_NAMES(Random.nextInt(INNER_ROOM_MAP_NAMES.size))
    if(!innerRooms.contains(room)) innerRooms = innerRooms :+ room
  }

  private val rooms: Array[(String,(Integer,Integer))] = Array(
    (WORLD_LEFT_BORDER_NAME, WORLD_LEFT_BORDER_OFFSET),
    (WORLD_TOP_BORDER_NAME, WORLD_TOP_BORDER_OFFSET),
    (WORLD_RIGHT_BORDER_NAME, WORLD_RIGHT_BORDER_OFFSET),
    (WORLD_BOTTOM_BORDER_NAME, WORLD_BOTTOM_BORDER_OFFSET),
    (HERO_ROOM_MAP_NAME, HERO_ROOM_OFFSET),
    (BOSS_ROOM_MAP_NAME, BOSS_ROOM_OFFSET),
    (TOP_KEY_ITEM_ROOM_NAME, TOP_KEY_ITEM_ROOM_OFFSET),
    (BOTTOM_KEY_ITEM_ROOM_NAME, BOTTOM_KEY_ITEM_ROOM_OFFSET),
    (innerRooms(0), INNER_ROOM_MAP_OFFSET(0)),
    (innerRooms(1), INNER_ROOM_MAP_OFFSET(1)),
    (innerRooms(2), INNER_ROOM_MAP_OFFSET(2)),
    (innerRooms(3), INNER_ROOM_MAP_OFFSET(3)),
    (innerRooms(4), INNER_ROOM_MAP_OFFSET(4)),
    (innerRooms(5), INNER_ROOM_MAP_OFFSET(5)),
    (INNER_BORDER_NAMES(Random.nextInt(INNER_BORDER_NAMES.length)), INNER_BORDER_OFFSET))

  private val view: View = new ViewImpl(entitiesContainer, observerManager, rooms)
  private val model: Model = new ModelImpl(entitiesContainer, rooms)

  private val executorService: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
  private val gameLoop: GameLoop = new GameLoopImpl(model, this)

  this.executorService.scheduleAtFixedRate(gameLoop, 0, GAME_LOOP_STEP, TimeUnit.NANOSECONDS)

  //TODO only when the main menu is ready
  //this.view.startGame()

  override def handleEvent(event: GameEvent): Unit = event match {
    case GameEvent.CloseApplication => this.terminateApplication()
    case _ => this.gameLoop.addAction(event)
  }

  override def gameOver(): Unit = {
    this.terminateApplication()
  }

  override def newLevel(): Unit = ???

  override def stopExecutorService(): Unit = this.executorService.shutdown()

  private def terminateApplication(): Unit = {
    // terminate game loop executor
    this.executorService.shutdown()

    // let view terminate itself
    this.view.terminate()
  }
}

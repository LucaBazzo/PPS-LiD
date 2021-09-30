package controller

import com.badlogic.gdx.physics.box2d.World
import model.Score
import model.entity.Items.Items
import model.entity.Statistic.Statistic
import model.entity._
import utils.EnemiesConstants.BOSS_TYPES
import utils.HeroConstants.HERO_STATISTICS_DEFAULT

import java.util.concurrent.{ExecutorService, Executors}

/** This trait represents all the data accessible from the view package. If
 * combine with the trait EntitiesSetter, the derived class could be used to
 * hold the game state for both the view (by providing only getter
 * functionalities) and the model.
 *
 * @see [[controller.EntitiesSetter]]
 */
trait EntitiesGetter {

  /** Retrieve specific game entities. Each entity contains the
   * LibGDX body (which defines where it is located and what the entity is
   * represented by), the entity type, the current state and the behaviour
   * adopted when colliding with other entities.
   *
   * @see [[model.entity.Entity]]
   *
   * @param predicate the predicate used to filter out unwanted entities
   * @return a list of game entities
   */
  def getEntities(predicate: Entity => Boolean): List[Entity]

  /** Retrieve real time hero statistics. Those statistics defines how
   * "strong" the hero is. Generally higher values implies an easier
   * adventure.
   *
   * @see [[model.entity.Statistic]]
   *
   * @return a map of statistics with matching values
   */
  def getHeroStatistics: Map[Statistic, Float]

  /** Retrieve the LibGDX world instance. The World object contains useful
   * functionalities for geometric bodies exploration and gameplay
   * configuration.
   *
   * @see [[com.badlogic.gdx.physics.box2d.World]]
   *
   * @return a LibGDX world instance
   */
  def getWorld: Option[World]

  /** Retrieve the game real time score. The score is influenced by killed
   * enemies and picked up items.
   *
   * @return an integer positive value
   */
  def getScore: Int

  /** Retrieve gameplay messages to be shown when the hero entity interacts
   * with the map environment (picking up items or checking a door).
   *
   * @return an optional string
   */
  def getMessage: Option[String]

  /** Holds the latest items picked by the hero. Each Item contains
   * the specific strategy to apply to the hero statistics. After a non empty
   * call of this method, the reference to the latest picked items is re-setted.
   *
   * @see [[model.entity.Items]]
   *
   * @return a List of Item object
   */
  def hasHeroPickedUpItem: List[Items]

  /** Retrieve the current level number. The level number increases as the hero
   * advances in the exploration of the game by entering a portal to the next
   * level.
   *
   * @return a positive integer number
   */
  def getLevelNumber: Int

  /** Flag representing the state of the level creation. During this state the
   * game world is being initialized and no entities can be safely interacted
   * with.
   *
   * @return a boolean value. If true, the game is ready to be displayed.
   */
  def isLevelReady: Boolean

  /** Access and retrieve all game entities. Each entity contains the
   * LibGDX body (which defines where it is located and what the entity is
   * represented by), the entity type, the current state and the behaviour
   * adopted when colliding with other entities.
   *
   * @see [[controller.EntitiesGetter#getEntities(scala.Function1) ]]
   * @see [[model.entity.Entity]]
   *
   * @return a list of game entities
   */
  def getAllEntities: List[Entity] = synchronized {
    getEntities(_ => true)
  }

  /** Retrieve the first entity found in the level that matches a given
   * predicate. This entity contains the LibGDX body (which defines where it is
   * located and what the entity is represented by), the entity type, the
   * current state and the behaviour adopted when colliding with other entities.
   *
   * This functionality is a shorthand of another functionality provided by
   * this trait.
   *
   * @see [[controller.EntitiesGetter#getEntities(scala.Function1) ]]
   * @see [[model.entity.Entity]]
   *
   * @param predicate the predicate used to filter out unwanted entities
   * @return an optional Entity if found
   */
  def getEntity(predicate: Entity => Boolean): Option[Entity] = getEntities(predicate) match {
    case e if e.nonEmpty => Option(e.head)
    case _ => None
  }

  /** Retrieve the Hero entity. The Hero is a particular kind of entity able
   * to move, attack, pick up items, interact with the game world and suffer
   * damage. This entity is governed directly by the player by executing
   * provided commands.
   *
   * This functionality is a shorthand of another functionality provided by
   * this trait.
   *
   * @see [[controller.EntitiesGetter#getEntities(scala.Function1) ]]
   * @see [[model.entity.Hero]]
   *
   * @return an optional Hero entity if found
   */
  def getHero: Option[Hero] = getEntity(e => e.getType equals EntityType.Hero) match {
    case e if e.nonEmpty => Option(e.get.asInstanceOf[Hero])
    case _ => None
  }

  /** Retrieve a boss type enemy. This enemy is a particular kind of entity
   * able to move, attack and suffer damage. This entity is directly governed
   * by the game itself and it's sole purpose is to attack and kill the hero.
   *
   * This functionality is a shorthand of another functionality provided by
   * this trait.
   *
   * @see [[model.entity.LivingEntity]]
   *
   * @return an optional LivingEntity entity if found
   */
  def getBoss: Option[LivingEntity] = getEntity(e => BOSS_TYPES contains e.getType) match {
    case e if e.nonEmpty => Option(e.get.asInstanceOf[LivingEntity])
    case _ => None
  }

  /** Retrieve the complete list of items collected by the hero in this run
   *
   * @return the list of items owned by the hero
   */
  def getItemsPicked: List[Items]
}

trait EntitiesSetter {
  /** Set the current level entities. Each entity contains the
   * LibGDX body (which defines where it is located and what the entity is
   * represented by), the entity type, the current state and the behaviour
   * adopted when colliding with other entities.
   *
   * This functionality is also useful whenever the level must be re-setted, by
   * providing an empty list.
   +
   * @see [[model.entity.Entity]]
   *
   * @param entities a list of game entities.
   */
  def setEntities(entities: List[Entity]): Unit

  /** Provide the LibGDX world instance. The World object contains useful
   * functionalities for geometric bodies exploration and gameplay
   * configuration.
   *
   * This functionality is also useful whenever the level must be re-setted, by
   * providing an empty list.
   *
   * @param world a com.badlogic.gdx.physics.box2d.World instance
   */
  def setWorld(world: Option[World]): Unit

  /** Reset the game score, bringing it to 0. This method should be called when
   * a new game has to be started and a previous one is terminated.
   */
  def resetScore(): Unit

  /** Increase the game score when the hero slays an enemy or when he picks up
   * an item.
   *
   * @param score an integer positive value
   */
  def addScore(score: Int): Unit

  /** Set a new message to be displayed. This message is defined when the hero
   * interacts with items or the environment.
   *
   * @param message a string
   */
  def addMessage(message: String): Unit

  /** Set the latest item picked by the hero. It contains the specific strategy
   * to apply to the hero statistics.
   *
   * @see [[model.entity.Items]]
   *
   * @param item an optional Item object
   */
  def heroJustPickedUpItem(item: Items): Unit

  /** Remove a specific item from the hero
   *
   * @see [[model.entity.Items]]
   *
   * @param item the item to be removed
   */
  def heroLoseItem(item: Items): Unit

  /** Set real time hero statistics. Those statistics defines how "strong" the
   * hero is. Generally higher values implies an easier adventure.
   *
   * @see [[model.entity.Statistic]]
   *
   * @param statistics a map of statistics with matching values
   */
  def setHeroStatistics(statistics: Map[Statistic, Float]): Unit

  /** Set the current level number. The level number increases as the hero
   * advances in the exploration of the game by entering a portal to the next
   * level.
   *
   * @param number a positive integer number
   */
  def setLevelNumber(number: Int): Unit

  /** Flag representing the state of the level creation. During this state the
   * game world is being initialized and no entities can be safely interacted
   * with.
   *
   * @param ready a boolean value. If true, the game is ready to be displayed.
   */
  def setLevelReady(ready: Boolean): Unit

  /** Add a newly created entity to the entities pool. After this method
   * execution, the entity will be updated. This method should be
   * called only when an entity (and it's Body) is created.
   *
   * @see [[model.entity.Entity]]
   *
   * @param entity the entity to be added
   */
  def addEntity(entity: Entity): Unit

  /** Remove an entity from the entities pool. After this method
   * execution, the entity will not be updated anymore. This method should be
   * called only when an entity (and it's Body) must be destroyed.
   *
   * @see [[model.entity.Entity]]

   * @param entity the entity to be added
   */
  def removeEntity(entity: Entity): Unit
}

class ModelResources extends EntitiesGetter with EntitiesSetter {

  private var world: Option[World] = Option.empty
  private var messages: List[String] = List.empty
  private var entities: List[Entity] = List.empty
  private var heroPickedUpAnItem: List[Items] = List.empty
  private var itemsPicked: List[Items] = List.empty
  private var levelNumber = 0
  private var score: Int = 0

  private var heroStatistics: Map[Statistic, Float] = HERO_STATISTICS_DEFAULT

  private var levelReady: Boolean = false

  override def getEntities(predicate: Entity => Boolean): List[Entity] = synchronized {
    this.entities.filter(predicate)
  }

  override def getWorld: Option[World] = synchronized {
    this.world
  }

  override def getScore: Int = synchronized {
    this.score
  }

  override def setEntities(entities: List[Entity]): Unit = synchronized {
    this.entities = entities
  }

  override def setWorld(world: Option[World]): Unit = synchronized {
    this.world = world
  }

  override def resetScore(): Unit = synchronized {
    this.score = 0
  }

  override def addScore(score: Int): Unit = synchronized {
    this.score += score
  }

  override def getMessage: Option[String] = {
    var res: Option[String] = Option.empty
    if (this.messages.nonEmpty) {
      res = Option.apply(this.messages.head)
      this.messages = List.empty
    }
    res
  }

  override def addMessage(message: String): Unit = this.messages = message :: this.messages

  override def getHeroStatistics: Map[Statistic, Float] = this.heroStatistics

  override def setHeroStatistics(statistics: Map[Statistic, Float]): Unit = this.heroStatistics = statistics

  override def setLevelNumber(levelNumber: Int): Unit = this.levelNumber = levelNumber

  override def getLevelNumber: Int = this.levelNumber

  override def hasHeroPickedUpItem: List[Items] = {
    val res = this.heroPickedUpAnItem
    this.heroPickedUpAnItem = List.empty
    res
  }

  override def heroJustPickedUpItem(item: Items): Unit = {
    this.itemsPicked = item :: this.itemsPicked
    this.heroPickedUpAnItem = item :: this.heroPickedUpAnItem
  }

  override def heroLoseItem(item: Items): Unit = this.itemsPicked = this.itemsPicked.filter(it => it != item)

  override def getItemsPicked: List[Items] = this.itemsPicked

  override def isLevelReady: Boolean = this.levelReady

  override def setLevelReady(ready: Boolean): Unit = this.levelReady = ready

  override def addEntity(entity: Entity): Unit = this.entities = entity :: this.entities

  override def removeEntity(entity: Entity): Unit = {
    this.entities = this.entities.filterNot((e: Entity) => e.equals(entity))

    // update score if the removed entity's type is Enemy or Item
    if (entity.isInstanceOf[Enemy] || entity.isInstanceOf[Item]) {
      this.addScore(entity.asInstanceOf[Score].getScore)
    }

    if (BOSS_TYPES.contains(entity.getType)) {
      val portal: Entity = this.getEntity(x => x.getType == EntityType.Portal).get
      portal.setState(State.Opening)
      val executorService: ExecutorService = Executors.newSingleThreadExecutor()
      executorService.execute(() => {
        Thread.sleep(1000)
        portal.setState(State.Standing)
      })
      executorService.shutdown()
    }
  }
}

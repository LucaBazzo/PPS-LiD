package model.entities

import controller.GameEvent
import controller.GameEvent.GameEvent
import model.attack.DoNothingAttackStrategy
import model.collisions.EntityCollisionBit
import model.entities.EntityType.EntityType
import model.entities.Items.Items
import model.entities.State._
import model.entities.Statistic._
import model.helpers.{EntitiesFactoryImpl, WorldUtilities}
import model.movement.{DoNothingMovementStrategy, HeroMovementStrategy}
import model.{EntityBody, HeroInteraction}
import utils.HeroConstants._

/** Represents the entity that will be moved by the player, it can move or attack
 *  based on the command received, change its size and interact with the environment.
 */
trait Hero extends LivingEntity {

  /** Implementation of the Entity Hero that will be command by the player.
   *
   *  @param command the command from the player to the hero
   */
  def notifyCommand(command: GameEvent): Unit

  /** Check if the hero is crouch or sliding.
   *
   *  @return true if the hero is little
   */
  def isLittle: Boolean

  /** Called when the hero changes its size.
   *
   *  @param little true if the hero has become little
   */
  def setLittle(little: Boolean): Unit

  /** Changes the hero box that will collide with another entities.
   *
   *  @param newSize the new size of the box
   *  @param addCoordinates the offset from the previous position
   */
  def changeHeroFixture(newSize: (Float, Float), addCoordinates: (Float, Float) = (0,0)): Unit

  def getItemsPicked: List[Items]
  /** Called when the hero pick an important item.
   *
   *  @param itemType the type of the item picked
   */
  def itemPicked(itemType: Items): Unit

  /** Check if the items specified was already picked by the hero.
   *
   *  @return true if the item type is present
   */
  def isItemPicked(item: Items): Boolean

  /** Sets an environment interaction for the hero, when the command choose is notified
   *  to the hero, itself will call the EnvironmentInteraction.
   *
   *  @param interaction A pair of Command - EnvironmentInteraction
   */
  def setEnvironmentInteraction(interaction: Option[HeroInteraction]): Unit

  /** Stops the update method of the hero for a chosen time.
   *
   *  @param time how much time the hero will not perform the update method
   */
  def stopHero(time: Float): Unit

  /** Sets the feet of the hero, used to check if the hero is touching the ground.
   *
   *  @param feet a mobile entity that will be attached at the bottom of the hero body
   */
  def setFeet(feet: MobileEntity): Unit

  /** Returns the hero's feet.
   *
   *  @return the feet if presents
   */
  def getFeet: Option[MobileEntity]

  /** Check if the hero is touching the ground with the feet.
   *
   *  @return true if it touching the ground
   */
  def isTouchingGround: Boolean

  /** Check if the hero is touching the wall on a specific side.
   *
   *  @param rightSide witch side the touching will be checked
   *  @return true if it touching a wall
   */
  def isTouchingWallOnSide(rightSide: Boolean = true): Boolean

  /** Check if the hero health is below 0
   *
   *  @return true if the hero is dead
   */
  def isDead: Boolean

  /** Notify the hero if he is doing an air attack.
   *
   *  @param isAttacking true if the air attack has begun
   */
  def setAirAttacking(isAttacking: Boolean): Unit

  /** Remove one of Hero's held items
   *
   *  @param item item to be losed
   */
  def loseItem(item: Items): Unit
}

/** Implementation of the Entity Hero that will be command by the player.
 *
 *  @constructor create a new hero
 *  @param entityType the type of entity that will be rendered by the view
 *  @param entityBody the container for the hero's body
 *  @param size the dimensions of the hero
 *  @param stats the statistics that affects the hero interactions
 */
class HeroImpl(private val entityType: EntityType,
               private var entityBody: EntityBody,
               private var size: (Float, Float),
               private val stats: Map[Statistic, Float])
  extends LivingEntityImpl(entityType, entityBody, size, stats) with Hero{

  private var feet: Option[MobileEntity] = Option.empty
  private var interaction: Option[HeroInteraction] = Option.empty
  private var itemsPicked: List[Items] = List.empty

  private var little: Boolean = false
  private var waitTimer: Float = 0

  private var isAirAttacking: Boolean = false

  override def notifyCommand(command: GameEvent): Unit = {
    if(this.interaction.nonEmpty && this.interaction.get.command == command)
      this.interaction.get.environmentInteraction.apply()
    else {
      command match {
        case GameEvent.Up | GameEvent.UpReleased | GameEvent.MoveRight | GameEvent.MoveLeft
             | GameEvent.Slide | GameEvent.Down | GameEvent.DownReleased => move(command)
        case GameEvent.Attack | GameEvent.BowAttack => attack(command)
        case GameEvent.Interaction =>
        case _ => throw new UnsupportedOperationException
      }
    }
  }

  //TODO da decidere
  def move(command: GameEvent): Unit = {
    if(this.movementStrategy != null)
      this.movementStrategy.apply(command)
  }

  def attack(command: GameEvent): Unit = {
    if(this.attackStrategy != null)
      this.attackStrategy.apply(command)
  }

  override def update(): Unit = {

    if(isNotWaiting) {
      if(this.isSlidingFinished)
        this.stopMovement()

      if(isAirAttackFinished)
        this.finishAirAttack()

      if(isDead)
        this.setState(State.Dying)
      else if(checkFalling)
        this.setState(State.Falling)
      else if(checkRunning)
        this.setState(State.Running)
      else if(!isFalling && (this is AirDownAttacking))
        this.setAirDownAttackEnd()
      else if(checkIdle)
        this.setState(State.Standing)

      if(checkNotLittle) {
        this.changeHeroFixture(HERO_SIZE, CROUCH_END_OFFSET)
        this.setLittle(false)
      }

      if(!this.attackStrategy.isAttackFinished) {
        this.attackStrategy.checkTimeEvent()
        this.attackStrategy.decrementAttackTimer()

        if(this.isBowAttackFinished){
          this.setState(State.Standing)
        }
        else if(this.attackStrategy.isAttackFinished){
          this.attackStrategy.stopAttack()
          this.setState(State.Standing)
        }
      }

    } else this.decrementWaiting(WAIT_TIME_DECREMENT)  //for sliding and crouch redefinition of body
  }

  override def setState(state: State): Unit = {
    super.setState(state)
  }

  override def setLittle(little: Boolean): Unit = {
    this.little = little
    this.stopHero(SHORT_WAIT_TIME)
  }

  override def isLittle: Boolean = this.little

  override def changeHeroFixture(newSize: (Float, Float), addCoordinates: (Float, Float) = (0,0)): Unit = {
    EntitiesFactoryImpl.addPendingEntityCreation(() => EntitiesFactoryImpl.changeHeroFixture(this, newSize, addCoordinates))
  }

  override def itemPicked(itemType: Items): Unit = {
    this.stopHero(LONG_WAIT_TIME)
    this.stopMovement()
    this.setState(State.pickingItem)
    this.itemsPicked = itemType :: this.itemsPicked
  }

  override def getItemsPicked: List[Items] = this.itemsPicked

  override def isItemPicked(item: Items): Boolean = this.itemsPicked.contains(item)

  override def loseItem(item: Items): Unit = {
    this.itemsPicked = this.itemsPicked.filter(x => x != item)
  }

  override def setEnvironmentInteraction(interaction: Option[HeroInteraction]): Unit = {
    this.interaction = interaction
    if(interaction.isEmpty)
      this.restoreNormalMovementStrategy()
  }

  override def stopHero(time: Float): Unit = this.waitTimer = time

  override def sufferDamage(damage: Float): Unit = {
    //the hero is invincible when he is sliding
    if(!(this is Sliding)) {
      super.sufferDamage(damage)
      //when the hero is dead, it changes the attack and movement strategy
      if (isDead) {
        this.attackStrategy.stopAttack()
        this.setAttackStrategy(DoNothingAttackStrategy())
        this.setMovementStrategy(DoNothingMovementStrategy())
      }
      else {
        //hurt when on ladder
        if((this is LadderClimbing) || (this is LadderDescending) || (this is LadderIdle)){
          this.restoreNormalMovementStrategy()
        }

        this.stopHero(SHORT_WAIT_TIME)
        this.setState(State.Hurt)
      }
    }
  }

  override def stopMovement(): Unit = super.stopMovement()

  override def isTouchingGround: Boolean = if(this.feet.nonEmpty) this.feet.get.isColliding else false

  override def setFeet(feet: MobileEntity): Unit = this.feet = Option.apply(feet)

  override def getFeet: Option[MobileEntity] = this.feet

  override def isTouchingWallOnSide(rightSide: Boolean = true): Boolean = {
    WorldUtilities.checkSideCollision(rightSide, this,
      EntityCollisionBit.Immobile, EntityCollisionBit.Door)
  }

  override def setAirAttacking(isAttacking: Boolean): Unit = this.isAirAttacking = isAttacking

  override def isDead: Boolean = this.getStatistic(Statistic.CurrentHealth).get <= 0

  private def restoreNormalMovementStrategy(): Unit = {
    this.setMovementStrategy(new HeroMovementStrategy(this, this.getStatistic(MovementSpeed).get))
    this.getEntityBody.setGravityScale()
    this.setState(State.Falling)
    this.getBody.setAwake(true)
  }

  private def isNotWaiting: Boolean = this.waitTimer <= 0
  private def decrementWaiting(value: Float): Unit = this.waitTimer -= value

  private def isFalling: Boolean = !this.isTouchingGround && this.entityBody.getBody.getLinearVelocity.y < 0
  private def isMovingHorizontally: Boolean = this.entityBody.getBody.getLinearVelocity.x != 0 && this.entityBody.getBody.getLinearVelocity.y == 0
  private def isIdle = this.entityBody.getBody.getLinearVelocity.x == 0 && this.entityBody.getBody.getLinearVelocity.y == 0

  private def checkFalling: Boolean = isFalling && (this isNot Jumping) && (this isNot LadderDescending) && (this isNot LadderClimbing) && (this isNot AirDownAttacking)
  private def checkRunning: Boolean = isMovingHorizontally && ((this is Jumping) || (this is Falling))
  private def checkIdle: Boolean = {
    isIdle && !isSwordAttacking && (this isNot Crouching) && (this isNot BowAttacking) && (this isNot LadderIdle) &&
    (this isNot LadderClimbing) && (this isNot LadderDescending)
  }

  private def checkNotLittle: Boolean = (this isNot Sliding) && (this isNot Crouching) && isLittle
  private def isSwordAttacking: Boolean = (this is Attack01) || (this is Attack02) || (this is Attack03)
  private def isBowAttackFinished: Boolean = (this is BowAttacking) && this.attackStrategy.isAttackFinished
  private def isSlidingFinished: Boolean = {
    (this.entityBody.getBody.getLinearVelocity.x <= 1 && (this is Sliding) && isFacingRight) ||
      (this.entityBody.getBody.getLinearVelocity.x >= -1 && (this is Sliding) && !isFacingRight)
  }

  private def setAirDownAttackEnd(): Unit = {
    this.stopHero(LONG_WAIT_TIME)
    this.setState(AirDownAttackingEnd)
  }

  private def isAirAttackFinished: Boolean = isAirAttacking && (this isNot AirDownAttacking) && (this isNot AirDownAttackingEnd)

  private def finishAirAttack(): Unit = {
    this.attackStrategy.stopAttack()
    this.restoreNormalMovementStrategy()
    this.setAirAttacking(false)
  }
}

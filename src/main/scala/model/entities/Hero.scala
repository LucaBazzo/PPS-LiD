package model.entities

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import controller.GameEvent
import controller.GameEvent.GameEvent
import model.attack.{DoNothingAttackStrategy, HeroAttackStrategy}
import model.collisions.{DoNothingCollisionStrategy, EntityCollisionBit}
import model.entities.EntityType.EntityType
import model.entities.Items.Items
import model.entities.State._
import model.entities.Statistic._
import model.helpers.EntitiesFactoryImpl.{createPolygonalShape, defineEntityBody}
import model.helpers.{EntitiesFactoryImpl, WorldUtilities}
import model.movement.{DoNothingMovementStrategy, HeroMovements}
import model.{EntityBody, HeroInteraction}
import utils.CollisionConstants.{HERO_COLLISIONS, HERO_FEET_COLLISIONS}
import model.collisions.ImplicitConversions._
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

  /** Notify the hero if he is doing an air attack.
   *
   *  @param isAttacking true if the air attack has begun
   */
  def setAirAttacking(isAttacking: Boolean): Unit

  /** Sets the normal movement strategy of the Hero.
   *
   */
  def restoreNormalMovementStrategy(): Unit

  /** Remove one of Hero's held items
   *
   *  @param item item to be lost
   */
  def loseItem(item: Items): Unit

  /** Check if the hero is touching the ground with the feet.
   *
   *  @return true if it touching the ground
   */
  def isTouchingGround: Boolean = if(getFeet.nonEmpty) getFeet.get.isColliding else false

  /** Check if the hero is touching the wall on a specific side.
   *
   *  @param rightSide witch side the touching will be checked
   *  @return true if it touching a wall
   */
  def isTouchingWallOnSide(rightSide: Boolean = true): Boolean = {
    WorldUtilities.checkSideCollision(rightSide, this,
      EntityCollisionBit.Immobile, EntityCollisionBit.Door)
  }

  /** Check if the hero health is below 0
   *
   *  @return true if the hero is dead
   */
  def isDead: Boolean = getStatistic(Statistic.CurrentHealth).get <= 0
}

object Hero {

  def apply(stats: Map[Statistic, Float] = HERO_STATISTICS_DEFAULT): Hero = {
    val hero: Hero = new HeroImpl(EntityType.Hero, createEntityBody(), HERO_SIZE.PPM, stats)

    hero.setCollisionStrategy(DoNothingCollisionStrategy())
    hero.setMovementStrategy(new HeroMovements(hero, stats(Statistic.MovementSpeed)))
    hero.setAttackStrategy(new HeroAttackStrategy(hero, stats(Statistic.Strength)))

    this.createHeroFeet(hero)
    EntitiesFactoryImpl.addEntity(hero)
    hero
  }

  /** Change the hero size and sets new feet
   *
   *  @param hero the player
   *  @param newSize the new dimension of the hero
   */
  def changeHeroSize(hero: Hero, newSize: (Float, Float)): Unit = {
    hero.getEntityBody
      .setShape(createPolygonalShape(newSize.PPM))
      .createFixture()

    hero.getEntityBody.addCoordinates(0, -hero.getSize._2 + newSize._2.PPM)
    hero.setSize(newSize.PPM)

    this.createHeroFeet(hero)
  }

  private def createHeroFeet(hero: Hero): Unit = {
    if(hero.getFeet.nonEmpty) {
      EntitiesFactoryImpl.destroyBody(hero.getFeet.get.getBody)
      EntitiesFactoryImpl.removeEntity(hero.getFeet.get)
    }

    val bodyPosition = hero.getPosition - (0, hero.getSize.y)
    val feetBody: EntityBody = defineEntityBody(BodyType.DynamicBody, EntityCollisionBit.Hero,
      HERO_FEET_COLLISIONS, createPolygonalShape(FEET_SIZE.PPM, rounder = true),
      bodyPosition, gravityScale = 0, friction = HERO_FRICTION)
    EntitiesFactoryImpl.createJoint(hero.getBody, feetBody.getBody)

    val heroFeet: MobileEntity = new MobileEntityImpl(EntityType.Mobile, feetBody, FEET_SIZE.PPM)
    heroFeet.setCollisionStrategy(DoNothingCollisionStrategy())

    hero.setFeet(heroFeet)
    EntitiesFactoryImpl.addEntity(heroFeet)
  }

  private def createEntityBody(size: (Float, Float) = HERO_SIZE,
                               position: (Float, Float) = HERO_POSITION): EntityBody =
    defineEntityBody(BodyType.DynamicBody, EntityCollisionBit.Hero,
    HERO_COLLISIONS, createPolygonalShape(size.PPM), position.PPM, friction = HERO_FRICTION)
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
        Hero.changeHeroSize(this, HERO_SIZE)
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

  override def setLittle(little: Boolean): Unit = {
    this.little = little
    this.stopHero(SHORT_WAIT_TIME)
  }

  override def isLittle: Boolean = this.little

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

  override def setFeet(feet: MobileEntity): Unit = this.feet = Option.apply(feet)

  override def getFeet: Option[MobileEntity] = this.feet

  override def setAirAttacking(isAttacking: Boolean): Unit = this.isAirAttacking = isAttacking

  override def restoreNormalMovementStrategy(): Unit = {
    this.setMovementStrategy(new HeroMovements(this, this.getStatistic(MovementSpeed).get))
    this.getEntityBody.setGravityScale()
    if(this.checkRestore)
      this.setState(State.Falling)
    this.getBody.setAwake(true)
  }

  private def isNotWaiting: Boolean = this.waitTimer <= 0
  private def decrementWaiting(value: Float): Unit = this.waitTimer -= value

  private def isFalling: Boolean = !this.isTouchingGround && this.entityBody.getBody.getLinearVelocity.y < 0
  private def isMovingHorizontally: Boolean = this.entityBody.getBody.getLinearVelocity.x != 0 && this.entityBody.getBody.getLinearVelocity.y == 0
  private def isIdle = this.entityBody.getBody.getLinearVelocity.x == 0 && this.entityBody.getBody.getLinearVelocity.y == 0

  private def checkRestore: Boolean = (this isNot Jumping) && (this isNot Somersault) && (this isNot Sliding)
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

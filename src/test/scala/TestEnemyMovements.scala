import com.badlogic.gdx.math.Vector2
import model.LevelImpl
import model.attack.DoNothingAttackStrategy
import model.behaviour.{EnemyBehavioursImpl, GroundEnemyMovementStrategy, PatrolMovementStrategy}
import model.collisions.DoNothingCollisionStrategy
import model.collisions.ImplicitConversions._
import model.entities.Enemy.createSkeletonEnemy
import model.entities._
import model.helpers.{EntitiesContainerMonitor, EntitiesFactoryImpl, ItemPoolImpl}
import model.movement.{DoNothingMovementStrategy, FaceTarget, MovementStrategy}
import org.scalatest.flatspec.AnyFlatSpec

class TestEnemyMovements extends AnyFlatSpec {
  private val PLATFORM_SIZE: (Float, Float) = (100, 10)
  private val PLATFORM_POSITION: (Float, Float) = (0, 0)

  private val WALL_SIZE: (Float, Float) = (10, 100)
  private val WALL_POSITION: (Float, Float) = (100, 0)

  private val ENEMY_POSITION: (Float, Float) = (0, 10)
  private val ENEMY_POSITION_NEAR_HERO: (Float, Float) = (0, 40)
  private val ENEMY_POSITION_NEAR_WALL: (Float, Float) = (77, 10)
  private val ENEMY_POSITION_NEAR_EDGE: (Float, Float) = (-100, 10)

  private val HERO_POSITION: (Float, Float) = (0, 20)

  private var floor: Entity = _
  private var rightWall: Entity = _

  private var enemy: EnemyImpl = _
  private var hero: Hero = _
  private var entitiesContainer: EntitiesContainerMonitor = _
  private var level: LevelImpl = _

  private def setEnemyMovementStrategy(movementStrategy: MovementStrategy): Unit = {
    // set a custom behaviour for the skeleton type enemy (he can only move)
    val behaviour: EnemyBehavioursImpl = new EnemyBehavioursImpl()
    behaviour.addBehaviour(DoNothingCollisionStrategy(), movementStrategy, DoNothingAttackStrategy())
    this.enemy.setBehaviour(behaviour)
  }

  private def initialize(enemyPosition: (Float, Float)): Unit = {
    entitiesContainer = new EntitiesContainerMonitor
    EntitiesFactoryImpl.setEntitiesContainerMonitor(entitiesContainer)

    this.level = new LevelImpl(null, entitiesContainer, new ItemPoolImpl())
    this.hero = entitiesContainer.getHero.get
    this.enemy = createSkeletonEnemy(enemyPosition)

    this.floor = Platform(PLATFORM_POSITION, PLATFORM_SIZE)
    this.rightWall = EntitiesFactoryImpl.createImmobileEntity(size=WALL_SIZE, position=WALL_POSITION)
  }

  // testing movement primitive policies not characterized by a complex behaviour but
  // a tangible movement task

  "An enemy" should "be standing" in {
    this.initialize(ENEMY_POSITION)
    this.setEnemyMovementStrategy(DoNothingMovementStrategy())

    this.enemy.setFacing(right=true)
    assert(this.enemy.isFacingRight)
    assertResult(State.Standing)(this.enemy.getState)

    this.enemy.update()

    assert(this.enemy.isFacingRight)
    assertResult(State.Standing)(this.enemy.getState)
  }

  "An enemy" should "move to the right" in {
    this.initialize(ENEMY_POSITION)
    this.setEnemyMovementStrategy(PatrolMovementStrategy(this.enemy))

    this.enemy.setFacing(right=true)

    this.enemy.update()

    assert(this.enemy.isFacingRight)
    assertResult(State.Running)(this.enemy.getState)
  }

  "An enemy" should "move unless obstructed" in {
    // position the enemy near a wall
    this.initialize(ENEMY_POSITION_NEAR_WALL)

    this.enemy.setFacing(right=true)
    this.setEnemyMovementStrategy(PatrolMovementStrategy(this.enemy))

    // two updates required: the first one changes the movement inner policy (from "stationary" to "active")
    // the second one executes the new movement, changing the enemy facing direction
    this.enemy.update()
    this.enemy.update()

    assert(!this.enemy.isFacingRight)
    assertResult(State.Running)(this.enemy.getState)
  }

  "An enemy" should "change direction if he is walking towards the end of the floor" in {
    // position the enemy near the floor edge
    this.initialize(ENEMY_POSITION_NEAR_EDGE)

    this.enemy.setFacing(right=false)
    this.setEnemyMovementStrategy(PatrolMovementStrategy(this.enemy))

    // two updates required: the first one changes the movement inner policy (from "stationary" to "active")
    // the second one executes the new movement, changing the enemy facing direction
    this.enemy.update()
    this.enemy.update()

    assert(this.enemy.isFacingRight)
    assertResult(this.enemy.getState)(State.Running)
  }

  "An enemy" should "face the hero" in {
    this.initialize(ENEMY_POSITION)
    this.setEnemyMovementStrategy(FaceTarget(this.enemy, this.hero))

    this.enemy.setFacing(right=false)

    this.enemy.update()

    assert(this.enemy.isFacingRight)
    assertResult(this.enemy.getState)(State.Standing)
  }

  // testing complex movement behaviours composed by different primitives strategies

  "An enemy" should "stop patrolling if facing the hero" in {
    this.initialize(ENEMY_POSITION)

    this.hero.setPosition(HERO_POSITION.PPM)
    this.enemy.setFacing(right=true)

    this.setEnemyMovementStrategy(GroundEnemyMovementStrategy(this.enemy, this.hero))

    this.enemy.update()

    assertResult(this.enemy.getState)(State.Standing)
    assert(this.enemy.getBody.getLinearVelocity.len()==0)
  }

  "An enemy" should "change direction and face the hero" in {
    this.initialize(ENEMY_POSITION)

    this.hero.setPosition(HERO_POSITION.PPM)
    this.enemy.setFacing(right=false)

    this.setEnemyMovementStrategy(GroundEnemyMovementStrategy(this.enemy, this.hero))

    this.enemy.update()

    assert(this.enemy.isFacingRight)
    assertResult(this.enemy.getState)(State.Standing)
  }

  "An enemy" should "stop if near the hero" in {
    this.initialize(ENEMY_POSITION)

    this.hero.setPosition(HERO_POSITION.PPM)
    this.enemy.setFacing(right=false)

    this.setEnemyMovementStrategy(GroundEnemyMovementStrategy(this.enemy, this.hero))
    this.enemy.getBody.setLinearVelocity(new Vector2(1, 0))

    this.enemy.update()

    assertResult(this.enemy.getState)(State.Standing)
    assert(this.enemy.getBody.getLinearVelocity.len()==0)
  }

  "An enemy" should "move toward the hero" in {
    this.initialize(ENEMY_POSITION_NEAR_HERO)

    this.hero.setPosition(HERO_POSITION.PPM)
    this.enemy.setFacing(right=true)

    this.setEnemyMovementStrategy(GroundEnemyMovementStrategy(this.enemy, this.hero))

    // two updates required: the first one changes the movement inner policy (from "patrol" to "chase")
    // the second one executes the new movement, changing the enemy facing direction and speed
    this.enemy.update()
    this.enemy.update()

    assertResult(State.Running)(this.enemy.getState)
    assert(!this.enemy.isFacingRight)
    assert(this.enemy.getBody.getLinearVelocity.len()!=0)
  }
}

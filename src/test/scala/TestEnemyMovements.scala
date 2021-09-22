import com.badlogic.gdx.math.Vector2
import model.collisions.EntityCollisionBit
import model.collisions.ImplicitConversions._
import model.entities.Statistic.Statistic
import model.entities._
import model.helpers.EntitiesFactoryImpl.createEnemyEntity
import model.helpers.{EntitiesContainerMonitor, EntitiesFactoryImpl}
import model.movement.{DoNothingMovementStrategy, FaceTarget, PatrolAndStop, PatrolPlatform}
import model.{Level, LevelImpl}
import org.scalatest.flatspec.AnyFlatSpec

class TestEnemyMovements extends AnyFlatSpec {
  private val PLATFORM_SIZE: (Float, Float) = (100, 10)
  private val PLATFORM_POSITION: (Float, Float) = (0, 0)
  private val PLATFORM_COLLISIONS: Short = EntityCollisionBit.Hero | EntityCollisionBit.Enemy

  private val WALL_SIZE: (Float, Float) = (10, 100)
  private val WALL_POSITION: (Float, Float) = (100, 0)

  private val ENEMY_POSITION: (Float, Float) = (0, 10)
  private val ENEMY_POSITION_NEAR_WALL: (Float, Float) = (60, 10)
  private val ENEMY_POSITION_NEAR_EDGE: (Float, Float) = (-100, 10)
  private val ENEMY_SIZE: (Float, Float) = (20, 20)

  private val HERO_POSITION: (Float, Float) = (0, 30)

  private val LEVEL_NUMBER: Int = 0
  private val ENEMY_SCORE: Int = 0

  private val ENEMY_STATISTICS: Map[Statistic, Float] = Map(
    Statistic.MaxMovementSpeed -> 40f.PPM,
    Statistic.Acceleration -> 5f.PPM,
    Statistic.VisionDistance -> 50f.PPM,
    Statistic.VisionAngle -> 90,
  )

  private var level: Level = _
  private var enemy: LivingEntity = _
  private var hero: Hero = _
  private var platform: Entity = _
  private var rightWall: Entity = _

  private def createStandingEnemy(position:(Float, Float)): Unit = {
    this.enemy = createEnemyEntity(position, ENEMY_SIZE, ENEMY_STATISTICS, Map(), LEVEL_NUMBER, ENEMY_SCORE, EntityType.Enemy)
    this.enemy.setMovementStrategy(new DoNothingMovementStrategy)
  }

  private def createFacingEnemy(position:(Float, Float)): Unit = {
    this.enemy = createEnemyEntity(position, ENEMY_SIZE, ENEMY_STATISTICS, Map(), LEVEL_NUMBER, ENEMY_SCORE, EntityType.Enemy)
    this.enemy.setMovementStrategy(new FaceTarget(this.enemy, this.hero))
  }

  private def createPatrollingEnemy(position:(Float, Float)): Unit = {
    this.enemy = createEnemyEntity(position, ENEMY_SIZE, ENEMY_STATISTICS, Map(), LEVEL_NUMBER, ENEMY_SCORE, EntityType.Enemy)
    this.enemy.setMovementStrategy(new PatrolPlatform(this.enemy))
  }

  private def createPatrolAndStopEnemy(position:(Float, Float)): Unit = {
    this.enemy = createEnemyEntity(position, ENEMY_SIZE, ENEMY_STATISTICS, Map(), LEVEL_NUMBER, ENEMY_SCORE, EntityType.Enemy)
    this.enemy.setMovementStrategy(new PatrolAndStop(this.enemy, this.hero))
  }

  private def createChasingEnemy(position:(Float, Float)): Unit = {
    this.enemy = createEnemyEntity(position, ENEMY_SIZE, ENEMY_STATISTICS, Map(), LEVEL_NUMBER, ENEMY_SCORE, EntityType.Enemy)
    this.enemy.setMovementStrategy(new DoNothingMovementStrategy)
  }

  private def initialize(): Unit = {
    val entitiesSetter:EntitiesContainerMonitor = new EntitiesContainerMonitor
    this.level = new LevelImpl(null, entitiesSetter)

    hero = this.level.getEntity(e => e.isInstanceOf[HeroImpl]).asInstanceOf[HeroImpl]

    this.platform = EntitiesFactoryImpl.createImmobileEntity(size=PLATFORM_SIZE,
      position=PLATFORM_POSITION, collisions = PLATFORM_COLLISIONS)

    this.rightWall = EntitiesFactoryImpl.createImmobileEntity(size=WALL_SIZE,
      position=WALL_POSITION, collisions = PLATFORM_COLLISIONS)
  }

  "An enemy" should "be standing" in {
    this.initialize()
    this.createStandingEnemy(ENEMY_POSITION)

    this.enemy.setFacing(right=true)
    assert(this.enemy.isFacingRight)
    assertResult(this.enemy.getState)(State.Standing)

    this.enemy.update()

    assert(this.enemy.isFacingRight)
    assertResult(this.enemy.getState)(State.Standing)
  }

  "An enemy" should "move to the right" in {
    this.initialize()
    this.createPatrollingEnemy(ENEMY_POSITION)

    this.enemy.setFacing(right=true)

    this.enemy.update()

    assert(this.enemy.isFacingRight)
    assertResult(this.enemy.getState)(State.Running)
  }

  "An enemy" should "move unless obstructed" in {
    this.initialize()

    // create a wall exaclty adjacent to the enemy
    this.createPatrollingEnemy(ENEMY_POSITION_NEAR_WALL)

    this.enemy.setFacing(right=true)

    this.enemy.update()

    assert(!this.enemy.isFacingRight)
    assertResult(this.enemy.getState)(State.Running)
  }

  "An enemy" should "move until the platform end" in {
    this.initialize()

    // create a wall exaclty adjacent to the enemy
    this.createPatrollingEnemy(ENEMY_POSITION_NEAR_EDGE)

    this.enemy.setFacing(right=false)

    this.enemy.update()

    assert(this.enemy.isFacingRight)
    assertResult(this.enemy.getState)(State.Running)
  }

  "An enemy" should "face the hero" in {
    this.initialize()

    this.createFacingEnemy(ENEMY_POSITION)

    this.enemy.setFacing(right=false)

    this.enemy.update()

    assert(this.enemy.isFacingRight)
    assertResult(this.enemy.getState)(State.Standing)
  }

  "An enemy" should "stop patrolling if facing the hero" in {
    this.initialize()

    this.createPatrolAndStopEnemy(ENEMY_POSITION)
    this.enemy.getBody.setLinearVelocity(new Vector2(1, 0))
    this.enemy.setFacing(right=true)

    this.enemy.update()

    assertResult(this.enemy.getState)(State.Standing)
    assert(this.enemy.getBody.getLinearVelocity.len()==0)
  }

  "An enemy" should "change direction and face the hero" in {
    this.initialize()

    this.createPatrolAndStopEnemy(ENEMY_POSITION)
    this.enemy.setFacing(right=false)

    this.enemy.update()

    assert(this.enemy.isFacingRight)
    assertResult(this.enemy.getState)(State.Standing)
  }

  "An enemy" should "stop if near the hero" in {
    this.initialize()

    this.createPatrolAndStopEnemy(ENEMY_POSITION)
    this.enemy.getBody.setLinearVelocity(new Vector2(1, 0))

    this.enemy.update()

    assertResult(this.enemy.getState)(State.Standing)
    assert(this.enemy.getBody.getLinearVelocity().len()==0)

  }

  "An enemy" should "move toward the hero" in {

  }

  "An enemy" should "be moving if far from the hero" in {

  }

  "An enemy" should "be moving whenever the hero is not around" in {

  }

}

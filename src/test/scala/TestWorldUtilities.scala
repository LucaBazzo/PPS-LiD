import model.collisions.ImplicitConversions.{entityToBody, _}
import model.entities.Enemy.{createSkeletonEnemy, createWormEnemy}
import model.entities.{Entity, LivingEntity}
import model.helpers.EntitiesFactoryImpl.{createImmobileEntity, createPlatform}
import model.helpers.GeometricUtilities.{isBodyAbove, isBodyBelow, isBodyOnTheLeft, isBodyOnTheRight}
import model.helpers.WorldUtilities.{canBodiesCollide, isBodyVisible}
import model.helpers.{EntitiesContainerMonitor, EntitiesFactoryImpl, ItemPoolImpl}
import model.{Level, LevelImpl}
import org.scalatest.flatspec.AnyFlatSpec

class TestWorldUtilities extends AnyFlatSpec {

  val VISION_DISTANCE_NEAR: Float = 10.PPM
  val VISION_DISTANCE_FAR: Float = 10.PPM
  val VISION_ANGLE_NARROW: Int = 10
  val VISION_ANGLE_WIDE: Int = 90
  val PLATFORM_SIZE: (Float, Float) = (100, 10)
  val PLATFORM_POSITION: (Float, Float) = (0, 0)

  val WALL_SIZE: (Float, Float) = (10, 100)
  val WALL_POSITION: (Float, Float) = (0, 0)

  val WORM_POSITION: (Float, Float) = (0, 0)
  val SKELETON_POSITION: (Float, Float) = (0, 50)

  var entitiesContainer: EntitiesContainerMonitor = new EntitiesContainerMonitor
  EntitiesFactoryImpl.setEntitiesContainerMonitor(entitiesContainer)
  var level: Level = new LevelImpl(null, entitiesContainer, new ItemPoolImpl())
  var enemy1: LivingEntity = createSkeletonEnemy(SKELETON_POSITION)
  var enemy2: LivingEntity = createWormEnemy(WORM_POSITION)
  var hero: LivingEntity = entitiesContainer.getHero.get
  var floor: Entity = EntitiesFactoryImpl.createPlatform(PLATFORM_POSITION, PLATFORM_SIZE)

  "An entity" should "see another entity if not obstructed" in {
    createPlatform(PLATFORM_POSITION, PLATFORM_SIZE)

    enemy1.setPosition(-30.PPM, 20.PPM)
    enemy2.setPosition(+30.PPM, 20.PPM)

    assert(isBodyVisible(enemy1, enemy2))

    // create wall between the two entities
    createImmobileEntity(position=WALL_POSITION, size=WALL_SIZE)

    assert(!isBodyVisible(enemy1, enemy2))
  }

  "An entity" should "see another entity inside his field of view" in {
    createPlatform(PLATFORM_POSITION, PLATFORM_SIZE)

    enemy1.setPosition(0, 0)
    enemy2.setPosition(0, 30.PPM)
    assert(!isBodyVisible(enemy1, enemy2, VISION_ANGLE_NARROW))


    enemy1.setPosition(0, 0)
    enemy2.setPosition(30.PPM, 0.PPM)
    assert(isBodyVisible(enemy1, enemy2, VISION_ANGLE_WIDE))

  }

  "An entity" should "be on the left/right of another entity" in {
    createPlatform(PLATFORM_POSITION, PLATFORM_SIZE)

    enemy1.setPosition(0, 0)
    enemy2.setPosition(10, 0.PPM)
    assert(isBodyOnTheRight(enemy1, enemy2))
    assert(!isBodyOnTheLeft(enemy1, enemy2))

    enemy2.setPosition(-10.PPM, 0.PPM)
    assert(!isBodyOnTheRight(enemy1, enemy2))
    assert(isBodyOnTheLeft(enemy1, enemy2))

    // to prevent ambiguous states, overlapping entities are both at each over left and right side
    enemy2.setPosition(0.PPM, 0.PPM)
    assert(isBodyOnTheRight(enemy1, enemy2))
    assert(isBodyOnTheLeft(enemy1, enemy2))
  }

  "An entity" should "be above/below another entity" in {
    createPlatform(PLATFORM_POSITION, PLATFORM_SIZE)

    enemy1.setPosition(0, 0)
    enemy2.setPosition(0, 10.PPM)
    assert(isBodyAbove(enemy1, enemy2))
    assert(!isBodyBelow(enemy1, enemy2))

    enemy2.setPosition(0.PPM, -10.PPM)
    assert(!isBodyAbove(enemy1, enemy2))
    assert(isBodyBelow(enemy1, enemy2))

    // to prevent ambiguous states, overlapping entities are respectively above and below each other
    enemy2.setPosition(0.PPM, 0.PPM)
    assert(isBodyAbove(enemy1, enemy2))
    assert(isBodyBelow(enemy1, enemy2))
  }

  "An entity" should "collide with another entity" in {
    // living entities bodies should not collide (they can overlap)
    assert(!canBodiesCollide(enemy1, enemy2))
    assert(!canBodiesCollide(enemy1, hero))

    assert(canBodiesCollide(floor, enemy2))
    assert(canBodiesCollide(floor, hero))
  }
}

import controller.ModelResources
import model.entity._
import model.helpers.EntitiesFactoryImpl.createImmobileEntity
import model.helpers.GeometricUtilities.{isBodyAbove, isBodyBelow, isBodyOnTheLeft, isBodyOnTheRight}
import model.helpers.ImplicitConversions.{RichInt, RichWorld, entityToBody}
import model.helpers.{EntitiesFactoryImpl, ItemPoolImpl}
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

  var entitiesContainer: ModelResources = new ModelResources
  EntitiesFactoryImpl.setEntitiesContainerMonitor(entitiesContainer)
  var level: Level = new LevelImpl(null, entitiesContainer, new ItemPoolImpl())
  var enemy1: LivingEntity = SkeletonEnemy(SKELETON_POSITION)
  var enemy2: LivingEntity = WormEnemy(WORM_POSITION)
  var hero: LivingEntity = entitiesContainer.getHero.get
  var floor: Entity = Platform(PLATFORM_POSITION, PLATFORM_SIZE)

  "An entity" should "see another entity if not obstructed" in {
    Platform(PLATFORM_POSITION, PLATFORM_SIZE)

    enemy1.setPosition(-30.PPM, 20.PPM)
    enemy2.setPosition(+30.PPM, 20.PPM)

    assert(entitiesContainer.getWorld.get.isBodyVisible(enemy1, enemy2))

    // create wall between the two entities
    createImmobileEntity(position=WALL_POSITION, size=WALL_SIZE)

    assert(!entitiesContainer.getWorld.get.isBodyVisible(enemy1, enemy2))
  }

  "An entity" should "see another entity inside his field of view" in {
    Platform(PLATFORM_POSITION, PLATFORM_SIZE)

    enemy1.setPosition(0, 0)
    enemy2.setPosition(0, 30.PPM)
    assert(!entitiesContainer.getWorld.get.isBodyVisible(enemy1, enemy2, VISION_ANGLE_NARROW))


    enemy1.setPosition(0, 0)
    enemy2.setPosition(30.PPM, 0.PPM)
    assert(entitiesContainer.getWorld.get.isBodyVisible(enemy1, enemy2, VISION_ANGLE_WIDE))

  }

  "An entity" should "be on the left/right of another entity" in {
    Platform(PLATFORM_POSITION, PLATFORM_SIZE)

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
    Platform(PLATFORM_POSITION, PLATFORM_SIZE)

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
    assert(!entitiesContainer.getWorld.get.canBodiesCollide(enemy1, enemy2))
    assert(!entitiesContainer.getWorld.get.canBodiesCollide(enemy1, hero))

    assert(entitiesContainer.getWorld.get.canBodiesCollide(floor, enemy2))
    assert(entitiesContainer.getWorld.get.canBodiesCollide(floor, hero))
  }
}

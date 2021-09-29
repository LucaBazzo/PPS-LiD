import controller.ModelResources
import model.entity._
import model.helpers.EntitiesFactoryImpl.{getEntitiesContainerMonitor, getItemPool}
import model.helpers.{EntitiesFactoryImpl, ItemPoolImpl, ItemPools}
import model.{Level, LevelImpl}
import org.scalatest.flatspec.AnyFlatSpec

class TestEnemy extends AnyFlatSpec {

  var enemy: LivingEntity = _
  var entitiesContainer: ModelResources = _
  var level: Level = _

  private def initialize(): Unit = {
    entitiesContainer = new ModelResources
    EntitiesFactoryImpl.setEntitiesContainerMonitor(entitiesContainer)

    level = new LevelImpl(null, entitiesContainer, new ItemPoolImpl())
    enemy = SkeletonEnemy((0, 0))
  }

  "An enemy" should "be able to suffer damage" in {
    initialize()

    enemy.sufferDamage(10)

    assertResult(enemy.getLife)(190f)
    assertResult(enemy.getState)(State.Hurt)
  }

  "An enemy" should "not be able to suffer positive damage" in {
    initialize()

    enemy.sufferDamage(-10)

    assertResult(enemy.getLife)(200f)
    assertResult(enemy.getState)(State.Standing)
  }

  "An enemy" should "die " in {
    initialize()

    enemy.sufferDamage(200)

    assertResult(enemy.getLife)(0)
    assertResult(enemy.getState)(State.Dying)
  }

  "An enemy death" should "increase the game score" in {
    initialize()

    assertResult(entitiesContainer.getScore)(0)

    enemy.destroyEntity()

    assertResult(entitiesContainer.getScore)(100)
  }

  "An enemy" should "disappear when killed" in {
    initialize()

    assert(entitiesContainer.getEntities(x => x.isInstanceOf[Enemy]).nonEmpty)

    enemy.destroyEntity()

    assert(entitiesContainer.getEntities(x => x.isInstanceOf[Enemy]).isEmpty)
  }

  "An enemy boss" should "drop a bow" in {
    initialize()

    val bossEnemy: EnemyImpl = WizardEnemy((0, 0))
    Portal((0, 0)) // needed to allow portal activation on boss enemy death

    assert(entitiesContainer.getEntities(x => x.isInstanceOf[Enemy]).size == 2)
    assert(entitiesContainer.getEntities(x => x.isInstanceOf[Item]).isEmpty)

    bossEnemy.destroyEntity()

    assert(entitiesContainer.getEntities(x => x.isInstanceOf[Enemy]).size == 1)
    assert(entitiesContainer.getEntities(x => x.isInstanceOf[Item]).nonEmpty)
    assert(entitiesContainer.getEntities(x => x.isInstanceOf[Item]).head.getType equals EntityType.BowItem)
  }

  "An enemy boss" can "drop items other than a bow" in {
    initialize()

    // give a bow item to the hero
    Item(ItemPools.Boss, getItemPool, getEntitiesContainerMonitor,
      position=(0, 0))
    entitiesContainer.getEntity(e => e.isInstanceOf[Item]).get.asInstanceOf[Item].collect()

    val bossEnemy: EnemyImpl = WizardEnemy((0, 0))
    Portal((0, 0)) // needed to allow portal activation on boss enemy death

    bossEnemy.destroyEntity()

    assert(!(entitiesContainer.getEntities(x => x.isInstanceOf[Item]).head.getType equals EntityType.BowItem))
  }
}

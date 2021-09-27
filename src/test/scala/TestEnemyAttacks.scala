import model.helpers.ImplicitConversions._
import model.entities._
import model.helpers.{EntitiesContainerMonitor, EntitiesFactoryImpl, ItemPoolImpl}
import model.{Level, LevelImpl}
import org.scalatest.flatspec.AnyFlatSpec

class TestEnemyAttacks extends AnyFlatSpec {

  var enemy: EnemyImpl = _
  var hero: Hero = _
  var entitiesContainer: EntitiesContainerMonitor = _
  var level: Level = _

  private def initialize(): Unit = {
    entitiesContainer = new EntitiesContainerMonitor
    EntitiesFactoryImpl.setEntitiesContainerMonitor(entitiesContainer)

    level = new LevelImpl(null, entitiesContainer, new ItemPoolImpl())

    val position = entitiesContainer.getHero.get.getPosition
    enemy = SkeletonEnemy(position.MPP)

    hero = entitiesContainer.getHero.get
  }

  private def getAttackEntities: List[MobileEntity] =
    entitiesContainer.getEntities(e => e.getType.equals(EntityType.Mobile)).asInstanceOf[List[MobileEntity]]

  "An enemy" should "create an attack entity" in {
    initialize()

    val mobileEntitiesCount: Int = entitiesContainer.getEntities(e => e.getType.equals(EntityType.Mobile)).size

    enemy.update()

    assertResult(enemy.getState)(State.Attack01)
    assert(entitiesContainer.getEntities(e => e.getType.equals(EntityType.Mobile)).size == mobileEntitiesCount + 1)
  }

  "An enemy attack" should "be able to damage a living entity" in {
    initialize()

    val attackEntities: List[Entity] = getAttackEntities

    // an attack entity is created since the enemy's target (the hero) is in striking range
    enemy.update()
    val attackEntity:MobileEntity = (getAttackEntities diff attackEntities).head
    attackEntity.collisionDetected(hero)

    // the hero should suffer damages equal to the enemy strength statistic
    assertResult(hero.getStatistic(Statistic.Health).get)(hero.getLife + attackEntity.getStatistic(Statistic.Strength).get)
  }

  "An enemy" should "stop attacking if dying" in {
    initialize()

    // an attack entity is created since the enemy's target (the hero) is in striking range
    enemy.update()
    val attackEntities: List[Entity] = getAttackEntities

    // the enemy suffer lethal damage
    enemy.sufferDamage(enemy.getStatistic(Statistic.CurrentHealth).get)

    // the enemy attack entity is prematurely removed since the enemy is dying
    assertResult(enemy.getState)(State.Dying)
    assertResult(attackEntities.size - 1)(getAttackEntities.size)
  }

  "An enemy" should " not change facing direction while attacking" in {
    initialize()

    // position the hero near the enemy (on the right)
    hero.setPosition(enemy.getPosition._1 + 10.PPM, enemy.getPosition._2)
    enemy.update()

    assertResult(State.Attack01)(enemy.getState)
    assert(enemy.isFacingRight)

    // position the hero on the over side. The enemy should not change facing direction while attacking
    hero.setPosition(enemy.getPosition._1 - 10.PPM, enemy.getPosition._2)

    enemy.update()

    assertResult(State.Attack01)(enemy.getState)
    assert(enemy.isFacingRight)

  }
}

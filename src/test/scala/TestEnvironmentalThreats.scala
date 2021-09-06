import model.{Level, LevelImpl}
import model.entities.{Entity, EntityType, Hero, Statistic}
import model.helpers.EntitiesContainerMonitor
import org.scalatest.flatspec.AnyFlatSpec

class TestEnvironmentalThreats extends AnyFlatSpec {

  "The Hero" should "be slowed while inside a water pool" in {
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    val level: Level = new LevelImpl(monitor)
    val pool: Entity = monitor.getEntities(x => x.getType == EntityType.Water).get.head
    val hero: Hero = monitor.getEntities(x => x.getType == EntityType.Hero).get.head.asInstanceOf[Hero]
    val prevMoveSpeed: Float = hero.getStatistic(Statistic.MovementSpeed).get
    pool.collisionDetected(Option.apply(hero))
    level.updateEntities(List.empty)
    assert(hero.getStatistic(Statistic.MovementSpeed).get < prevMoveSpeed )
  }

  "The Hero" should "regain its previous movement speed after exiting a water pool" in {
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    val level: Level = new LevelImpl(monitor)
    val pool: Entity = monitor.getEntities(x => x.getType == EntityType.Water).get.head
    val hero: Hero = monitor.getEntities(x => x.getType == EntityType.Hero).get.head.asInstanceOf[Hero]
    val prevMoveSpeed: Float = hero.getStatistic(Statistic.MovementSpeed).get
    pool.collisionDetected(Option.apply(hero))
    level.updateEntities(List.empty)
    pool.collisionReleased(Option.apply(hero))
    assert(hero.getStatistic(Statistic.MovementSpeed).get == prevMoveSpeed )
  }

  "The Hero" should "periodically suffer damage while inside a lava pool" in {
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    val level: Level = new LevelImpl(monitor)
    val pool: Entity = monitor.getEntities(x => x.getType == EntityType.Lava).get.head
    val hero: Hero = monitor.getEntities(x => x.getType == EntityType.Hero).get.head.asInstanceOf[Hero]
    val prevHP: Float = hero.getStatistic(Statistic.CurrentHealth).get
    pool.collisionDetected(Option.apply(hero))
    level.updateEntities(List.empty)
    Thread.sleep(1000)
    val midHP: Float = hero.getStatistic(Statistic.CurrentHealth).get
    assert(midHP < prevHP)
    Thread.sleep(1000)
    pool.collisionReleased(Option.apply(hero))
    assert(hero.getStatistic(Statistic.CurrentHealth).get < midHP)
  }

  "The Hero" must "stop suffering damage after exiting a lava pool" in {
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    val level: Level = new LevelImpl(monitor)
    val pool: Entity = monitor.getEntities(x => x.getType == EntityType.Lava).get.head
    val hero: Hero = monitor.getEntities(x => x.getType == EntityType.Hero).get.head.asInstanceOf[Hero]
    val prevHP: Float = hero.getStatistic(Statistic.CurrentHealth).get
    pool.collisionDetected(Option.apply(hero))
    level.updateEntities(List.empty)
    Thread.sleep(1500)
    val midHP: Float = hero.getStatistic(Statistic.CurrentHealth).get
    assert(midHP < prevHP)
    pool.collisionReleased(Option.apply(hero))
    val finHP: Float = hero.getStatistic(Statistic.CurrentHealth).get
    assert(finHP <= midHP)
    Thread.sleep(1500)
    assert(hero.getStatistic(Statistic.CurrentHealth).get == finHP)
    Thread.sleep(1500)
    assert(hero.getStatistic(Statistic.CurrentHealth).get == finHP)
  }

}

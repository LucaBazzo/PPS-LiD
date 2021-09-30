import controller.ModelResources
import model.LevelImpl
import model.entity._
import model.helpers.{EntitiesFactoryImpl, ItemPoolImpl}
import org.scalatest.flatspec.AnyFlatSpec

class TestEnvironmentalThreats extends AnyFlatSpec {

  "The Hero" should "be slowed while inside a water pool" in {
    val monitor: ModelResources = initialize()
    val pool: Entity = monitor.getEntities(x => x.getType == EntityType.Water).head
    val hero: Hero = monitor.getEntities(x => x.getType == EntityType.Hero).head.asInstanceOf[Hero]
    val prevMoveSpeed: Float = hero.getStatistic(Statistic.MovementSpeed).get
    pool.collisionDetected(hero)
    assert(hero.getStatistic(Statistic.MovementSpeed).get < prevMoveSpeed )
  }

  "The Hero" should "regain its previous movement speed after exiting a water pool" in {
    val monitor: ModelResources = initialize()
    val pool: Entity = monitor.getEntities(x => x.getType == EntityType.Water).head
    val hero: Hero = monitor.getEntities(x => x.getType == EntityType.Hero).head.asInstanceOf[Hero]
    val prevMoveSpeed: Float = hero.getStatistic(Statistic.MovementSpeed).get
    pool.collisionDetected(hero)
    pool.collisionReleased(hero)
    assert(hero.getStatistic(Statistic.MovementSpeed).get == prevMoveSpeed )
  }

  "The Hero" should "periodically suffer damage while inside a lava pool" in {
    val monitor: ModelResources = initialize()
    val pool: Entity = monitor.getEntities(x => x.getType == EntityType.Lava).head
    val hero: Hero = monitor.getEntities(x => x.getType == EntityType.Hero).head.asInstanceOf[Hero]
    val prevHP: Float = hero.getStatistic(Statistic.CurrentHealth).get
    pool.collisionDetected(hero)
    Thread.sleep(1000)
    pool.update()
    val midHP: Float = hero.getStatistic(Statistic.CurrentHealth).get
    assert(midHP < prevHP)
    Thread.sleep(1000)
    pool.update()
    pool.collisionReleased(hero)
    assert(hero.getStatistic(Statistic.CurrentHealth).get <= midHP)
  }

  "The Hero" must "stop suffering damage after exiting a lava pool" in {
    val monitor: ModelResources = initialize()
    val pool: Entity = monitor.getEntities(x => x.getType == EntityType.Lava).head
    val hero: Hero = monitor.getEntities(x => x.getType == EntityType.Hero).head.asInstanceOf[Hero]
    val prevHP: Float = hero.getStatistic(Statistic.CurrentHealth).get
    pool.collisionDetected(hero)
    Thread.sleep(1500)
    pool.update()
    val midHP: Float = hero.getStatistic(Statistic.CurrentHealth).get
    assert(midHP < prevHP)
    pool.collisionReleased(hero)
    val finHP: Float = hero.getStatistic(Statistic.CurrentHealth).get
    assert(finHP <= midHP)
    Thread.sleep(1500)
    pool.update()
    assert(hero.getStatistic(Statistic.CurrentHealth).get == finHP)
    Thread.sleep(1500)
    pool.update()
    assert(hero.getStatistic(Statistic.CurrentHealth).get == finHP)
  }

  private def initialize(): ModelResources = {
    val monitor: ModelResources = new ModelResources
    EntitiesFactoryImpl.setEntitiesContainerMonitor(monitor)
    //TODO null temporaneo
    new LevelImpl(null, monitor, new ItemPoolImpl())
    LavaPool((10,10), (100,10))
    WaterPool((10,10), (100,10))
    monitor
  }

}

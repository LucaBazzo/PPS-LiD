import model.LevelImpl
import model.entities.Chest
import model.helpers.{EntitiesContainerMonitor, EntitiesFactoryImpl, ItemPoolImpl}
import org.scalatest.flatspec.AnyFlatSpec

class TestChest extends AnyFlatSpec {
  /*"A chest" should "open when the hero interact with it" in {
    val monitor: EntitiesContainerMonitor = this.initialize()
    val chest: ImmobileEntity = monitor.getEntities(x => x.isInstanceOf[ImmobileEntity] && x.getType == EntityType.Chest).get.head.asInstanceOf[ImmobileEntity]
    val hero: Hero = monitor.getEntities(x => x.isInstanceOf[Hero]).get.head.asInstanceOf[Hero]
    chest.collisionDetected(hero)
    hero.notifyCommand(GameEvent.Interaction)
    assert(monitor.getEntities(x => x.isInstanceOf[ImmobileEntity] && x.getType == EntityType.Chest && x.getState == State.Opening).get.nonEmpty)
  }*/

  /*"A chest" should "spawn an item when opened" in {
    val monitor: EntitiesContainerMonitor = this.initialize()
    val chest: ImmobileEntity = monitor.getEntities(x => x.isInstanceOf[ImmobileEntity] && x.getType == EntityType.Chest).get.head.asInstanceOf[ImmobileEntity]
    val hero: Hero = monitor.getEntities(x => x.isInstanceOf[Hero]).get.head.asInstanceOf[Hero]
    chest.collisionDetected(hero)
    hero.notifyCommand(GameEvent.Interaction)
    EntitiesFactoryImpl.applyPendingFunctions()
    assert(monitor.getEntities(x => x.getType == EntityType.PotionItem || x.getType == EntityType.SmallPotionItem ||
      x.getType == EntityType.LargePotionItem || x.getType == EntityType.HugePotionItem).get.nonEmpty)
  }*/

  private def initialize(): EntitiesContainerMonitor = {
    val monitor: EntitiesContainerMonitor = new EntitiesContainerMonitor
    EntitiesFactoryImpl.setEntitiesContainerMonitor(monitor)
    //TODO null temporaneo
    new LevelImpl(null, monitor, new ItemPoolImpl())
    Chest((10,10), (480,150))
    monitor
  }
}

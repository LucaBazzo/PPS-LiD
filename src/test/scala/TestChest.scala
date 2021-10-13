import controller.{GameEvent, ModelResources}
import model.LevelImpl
import model.entity.{Chest, EntityType, Hero, ImmobileEntity, State}
import model.helpers.{EntitiesFactoryImpl, ItemPoolImpl}
import org.scalatest.flatspec.AnyFlatSpec

class TestChest extends AnyFlatSpec {
  class TestChest extends AnyFlatSpec {
    "A chest" should "open when the hero interact with it" in {
      val monitor: ModelResources = this.initialize()
      val chest: ImmobileEntity = monitor.getEntities(x => x.isInstanceOf[ImmobileEntity] && x.getType == EntityType.Chest).head.asInstanceOf[ImmobileEntity]
      val hero: Hero = monitor.getEntities(x => x.isInstanceOf[Hero]).head.asInstanceOf[Hero]
      chest.collisionDetected(hero)
      hero.notifyCommand(GameEvent.Interaction)
      assert(monitor.getEntities(x => x.isInstanceOf[ImmobileEntity] && x.getType == EntityType.Chest && x.getState == State.Opening).nonEmpty)
    }

    "A chest" should "spawn an item when opened" in {
      val monitor: ModelResources = this.initialize()
      val chest: ImmobileEntity = monitor.getEntities(x => x.isInstanceOf[ImmobileEntity] && x.getType == EntityType.Chest).head.asInstanceOf[ImmobileEntity]
      val hero: Hero = monitor.getEntities(x => x.isInstanceOf[Hero]).head.asInstanceOf[Hero]
      chest.collisionDetected(hero)
      hero.notifyCommand(GameEvent.Interaction)
      EntitiesFactoryImpl.applyPendingFunctions()
      assert(monitor.getEntities(x =>
        x.getType == EntityType.PotionItem || x.getType == EntityType.SmallPotionItem ||
        x.getType == EntityType.LargePotionItem || x.getType == EntityType.HugePotionItem).nonEmpty)
    }

    private def initialize(): ModelResources = {
      val monitor: ModelResources = new ModelResources
      EntitiesFactoryImpl.setEntitiesContainerMonitor(monitor)
      new LevelImpl(null, monitor, new ItemPoolImpl())
      Chest((10,10), (480,150))
      monitor
    }
  }
}

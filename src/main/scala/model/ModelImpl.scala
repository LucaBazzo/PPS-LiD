package model

import com.badlogic.gdx.physics.box2d.World
import controller.GameEvent.GameEvent
import model.entities.HeroImpl
import utils.ApplicationConstants.{GRAVITY_FORCE, POSITION_ITERATIONS, TIME_STEP, VELOCITY_ITERATIONS}
import view.screens.helpers.WorldCreator

trait Model {

  def update(actions: List[GameEvent])
}

class ModelImpl(private val entitiesSetter: EntitiesSetter) extends Model {

  private val world: World = new World(GRAVITY_FORCE, true)
  private val entitiesFactory: EntitiesFactory = new EntitiesFactoryImpl(world)

  private val player: HeroImpl = this.entitiesFactory.createHeroEntity()
  this.entitiesSetter.setEntities(List(player))

  new WorldCreator(this.world)

  this.entitiesSetter.setWorld(this.world)

  override def update(actions: List[GameEvent]): Unit = {
    //println("MODEL update - " + actions.toString())

    if(actions.nonEmpty) {
      for(command <- actions) this.player.setCommand(command)
    }

    this.world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS)
  }
}

package model.helpers

import ImplicitConversions.RichInt
import com.badlogic.gdx.physics.box2d.World
import model.entity.Entity
import ImplicitConversions.RichWorld

/** Static utility class containing useful functionalities for moving
 * enemies.
 *
 */
object EntitiesUtilities {
  val world: World = EntitiesFactoryImpl.getEntitiesContainerMonitor.getWorld.get
  def isPathObstructedOnTheLeft(entity: Entity, hOffset: Float=0.PPM, vOffset: Float=0.PPM): Boolean = {
    val body = entity.getBody
    val position = entity.getBody.getPosition
    val size = entity.getSize

    world.checkCollision(
      position.x - size._1 - hOffset, position.y - size._2 / 2 + vOffset,
      position.x - size._1 - hOffset, position.y + size._2 / 2 + vOffset,
      body)
  }

  def isPathObstructedOnTheRight(entity: Entity, hOffset: Float=0.PPM, vOffset: Float=0.PPM): Boolean = {
    val body = entity.getBody
    val position = entity.getBody.getPosition
    val size = entity.getSize

    world.checkCollision(
      position.x + size._1 + hOffset, position.y - size._2 / 2 + vOffset,
      position.x + size._1 + hOffset, position.y + size._2 / 2 + vOffset,
      body)
  }

  def isFloorPresentOnTheRight(entity: Entity, hOffset: Float=0.PPM, vOffset: Float=0.PPM): Boolean = {
    val body = entity.getBody
    val position = entity.getBody.getPosition
    val size = entity.getSize

    world.checkCollision(
      position.x + size._1 + hOffset, position.y - size._2 + vOffset,
      position.x + size._1 + hOffset, position.y - size._2 + vOffset,
      body)
  }

  def isFloorPresentOnTheLeft(entity: Entity, hOffset: Float=0.PPM, vOffset: Float=0.PPM): Boolean = {
    val body = entity.getBody
    val position = entity.getBody.getPosition
    val size = entity.getSize

    world.checkCollision(
      position.x - size._1 - hOffset, position.y - size._2 + vOffset,
      position.x - size._1 - hOffset, position.y - size._2 + vOffset,
      body)
  }
}

package model.movement

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import model.entities.{Entity, Hero}
import model.helpers.WorldUtilities.{checkAABBCollision, checkPointCollision, scaleForceVector}

import scala.util.Random


class Stationary() extends MovementStrategy {
  override def move(): Unit = { }

}

class PatrolPlatform(val entity: Entity, val world: World) extends MovementStrategy {
  // TODO: make movement acceleration and max movement speed an enemy stat
  var movingLeft = true

  protected def canMoveLeft: Boolean =
    !checkPointCollision(world, entity.getPosition._1 - entity.getSize._1 - 0.5f, entity.getPosition._2) &&
      checkPointCollision(world, entity.getPosition._1 - entity.getSize._1 - 0.5f, entity.getPosition._2 - entity.getSize._2 - 0.5f)

  protected def canMoveRight: Boolean =
    !checkPointCollision(world, entity.getPosition._1 + entity.getSize._1 + 0.5f, entity.getPosition._2) &&
      checkPointCollision(world, entity.getPosition._1 + entity.getSize._1 + 0.5f, entity.getPosition._2 - entity.getSize._2 - 0.5f)

  override def move(): Unit = {
    // change direction check
    if (!canMoveLeft && movingLeft) movingLeft = false
    if (!canMoveRight && !movingLeft) movingLeft = true

    // apply movement to entity's body
    if (movingLeft && entity.getBody.getLinearVelocity.x >= -3) {
      entity.getBody.applyLinearImpulse(new Vector2(-3f, 0), entity.getBody.getWorldCenter, true)
    } else if (!movingLeft && entity.getBody.getLinearVelocity.x <= 3){
      entity.getBody.applyLinearImpulse(new Vector2(+3f, 0), entity.getBody.getWorldCenter, true)
    }
  }
}

class PatrolPlatformRandomly(override val entity: Entity, override val world: World) extends PatrolPlatform(entity, world)  {
  private val randomGen = new Random(0)
  private var lastRandomChange = System.currentTimeMillis()

  override def move(): Unit = {
    // randomly change direction every 5 seconds
    if (System.currentTimeMillis() - lastRandomChange > 5000) {
      movingLeft = randomGen.nextBoolean()
      lastRandomChange = System.currentTimeMillis()
    }
    super.move()
  }
}

class ChaseHero(val entity: Entity, val world: World, val heroEntity:Entity) extends MovementStrategy {
  // TODO: stop moving when near hero. entity continues to appliy forces and move towards hero when touching hero.
  // TODO: make fieldOfView an enemy stat
  val linearFieldOfView: Float = 5f

  override def move(): Unit = {
    if (isHeroOnTheFarLeft && !isHeroOnTheNearLeft) {
      entity.getBody.applyLinearImpulse(scaleForceVector(new Vector2(-5f, 0)), entity.getBody.getWorldCenter, true)
    } else if (isHeroOnTheFarRight && !isHeroOnTheNearRight) {
      entity.getBody.applyLinearImpulse(scaleForceVector(new Vector2(5f, 0)), entity.getBody.getWorldCenter, true)
    }
  }

  protected def isHeroOnTheFarLeft: Boolean = checkAABBCollision(world,
    entity.getPosition._1 - entity.getSize._1 - linearFieldOfView, entity.getPosition._2,
    entity.getPosition._1 - entity.getSize._1 , entity.getPosition._2,
    heroEntity)

  protected def isHeroOnTheFarRight: Boolean = checkAABBCollision(world,
    entity.getPosition._1 + entity.getSize._1, entity.getPosition._2,
    entity.getPosition._1 + entity.getSize._1 + linearFieldOfView, entity.getPosition._2,
    heroEntity)

  protected def isHeroOnTheNearLeft: Boolean = checkAABBCollision(world,
    entity.getPosition._1 - entity.getSize._1 - 2, entity.getPosition._2,
    entity.getPosition._1 - entity.getSize._1 , entity.getPosition._2,
    heroEntity)

  protected def isHeroOnTheNearRight: Boolean = checkAABBCollision(world,
    entity.getPosition._1 + entity.getSize._1, entity.getPosition._2,
    entity.getPosition._1 + entity.getSize._1 + 2, entity.getPosition._2,
    heroEntity)
}

class PatrolAndStopIfFacingHero(override val entity: Entity, override val world: World, val heroEntity: Entity) extends PatrolPlatform(entity, world)  {

  override def move(): Unit = {
    if ((!isHeroOnTheNearLeft && movingLeft) || (!isHeroOnTheNearRight && !movingLeft))
      super.move()
  }

  protected def isHeroOnTheNearLeft: Boolean = checkAABBCollision(world,
    entity.getPosition._1 - entity.getSize._1 - 2, entity.getPosition._2,
    entity.getPosition._1 - entity.getSize._1 , entity.getPosition._2,
    heroEntity)

  protected def isHeroOnTheNearRight: Boolean = checkAABBCollision(world,
    entity.getPosition._1 + entity.getSize._1, entity.getPosition._2,
    entity.getPosition._1 + entity.getSize._1 + 2, entity.getPosition._2,
    heroEntity)
}


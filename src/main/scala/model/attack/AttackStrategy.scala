package model.attack

import com.badlogic.gdx.physics.box2d.World
import model.Level
import model.entities.{Attack, Entity, MobileEntity}
import model.helpers.EntitiesFactory
import model.helpers.WorldUtilities._
import model.movement.MovementStrategy

trait AttackStrategy {
  def canAttack(): Boolean
  def attack(): Unit
}

class DoNotAttack() extends AttackStrategy {
  override def canAttack(): Boolean = false

  override def attack(): Unit = {}
}

class MeleeSwordAttack(enemyEntity: Entity, heroEntity:Entity, world:World, level:Level) extends AttackStrategy {
  private val maxDistance = 3

  override def canAttack(): Boolean = checkEntityIsVisible(heroEntity, enemyEntity, world, level) &&
    getEntitiesDistance(enemyEntity, heroEntity) <= maxDistance

  override def attack(): Unit = {
    if (canAttack()) {

    }
  }
}

class RangedArrowAttack(enemyEntity: Entity, heroEntity:Entity, world:World, level:Level, entitiesFactory: EntitiesFactory) extends AttackStrategy {
  private val maxDistance = 10

  val attackFrequency = 1000
  var lastAttackTime = 0l

  override def canAttack(): Boolean =
    checkEntityIsVisible(heroEntity, enemyEntity, world, level) &&
      getEntitiesDistance(enemyEntity, heroEntity) <= maxDistance &&
      System.currentTimeMillis() - lastAttackTime >= attackFrequency

  override def attack(): Unit = {
    if (canAttack()) {
      lastAttackTime = System.currentTimeMillis()
      val attackEntity:Attack = entitiesFactory.createArrowProjectile(
          enemyEntity.getBody.getWorldCenter,
          heroEntity.getBody.getWorldCenter, enemyEntity)
      level.addEntity(attackEntity)
    }
  }

}
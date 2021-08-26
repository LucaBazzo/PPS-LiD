package model.attack

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._
import model.collisions.ApplyDamage
import model.helpers.EntitiesFactoryImpl.{createEnemyProjectile, createEnemySwordAttack}
import model.collisions.ImplicitConversions.RichInt
import model.entities.{Entity, MobileEntity, MobileEntityImpl}
import model.helpers.EntitiesFactoryImpl
import model.helpers.WorldUtilities.{checkBodyIsVisible, getBodiesDistance, isTargetOnTheRight}
import model.movement.ProjectileTrajectory


class DoNotAttack() extends AttackStrategy {
  private def canAttack: Boolean = false

  override def apply(): Unit = { }

  override def isAttackFinished: Boolean = true

  override def stopAttack(): Unit = { }

}
//
//class ContactAttackStrategy(sourceEntity: Entity, targetEntity:Entity, world:World, level:Level)
//  extends AttackStrategy {
//
//  spawnAttack()
//
//  def spawnAttack(): Unit = {
//    val bodyDef: BodyDef = new BodyDef()
//    bodyDef.position.set(sourceEntity.getBody.getWorldCenter)
//    bodyDef.`type` = BodyDef.BodyType.DynamicBody
//    val body: Body = world.createBody(bodyDef)
//
//    val fixtureDef: FixtureDef = new FixtureDef()
//    val shape: CircleShape = new CircleShape()
//    shape.setRadius(sourceEntity.getSize._1)
//    fixtureDef.isSensor = true
//    fixtureDef.shape = shape
//    val fixture = body.createFixture(fixtureDef)
//
//    // set collisions filter ??
//
//    val jointDef:WeldJointDef = new WeldJointDef()
//    jointDef.initialize(sourceEntity.getBody, body, sourceEntity.getBody.getPosition)
//    val joint = world.createJoint(jointDef)
//
//    val contactAttack:Entity = new MobileEntityImpl(body, sourceEntity.getSize)
//    contactAttack.setCollisionStrategy(new ApplyDamage(contactAttack, targetEntity))
//    level.addEntity(contactAttack)
//  }
//
//  private def canAttack: Boolean = false
//
//  override def apply(): Unit = { }
//
//  override def isAttackFinished: Boolean = false
//}

class MeleeAttackStrategy(sourceEntity: Entity, targetEntity:Entity, world:World) extends AttackStrategy {
  protected val maxDistance:Float = 4
  protected val visibilityMaxHorizontalAngle:Int = 80
  protected val attackFrequency:Int = 2000
  protected val attackDuration:Int = 1200

  protected var lastAttackTime:Long = 0

  protected var attackInstance: Option[MobileEntity] = Option.empty

  private def canAttack: Boolean =  {
    System.currentTimeMillis() - this.lastAttackTime > this.attackFrequency &&
      checkBodyIsVisible(world, sourceEntity.getBody, targetEntity.getBody, this.visibilityMaxHorizontalAngle) &&
      getBodiesDistance(sourceEntity.getBody, targetEntity.getBody) <= this.maxDistance
  }

  override def apply(): Unit = {
    if (canAttack) {
      this.lastAttackTime = System.currentTimeMillis()
      this.attackInstance = Option(spawnAttack())
    }

    // delete the attack entity
    if (attackInstance.isDefined && System.currentTimeMillis() - this.lastAttackTime >= this.attackDuration) {
      EntitiesFactoryImpl.destroyBody(this.attackInstance.get.getBody)
      EntitiesFactoryImpl.removeEntity(this.attackInstance.get)
      this.attackInstance = Option.empty
    }
  }

  override def isAttackFinished: Boolean = this.attackInstance.isEmpty

  private def spawnAttack(): MobileEntity = {
    val spawnCoordinates: Vector2 = sourceEntity.getBody.getWorldCenter.add(
      if (isTargetOnTheRight(sourceEntity.getBody, targetEntity.getBody))
        sourceEntity.getSize._1 else -sourceEntity.getSize._1, 0)

    val entity:MobileEntity = createEnemySwordAttack((10f, 10f), (spawnCoordinates.x, spawnCoordinates.y), sourceEntity.getBody)

    entity.setCollisionStrategy(new ApplyDamage(entity, targetEntity))
    entity.getBody.setBullet(true)
    entity
  }

  override def stopAttack(): Unit = ???
}

class RangedArrowAttack(sourceEntity: Entity, targetEntity:Entity, world:World) extends AttackStrategy {
  protected val maxDistance:Float = 50.PPM
  protected val visibilityMaxHorizontalAngle:Int = 10
  protected val attackFrequency:Int = 2000
  protected val attackDuration:Int = 1500

  protected var lastAttackTime:Long = 0

  protected var activeAttacks: Map[MobileEntity, Long] = Map.empty

  private def canAttack: Boolean =  {
    System.currentTimeMillis() - this.lastAttackTime > this.attackFrequency &&
      checkBodyIsVisible(world, sourceEntity.getBody, targetEntity.getBody, this.visibilityMaxHorizontalAngle) &&
      getBodiesDistance(sourceEntity.getBody, targetEntity.getBody) <= this.maxDistance
  }

  override def apply(): Unit = {
    if (canAttack) {
      this.lastAttackTime = System.currentTimeMillis()
      this.activeAttacks = this.activeAttacks + (spawnAttack() -> this.lastAttackTime)
    }

    // remove attack entities
    val currentTime:Long = System.currentTimeMillis()
    this.activeAttacks.find(item => currentTime - item._2 >= this.attackDuration).map(item => item._1).foreach(e => {
      EntitiesFactoryImpl.destroyBody(e.getBody)
      EntitiesFactoryImpl.removeEntity(e)
    })
    this.activeAttacks = this.activeAttacks.filter(item => currentTime - item._2 < this.attackDuration)
  }

  override def isAttackFinished: Boolean = System.currentTimeMillis() - this.lastAttackTime >= this.attackDuration

  private def spawnAttack(): MobileEntity = {
    val spawnCoordinates = sourceEntity.getBody.getWorldCenter.add(
      if (isTargetOnTheRight(sourceEntity.getBody, targetEntity.getBody))
        sourceEntity.getSize._1 else -sourceEntity.getSize._1, 0)

    val entity:MobileEntity = createEnemyProjectile((5, 5), (spawnCoordinates.x, spawnCoordinates.y))

    entity.setMovementStrategy(new ProjectileTrajectory(entity, sourceEntity, targetEntity))
    entity.setCollisionStrategy(new ApplyDamage(entity, targetEntity))
//    entity.setCollisionStrategy(new ApplyDamageAndDestroyEntity(entity, targetEntity))
    entity.getBody.setBullet(true)
    entity
  }

  override def stopAttack(): Unit = ???
}

//class MagicMissleAttack(enemyEntity: Entity, heroEntity:Entity, world:World, level:Level) extends AttackStrategy {
//  private val maxDistance = 10
//
//  val attackFrequency = 1000
//  var lastAttackTime = 0l
//
//  override def attack(): Unit = {
//    lastAttackTime = System.currentTimeMillis()
//    val attackEntity:Attack = entitiesFactory.createProjectile(
//      enemyEntity.getBody.getWorldCenter,
//      heroEntity.getBody.getWorldCenter, enemyEntity)
//    level.addEntity(attackEntity)
//  }
//
//}
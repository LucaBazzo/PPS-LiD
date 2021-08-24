package model.attack

import com.badlogic.gdx.physics.box2d._
import model.collisions.ApplyDamage
import model.collisions.ImplicitConversions.RichInt
import model.entities.{Entity, MobileEntity, MobileEntityImpl}
import model.helpers.EntitiesFactoryImpl.createEnemyProjectile
import model.helpers.WorldUtilities.{checkBodyIsVisible, getBodiesDistance, isTargetOnTheRight}
import model.movement.ProjectileTrajectory


class DoNotAttack() extends AttackStrategy {
  private def canAttack: Boolean = false

  override def apply(): Unit = { }

  override def isAttackFinished: Boolean = false

  override def stopAttack(): Unit = ???
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
//
//class MeleeAttackStrategy(sourceEntity: Entity, targetEntity:Entity, world:World, level:Level) extends AttackStrategy {
//  protected val maxDistance:Float = 3
//  protected val visibilityMaxHorizontalAngle:Int = 80
//  protected val attackFrequency:Int = 2000
//  protected val attackDuration:Int = 1000
//
//  protected var lastAttackTime:Long = 0
//
//  private def canAttack: Boolean =  {
//    System.currentTimeMillis() - lastAttackTime > attackFrequency &&
//      checkBodyIsVisible(world, sourceEntity.getBody, targetEntity.getBody, visibilityMaxHorizontalAngle) &&
//      getBodiesDistance(sourceEntity.getBody, targetEntity.getBody) <= maxDistance
//  }
//
//  override def apply(): Unit = {
//    if (canAttack) {
//      lastAttackTime = System.currentTimeMillis()
//      level.addEntity(spawnAttack())
//    }
//  }
//
//  override def isAttackFinished: Boolean = System.currentTimeMillis() - lastAttackTime < attackDuration
//
//  private def spawnAttack(): Entity = {
//    val bodyDef: BodyDef = new BodyDef()
//    bodyDef.position.set(sourceEntity.getBody.getWorldCenter.add(
//      if (isTargetOnTheRight(sourceEntity.getBody, targetEntity.getBody)) +1f else -1f, 0))
//    bodyDef.`type` = BodyDef.BodyType.DynamicBody
//    val body: Body = world.createBody(bodyDef)
//
//    val fixtureDef: FixtureDef = new FixtureDef()
//    val shape: PolygonShape = new PolygonShape()
//    shape.setAsBox(sourceEntity.getSize._1, sourceEntity.getSize._2)
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
//
//    contactAttack
//  }
//}

class RangedArrowAttack(sourceEntity: Entity, targetEntity:Entity, world:World) extends AttackStrategy {
  protected val maxDistance:Float = 50.PPM
  protected val visibilityMaxHorizontalAngle:Int = 10
  protected val attackFrequency:Int = 2000
  protected val attackDuration:Int = 1000

  protected var lastAttackTime:Long = 0

  protected var activeAttacks: List[MobileEntityImpl] = List.empty

  private def canAttack: Boolean =  {
    System.currentTimeMillis() - lastAttackTime > attackFrequency &&
      checkBodyIsVisible(world, sourceEntity.getBody, targetEntity.getBody, visibilityMaxHorizontalAngle) &&
      getBodiesDistance(sourceEntity.getBody, targetEntity.getBody) <= maxDistance
  }

  override def apply(): Unit = {
    if (canAttack) {
      lastAttackTime = System.currentTimeMillis()
      spawnAttack()
    }
  }

  override def isAttackFinished: Boolean = System.currentTimeMillis() - lastAttackTime < attackDuration

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
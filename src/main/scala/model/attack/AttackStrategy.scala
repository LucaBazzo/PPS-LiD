package model.attack

import com.badlogic.gdx.physics.box2d._
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef
import model.Level
import model.collisions.ApplyDamage
import model.entities.{Entity, MobileEntityImpl, TimedAttack}
import model.helpers.EntitiesBits
import model.helpers.WorldUtilities.{checkBodyIsVisible, getBodiesDistance, isTargetOnTheRight}
import model.movement.ProjectileTrajectory

//class TimedAttack(attackEntity:Entity, delta:Int, level: Level) extends Runnable {
//  override def run(): Unit = {
//    Thread.sleep(delta)
//    level.removeEntity(attackEntity)
//  }
//}

trait AttackStrategy {
  def attack(): Unit
  def canAttack: Boolean
  def isAttacking: Boolean
}

class DoNotAttack() extends AttackStrategy {
  override def canAttack: Boolean = false

  override def attack(): Unit = { }

  override def isAttacking: Boolean = false
}

class ContactAttackStrategy(sourceEntity: Entity, targetEntity:Entity, world:World, level:Level)
  extends AttackStrategy {

  spawnAttack()

  def spawnAttack(): Unit = {
    val bodyDef: BodyDef = new BodyDef()
    bodyDef.position.set(sourceEntity.getBody.getWorldCenter)
    bodyDef.`type` = BodyDef.BodyType.DynamicBody
    val body: Body = world.createBody(bodyDef)

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: CircleShape = new CircleShape()
    shape.setRadius(sourceEntity.getSize._1)
    fixtureDef.isSensor = true
    fixtureDef.shape = shape
    val fixture = body.createFixture(fixtureDef)

    // set collisions filter ??

    val jointDef:WeldJointDef = new WeldJointDef()
    jointDef.initialize(sourceEntity.getBody, body, sourceEntity.getBody.getPosition)
    val joint = world.createJoint(jointDef)

    val contactAttack:Entity = new MobileEntityImpl(body, sourceEntity.getSize)
    contactAttack.setCollisionStrategy(new ApplyDamage(contactAttack, targetEntity))
    level.addEntity(contactAttack)
  }

  override def canAttack: Boolean = false

  override def attack(): Unit = { }

  override def isAttacking: Boolean = false
}

class MeleeAttackStrategy(sourceEntity: Entity, targetEntity:Entity, world:World, level:Level) extends AttackStrategy {
  protected val maxDistance:Float = 3
  protected val visibilityMaxHorizontalAngle:Int = 80
  protected val attackFrequency:Int = 2000
  protected val attackDuration:Int = 1000

  protected var lastAttackTime:Long = 0

  override def canAttack: Boolean =  {
    System.currentTimeMillis() - lastAttackTime > attackFrequency &&
      checkBodyIsVisible(world, sourceEntity.getBody, targetEntity.getBody, visibilityMaxHorizontalAngle) &&
      getBodiesDistance(sourceEntity.getBody, targetEntity.getBody) <= maxDistance
  }

  override def attack(): Unit = {
    if (canAttack) {
      lastAttackTime = System.currentTimeMillis()
      level.addEntity(spawnAttack())
    }
  }

  override def isAttacking: Boolean = System.currentTimeMillis() - lastAttackTime < attackDuration

  private def spawnAttack(): Entity = {
    val bodyDef: BodyDef = new BodyDef()
    bodyDef.position.set(sourceEntity.getBody.getWorldCenter.add(
      if (isTargetOnTheRight(sourceEntity.getBody, targetEntity.getBody)) +1f else -1f, 0))
    bodyDef.`type` = BodyDef.BodyType.DynamicBody
    val body: Body = world.createBody(bodyDef)

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: PolygonShape = new PolygonShape()
    shape.setAsBox(sourceEntity.getSize._1, sourceEntity.getSize._2)
    fixtureDef.isSensor = true
    fixtureDef.shape = shape
    val fixture = body.createFixture(fixtureDef)

    // set collisions filter ??

    val jointDef:WeldJointDef = new WeldJointDef()
    jointDef.initialize(sourceEntity.getBody, body, sourceEntity.getBody.getPosition)
    val joint = world.createJoint(jointDef)

    val contactAttack:Entity = new MobileEntityImpl(body, sourceEntity.getSize)
    contactAttack.setCollisionStrategy(new ApplyDamage(contactAttack, targetEntity))

    contactAttack
  }
}

class RangedArrowAttack(sourceEntity: Entity, targetEntity:Entity, world:World, level:Level) extends AttackStrategy {
  protected val maxDistance:Float = 15
  protected val visibilityMaxHorizontalAngle:Int = 80
  protected val attackFrequency:Int = 2000
  protected val attackDuration:Int = 1000

  protected var lastAttackTime:Long = 0

  protected var activeAttacks: List[MobileEntityImpl] = List.empty

  override def canAttack: Boolean =  {
    System.currentTimeMillis() - lastAttackTime > attackFrequency &&
      checkBodyIsVisible(world, sourceEntity.getBody, targetEntity.getBody, visibilityMaxHorizontalAngle) &&
      getBodiesDistance(sourceEntity.getBody, targetEntity.getBody) <= maxDistance
  }

  override def attack(): Unit = {
    if (canAttack) {
      lastAttackTime = System.currentTimeMillis()
      level.addEntity(spawnAttack())
    }
  }

  override def isAttacking: Boolean = System.currentTimeMillis() - lastAttackTime < attackDuration

  private def spawnAttack(): MobileEntityImpl = {
    val bodyDef: BodyDef = new BodyDef()
    bodyDef.position.set(sourceEntity.getBody.getWorldCenter.add(
      if (isTargetOnTheRight(sourceEntity.getBody, targetEntity.getBody))
        sourceEntity.getSize._1 else -sourceEntity.getSize._1, 0))
    bodyDef.`type` = BodyDef.BodyType.DynamicBody
    val body: Body = world.createBody(bodyDef)

    val fixtureDef: FixtureDef = new FixtureDef()
    val shape: CircleShape = new CircleShape()
    shape.setRadius(0.5f)
    fixtureDef.isSensor = true
    fixtureDef.shape = shape
    fixtureDef.filter.categoryBits = EntitiesBits.ENEMY_PROJECTILE_CATEGORY_BIT
    fixtureDef.filter.maskBits = EntitiesBits.ENEMY_PROJECTILE_COLLISIONS_MASK
    val fixture = body.createFixture(fixtureDef)

    val arrowAttack:TimedAttack = new TimedAttack(body, sourceEntity.getSize, 1000, level)
    arrowAttack.setMovementStrategy(new ProjectileTrajectory(arrowAttack, sourceEntity, targetEntity))
    arrowAttack.setCollisionStrategy(new ApplyDamage(arrowAttack, targetEntity))
//    arrowAttack.setCollisionStrategy(new ApplyDamageAndDestroySourceEntity(arrowAttack, targetEntity, level))
//    arrowAttack.getBody.setBullet(true)
    arrowAttack
  }
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
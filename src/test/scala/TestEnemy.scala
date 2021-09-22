import com.badlogic.gdx.math.Vector2
import model.collisions.EntityCollisionBit
import model.collisions.ImplicitConversions._
import model.entities.Statistic.Statistic
import model.entities._
import model.helpers.EntitiesFactoryImpl.createEnemyEntity
import model.helpers.{EntitiesContainerMonitor, EntitiesFactoryImpl}
import model.movement.{DoNothingMovementStrategy, FaceTarget, PatrolAndStop, PatrolPlatform}
import model.{Level, LevelImpl}
import org.scalatest.flatspec.AnyFlatSpec

class TestEnemy extends AnyFlatSpec {

  "An enemy" should "be able to suffer damage" in {

  }

  "An enemy" should "be able to die" in {

  }

  "An enemy death" should "increase the game score" in {

  }

  "An enemy" can "drop items on death" in {

  }

  "An enemy boss" should "drop a bow" in {

  }

  "An enemy boss" can "drop items other than a bow" in {

  }
}

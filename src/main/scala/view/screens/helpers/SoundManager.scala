package view.screens.helpers

import com.badlogic.gdx.audio.{Music, Sound}
import utils.SoundConstants.{getMusicMap, getSoundMap}
import view.screens.helpers.SoundEvent.{AirDownAttack, Attack1, Attack2, Attack3, BowAttack, Dying, EnemyAttack, EnemyDeath, Hurt, Jump, OpeningDoor, PickItem, SoundEvent, WorldSoundtrack}

object SoundEvent extends Enumeration {
  type SoundEvent = Value
  val WorldSoundtrack, OpeningScreenSoundtrack,
  Jump, Attack1, Attack2, Attack3, BowAttack, AirDownAttack, Hurt, Dying,
  EnemyAttack, EnemyDeath, OpeningDoor, PickItem = Value
}

class SoundManager {

  private val musicMap: Map[SoundEvent, Music] = getMusicMap()
  private val soundMap: Map[SoundEvent, Sound] = getSoundMap()

  def playSound(soundEvent: SoundEvent): Unit = {

    println("PLAY SOUND: " + soundEvent)

    soundEvent match {
      case WorldSoundtrack => {
        musicMap(WorldSoundtrack).setLooping(true)
        musicMap(WorldSoundtrack).play
      }
      case Jump => soundMap(Jump).play
      case Attack1 => soundMap(Attack1).play
      case Attack2 => soundMap(Attack2).play
      case Attack3 => soundMap(Attack3).play
      case BowAttack => soundMap(BowAttack).play
      case AirDownAttack => soundMap(AirDownAttack).play
      case Hurt => soundMap(Hurt).play
      case Dying => soundMap(Dying).play
      case OpeningDoor => soundMap(OpeningDoor).play
      case PickItem => soundMap(PickItem).play
      case EnemyAttack => soundMap(EnemyAttack).play
      case EnemyDeath => soundMap(EnemyDeath).play
      case _ => println("unsupported sound")
    }
  }
}
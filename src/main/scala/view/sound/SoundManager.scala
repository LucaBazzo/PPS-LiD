package view.sound

import com.badlogic.gdx.audio.{Music, Sound}
import model.entity.EntityType.EntityType
import model.entity.State.State
import model.entity.{EntityType, State}
import utils.SoundConstants.{getMusicMap, getSoundMap}
import view.sound.SoundEvent.{AirDownAttack, Attack1, Attack2, Attack3, BossSoundtrack, BowAttack,
  Dying, EnemyAttack, EnemyDeath, Hurt, Jump, OpeningDoor, PickItem, SoundEvent, WorldSoundtrack}

/**
 * event that can be associated to a sound or a music
 */
object SoundEvent extends Enumeration {
  type SoundEvent = Value
  val WorldSoundtrack, OpeningScreenSoundtrack, BossSoundtrack,
  Jump, Attack1, Attack2, Attack3, BowAttack, AirDownAttack, Hurt, Dying,
  EnemyAttack, EnemyDeath, OpeningDoor, PickItem = Value
}

/**
 * manage all sound and music execution related to SoundEvents
 */
class SoundManager {

  private val musicMap: Map[SoundEvent, Music] = getMusicMap()
  private val soundMap: Map[SoundEvent, Sound] = getSoundMap()
  //support variable to track the actually-playing music
  private var previousMusicEvent: SoundEvent = _

  /**
   * @param soundEvent play the sound related to the event
   */
  def playSound(soundEvent: SoundEvent): Unit = {
    soundEvent match {
      case WorldSoundtrack | BossSoundtrack => {
        //check if there is a playing music
        if (previousMusicEvent == null) {
          musicMap(soundEvent).setLooping(true)
          musicMap(soundEvent).play()
        } else if (!previousMusicEvent.equals(soundEvent)) {
          musicMap(previousMusicEvent).pause()

          musicMap(soundEvent).setLooping(true)
          musicMap(soundEvent).play
        }
        previousMusicEvent = soundEvent
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
      case _ =>
    }
  }

  /**
   * stop the actually-playing music
   */
  def stopMusic(): Unit = musicMap(previousMusicEvent).stop()

  /**
   * call playSound method with a SoundEvent based on the new state of the entity
   * @param entityType used to identify the entity type(hero, door, enemy...)
   * @param entityState used to identify the entity state
   */
  def playSoundOnStateChange(entityType: EntityType, entityState: State): Unit = {
     entityType match {
       case EntityType.Hero =>
         if(entityState.equals(State.Jumping) || entityState.equals(State.Somersault)) this.playSound(SoundEvent.Jump)
         else if(entityState.equals(State.Attack01)) this.playSound(SoundEvent.Attack1)
         else if(entityState.equals(State.Attack02)) this.playSound(SoundEvent.Attack2)
         else if(entityState.equals(State.Attack03)) this.playSound(SoundEvent.Attack3)
         else if(entityState.equals(State.BowAttacking)) this.playSound(SoundEvent.BowAttack)
         else if(entityState.equals(State.AirDownAttacking)) this.playSound(SoundEvent.AirDownAttack)
         else if(entityState.equals(State.PickingItem)) this.playSound(SoundEvent.PickItem)
         else if(entityState.equals(State.Hurt)) this.playSound(SoundEvent.Hurt)
         else if(entityState.equals(State.Dying)) this.playSound(SoundEvent.Dying)
       case EntityType.Door =>
         if(entityState.equals(State.Opening))
           this.playSound(SoundEvent.OpeningDoor)
       case EntityType.EnemySkeleton | EntityType.EnemySlime | EntityType.EnemyPacman | EntityType.EnemyWorm =>
         if(entityState.equals(State.Dying))
           this.playSound(SoundEvent.EnemyDeath)
       case _ =>
     }
  }
}

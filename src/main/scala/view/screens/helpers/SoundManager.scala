package view.screens.helpers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.{Music, Sound}
import utils.ApplicationConstants.{JUMP_SOUND_LOCATION, LEVEL_MUSIC_LOCATION}
import view.screens.helpers.SoundEvent.{Jump, SoundEvent, WorldSoundtrack}

object SoundEvent extends Enumeration {
  type SoundEvent = Value
  val Jump, WorldSoundtrack = Value
}
class SoundManager {

  private val worldSoundtrack: Music = Gdx.audio.newMusic(Gdx.files.internal(LEVEL_MUSIC_LOCATION))
  private val jumpSound: Sound = Gdx.audio.newSound(Gdx.files.internal(JUMP_SOUND_LOCATION))
  private val attackSound1: Sound = null
  private val attackSound2: Sound = null
  private val attackSound3: Sound = null
  private val keyItemSound: Sound = null
  private val powerupSound: Sound = null

  def playSound(soundEvent: SoundEvent): Unit = {

    println("playing sound: " + soundEvent)
    soundEvent match {
      case WorldSoundtrack => {
        worldSoundtrack.setLooping(true)
        worldSoundtrack.play()
      }
      case Jump => this.jumpSound.play
      case _ => println("unsupported sound")
    }
  }
}
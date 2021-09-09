package controller

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.{Music, Sound}
import com.badlogic.gdx.files.FileHandle
import utils.ApplicationConstants.{HERO_ATTACK_1_SOUND_LOCATION, HERO_ATTACK_2_SOUND_LOCATION, HERO_ATTACK_3_SOUND_LOCATION, JUMP_SOUND_LOCATION, KEY_ITEM_SOUND_LOCATION, LEVEL_MUSIC_LOCATION, POWERUP_SOUND_LOCATION}

class SoundManager {

  private var music: Music = null
  private var jumpSound: Sound = null
  private var attackSound1: Sound = null
  private var attackSound2: Sound = null
  private var attackSound3: Sound = null
  private var keyItemSound: Sound = null
  private var powerupSound: Sound = null

  def loadSounds(): Unit = {
    jumpSound = Gdx.audio.newSound(Gdx.files.internal(JUMP_SOUND_LOCATION))
//    attackSound1 = Gdx.audio.newSound(Gdx.files.internal(HERO_ATTACK_1_SOUND_LOCATION))
//    attackSound2 = Gdx.audio.newSound(Gdx.files.internal(HERO_ATTACK_2_SOUND_LOCATION))
//    attackSound3 = Gdx.audio.newSound(Gdx.files.internal(HERO_ATTACK_3_SOUND_LOCATION))
//    keyItemSound = Gdx.audio.newSound(Gdx.files.internal(KEY_ITEM_SOUND_LOCATION))
//    powerupSound = Gdx.audio.newSound(Gdx.files.internal(POWERUP_SOUND_LOCATION))

  }

  def loadMusic(): Unit = {
    val fileHandle: FileHandle = Gdx.files.internal(LEVEL_MUSIC_LOCATION)
    music = Gdx.audio.newMusic(fileHandle)
  }

  def startMusic(): Unit = {
    if(music == null) loadMusic

    music.setLooping(true)
    music.play()
  }


  def playSound(soundName: String): Unit = {
    if(jumpSound == null) this.loadSounds()

    soundName match {
      case "JUMP" => this.jumpSound.play
      case "ATTACK1" => this.attackSound1.play
      case "ATTACK2" => this.attackSound2.play
      case "ATTACK3" => this.attackSound3.play
      case "KEY_ITEM" => this.keyItemSound.play
      case "POWERUP" => this.powerupSound.play
      case _ => println("unsupported sound")
    }
  }
}
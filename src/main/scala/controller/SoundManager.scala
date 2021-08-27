package controller

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.{Music, Sound}
import com.badlogic.gdx.files.FileHandle
import utils.ApplicationConstants.{JUMP_SOUND_LOCATION, LEVEL_MUSIC_LOCATION}

class SoundManager {

  def startMusic(): Unit = {

    val fileHandle: FileHandle = Gdx.files.internal(LEVEL_MUSIC_LOCATION)

    val music: Music = Gdx.audio.newMusic(fileHandle)
    music.setLooping(true)
    music.play()

  }

  def jumpSound(): Unit = {

    val fileHandle: FileHandle = Gdx.files.internal(JUMP_SOUND_LOCATION)

    val sound: Sound = Gdx.audio.newSound(fileHandle)
    sound.play()

  }

}

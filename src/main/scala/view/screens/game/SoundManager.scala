package view.screens.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.files.FileHandle
import utils.ApplicationConstants.LEVEL_MUSIC_LOCATION

class SoundManager {

  def startMusic(): Unit = {

    val fileHandle: FileHandle = Gdx.files.internal(LEVEL_MUSIC_LOCATION)

    val music: Music = Gdx.audio.newMusic(fileHandle)
    music.setLooping(true)
    music.play()

  }

}

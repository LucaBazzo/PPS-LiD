package utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.{Music, Sound}
import view.screens.helpers.SoundEvent._

object SoundConstants {

  val LEVEL_MUSIC_LOCATION = "assets/audio/music.mp3"
  val JUMP_SOUND_LOCATION = "assets/audio/jump.wav"
  val HERO_ATTACK_1_SOUND_LOCATION = "assets/audio/jump.wav"
  val HERO_ATTACK_2_SOUND_LOCATION = "assets/audio/jump.wav"
  val HERO_ATTACK_3_SOUND_LOCATION = "assets/audio/jump.wav"
  val BOW_SOUND_LOCATION = "assets/audio/jump.wav"
  val AIR_DOWN_SOUND_LOCATION = "assets/audio/jump.wav"
  val HURT_SOUND_LOCATION = "assets/audio/jump.wav"
  val DYING_SOUND_LOCATION = "assets/audio/jump.wav"
  val OPENING_CHEST_SOUND_LOCATION = "assets/audio/jump.wav"
  val OPENING_DOOR_SOUND_LOCATION = "assets/audio/jump.wav"
  val PICK_ITEM_SOUND_LOCATION = "assets/audio/jump.wav"
  val ENEMY_ATTACK_SOUND_LOCATION = "assets/audio/jump.wav"
  val ENEMY_DEATH_SOUND_LOCATION = "assets/audio/jump.wav"


  val POWERUP_SOUND_LOCATION = "assets/audio/powerup.wav"
  val KEY_ITEM_SOUND_LOCATION = "assets/audio/key-item.wav"

  def getMusicMap(): Map[SoundEvent, Music] = Map(
    WorldSoundtrack -> Gdx.audio.newMusic(Gdx.files.internal(LEVEL_MUSIC_LOCATION))
  )

  def getSoundMap(): Map[SoundEvent, Sound] = Map(
    Jump -> Gdx.audio.newSound(Gdx.files.internal(JUMP_SOUND_LOCATION)),
    Attack1 -> Gdx.audio.newSound(Gdx.files.internal(HERO_ATTACK_1_SOUND_LOCATION)),
    Attack2 -> Gdx.audio.newSound(Gdx.files.internal(HERO_ATTACK_2_SOUND_LOCATION)),
    Attack3 -> Gdx.audio.newSound(Gdx.files.internal(HERO_ATTACK_3_SOUND_LOCATION)),
    BowAttack -> Gdx.audio.newSound(Gdx.files.internal(BOW_SOUND_LOCATION)),
    AirDownAttack -> Gdx.audio.newSound(Gdx.files.internal(AIR_DOWN_SOUND_LOCATION)),
    Hurt -> Gdx.audio.newSound(Gdx.files.internal(HURT_SOUND_LOCATION)),
    Dying -> Gdx.audio.newSound(Gdx.files.internal(DYING_SOUND_LOCATION)),
    OpeningChest -> Gdx.audio.newSound(Gdx.files.internal(OPENING_CHEST_SOUND_LOCATION)),
    OpeningDoor -> Gdx.audio.newSound(Gdx.files.internal(OPENING_DOOR_SOUND_LOCATION)),
    PickItem -> Gdx.audio.newSound(Gdx.files.internal(PICK_ITEM_SOUND_LOCATION)),
    EnemyAttack -> Gdx.audio.newSound(Gdx.files.internal(ENEMY_ATTACK_SOUND_LOCATION)),
    EnemyDeath -> Gdx.audio.newSound(Gdx.files.internal(ENEMY_DEATH_SOUND_LOCATION))
  )

}

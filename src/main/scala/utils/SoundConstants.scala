package utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.{Music, Sound}
import view.sound.SoundEvent._

object SoundConstants {

  val LEVEL_MUSIC_LOCATION = "assets/audio/world-soundtrack.mp3"
  val BOSS_MUSIC_LOCATION = "assets/audio/boss-soundtrack.mp3"
  val JUMP_SOUND_LOCATION = "assets/audio/jump.wav"
  val HERO_ATTACK_1_SOUND_LOCATION = "assets/audio/sword1.wav"
  val HERO_ATTACK_2_SOUND_LOCATION = "assets/audio/sword2.wav"
  val HERO_ATTACK_3_SOUND_LOCATION = "assets/audio/sword3.wav"
  val BOW_SOUND_LOCATION = "assets/audio/jump.wav"
  val AIR_DOWN_SOUND_LOCATION = "assets/audio/air-down-attack.wav"
  val HURT_SOUND_LOCATION = "assets/audio/hero-hurt.wav"
  val DYING_SOUND_LOCATION = "assets/audio/hero-death.wav"
  val OPENING_DOOR_SOUND_LOCATION = "assets/audio/door-open.wav"
  val PICK_ITEM_SOUND_LOCATION = "assets/audio/item.wav"
  val ENEMY_ATTACK_SOUND_LOCATION = "assets/audio/jump.wav"
  val ENEMY_DEATH_SOUND_LOCATION = "assets/audio/enemy-death.wav"


  val POWERUP_SOUND_LOCATION = "assets/audio/powerup.wav"
  val KEY_ITEM_SOUND_LOCATION = "assets/audio/key-item.wav"

  def getMusicMap(): Map[SoundEvent, Music] = Map(
    OpeningScreenSoundtrack -> Gdx.audio.newMusic(Gdx.files.internal(LEVEL_MUSIC_LOCATION)),
    WorldSoundtrack -> Gdx.audio.newMusic(Gdx.files.internal(LEVEL_MUSIC_LOCATION)),
    BossSoundtrack -> Gdx.audio.newMusic(Gdx.files.internal(BOSS_MUSIC_LOCATION))
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
    OpeningDoor -> Gdx.audio.newSound(Gdx.files.internal(OPENING_DOOR_SOUND_LOCATION)),
    PickItem -> Gdx.audio.newSound(Gdx.files.internal(PICK_ITEM_SOUND_LOCATION)),
    EnemyAttack -> Gdx.audio.newSound(Gdx.files.internal(ENEMY_ATTACK_SOUND_LOCATION)),
    EnemyDeath -> Gdx.audio.newSound(Gdx.files.internal(ENEMY_DEATH_SOUND_LOCATION))
  )

}

package uz.angrykitten.catloop.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import uz.angrykitten.catloop.R

private enum class MusicTrack(val resId: Int) {
    Menu(R.raw.menu),
    Game(R.raw.game),
}

class AppAudioController(context: Context) {
    private val appContext = context.applicationContext
    private val loadedEffects = mutableSetOf<Int>()

    private var musicPlayer: MediaPlayer? = null
    private var currentTrack: MusicTrack? = null
    private var musicEnabled = true

    private val musicAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    private val effectsPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val bounceEffectId = effectsPool.load(appContext, R.raw.level, 1)
    private val gameOverEffectId = effectsPool.load(appContext, R.raw.lost, 1)

    init {
        effectsPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedEffects += sampleId
            }
        }
    }

    fun playMenuMusic() {
        playMusic(MusicTrack.Menu)
    }

    fun playGameMusic() {
        playMusic(MusicTrack.Game)
    }

    fun setMusicEnabled(enabled: Boolean) {
        musicEnabled = enabled
        if (enabled) {
            currentTrack?.let(::playMusic)
        } else {
            stopMusic()
        }
    }

    fun playBounceSound() {
        playEffect(bounceEffectId, volume = 0.7f)
    }

    fun playGameOverSound() {
        playEffect(gameOverEffectId, volume = 0.9f)
    }

    fun stopMusic() {
        musicPlayer?.let { player ->
            runCatching {
                if (player.isPlaying) {
                    player.stop()
                }
            }
            player.release()
        }
        musicPlayer = null
    }

    fun release() {
        stopMusic()
        effectsPool.release()
    }

    private fun playMusic(track: MusicTrack) {
        val trackChanged = currentTrack != track
        currentTrack = track
        if (!musicEnabled) return
        if (!trackChanged && musicPlayer?.isPlaying == true) return

        stopMusic()
        musicPlayer = MediaPlayer.create(appContext, track.resId)?.apply {
            setAudioAttributes(musicAttributes)
            isLooping = true
            setVolume(0.55f, 0.55f)
            start()
        }
    }

    private fun playEffect(effectId: Int, volume: Float) {
        if (!loadedEffects.contains(effectId)) return
        effectsPool.play(effectId, volume, volume, 1, 0, 1f)
    }
}

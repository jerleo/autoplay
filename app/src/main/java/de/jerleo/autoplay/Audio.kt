package de.jerleo.autoplay

import android.animation.ValueAnimator
import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.util.Log
import android.view.KeyEvent

class Audio(
    private val context: Context,
    private val settings: Settings
) {
    private var manager: AudioManager? = null

    private fun manager(): AudioManager? {
        if (manager == null)
            manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return manager
    }

    fun startPlayback(): Boolean {

        if (manager()?.isMusicActive == true) {
            Log.i(Main.TAG, "Music is active")
            return false
        }

        // Get delay in milliseconds from preference
        val delay = settings.delay() * 1000L

        // Start playback with delay
        Handler().postDelayed({ launchAudio() }, delay)

        return true
    }

    private fun launchAudio() {

        Log.i(Main.TAG, "Sending media key event")
        manager()?.let {
            it.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_MEDIA_PLAY
                )
            )
            adjustVolume()
        }
    }

    private fun adjustVolume() {

        Log.i(Main.TAG, "Adjusting volume")
        manager()?.let {
            val percentage = settings.volume()
            val maxVolume = it.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val currentVolume = it.getStreamVolume(AudioManager.STREAM_MUSIC)
            val targetVolume = percentage * maxVolume / 100

            ValueAnimator.ofInt(currentVolume, targetVolume).apply {
                duration = 3000L
                addUpdateListener { animation: ValueAnimator? ->
                    run {
                        val currentValue = animation?.animatedValue as Int
                        it.setStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            currentValue,
                            AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
                        )
                    }
                }
                this.start()
            }
        }
    }

}
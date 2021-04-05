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

    fun startPlayback(device: String): Boolean {

        if (manager()?.isMusicActive == true) {
            Log.i(Main.TAG, "Music is active")
            return false
        }

        // Get delay in milliseconds from device preference
        val delay = settings.delay(device)?.times(1000L)

        // Start playback with delay
        delay?.let { Handler().postDelayed({ launchAudio(device) }, it) }

        return true
    }

    private fun launchAudio(device: String) {

        Log.i(Main.TAG, "Sending media key event")
        manager()?.let {
            it.dispatchMediaKeyEvent(
                KeyEvent(
                    KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_MEDIA_PLAY
                )
            )
            adjustVolume(device)
        }
    }

    private fun adjustVolume(device: String) {

        Log.i(Main.TAG, "Adjusting volume")
        manager()?.let {
            val percentage = settings.volume(device)
            val maxVolume = it.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val currentVolume = it.getStreamVolume(AudioManager.STREAM_MUSIC)
            val targetVolume = (percentage?.times(maxVolume) ?: 0) / 100

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
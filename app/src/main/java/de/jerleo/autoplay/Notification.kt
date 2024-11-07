package de.jerleo.autoplay

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

class Notification(private val main: Main) {

    companion object {
        const val CHANNEL_ID = "Information"
        const val NOTIFICATION_ID = 1
    }

    val manager: NotificationManager by lazy {
        main.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @SuppressLint("MissingPermission")
    fun show(title: String, text: String) {

        if (!createChannel())
            return

        val builder = NotificationCompat.Builder(main, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_logo)
            setContentTitle(title)
            setContentText(text)
        }
        manager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun createChannel(): Boolean {

        if (!manager.areNotificationsEnabled())
            return false

        val name = CHANNEL_ID
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        manager.createNotificationChannel(channel)
        return true
    }

    fun cancel() {
        manager.cancelAll()
    }

}
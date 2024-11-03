package de.jerleo.autoplay

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class Notification(private val main: Main) {

    companion object {
        const val CHANNEL_ID = "Information"
        const val NOTIFICATION_ID = 1
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
        with(NotificationManagerCompat.from(main)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun createChannel(): Boolean {

        val notificationManager: NotificationManager =
            main.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (!notificationManager.areNotificationsEnabled())
            return false

        val name = CHANNEL_ID
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        notificationManager.createNotificationChannel(channel)
        return true
    }

}
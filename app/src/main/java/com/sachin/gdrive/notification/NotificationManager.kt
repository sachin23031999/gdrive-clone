package com.sachin.gdrive.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sachin.gdrive.R
import com.sachin.gdrive.common.log.logD
import com.sachin.gdrive.common.notificationManager

/**
 * Notification manager for the application.
 */
class NotificationManager(
    private val context: Context
) {

    private val notificationManager = NotificationManagerCompat.from(context)

    fun buildChannel() {
        val channel = NotificationChannel(
            GDRIVE_NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.notification_channel_label),
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
        logD { "Gdrive notification channel created" }
    }

    fun showUploadNotification(title: String, desc: String, progress: Int) {
        context.notificationManager.notify(
            GDRIVE_NOTIFICATION_ID,
            getCommonBuilder()
                .setContentTitle(title)
                .setOngoing(true)
                .setContentText(desc)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setProgress(100, progress, false)
                .build()
        )
    }

    fun dismissUploadNotification() {
        context.notificationManager.cancel(GDRIVE_NOTIFICATION_ID)
    }

    private fun getCommonBuilder() = NotificationCompat.Builder(
        context,
        GDRIVE_NOTIFICATION_CHANNEL_ID
    )

    companion object {
        private const val GDRIVE_NOTIFICATION_CHANNEL_ID = "GDriveNotificationChannel"
        private const val GDRIVE_NOTIFICATION_ID = 201
    }
}
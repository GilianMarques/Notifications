package dev.gmarques.notifications

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NotificationManagerApp : Application() {

    companion object {
        const val CHANNEL_ID_BLOCKED = "blocked_channel"
        const val CHANNEL_ID_SERVICE = "notification_manager_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal para notificações bloqueadas
            val blockedChannel = NotificationChannel(
                CHANNEL_ID_BLOCKED,
                getString(R.string.blocked_notifications_channel),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.blocked_notifications_channel_description)
            }

            // Canal para o serviço em primeiro plano
            val serviceChannel = NotificationChannel(
                CHANNEL_ID_SERVICE,
                getString(R.string.service_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.service_channel_description)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(blockedChannel)
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }
}
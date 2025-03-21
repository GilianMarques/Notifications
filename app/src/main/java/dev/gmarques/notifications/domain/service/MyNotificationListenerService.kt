package dev.gmarques.notifications.domain.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.gmarques.notifications.R
import dev.gmarques.notifications.domain.usecase.NotificationFilterUseCase
import dev.gmarques.notifications.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Service para monitorar notificações
 */
@AndroidEntryPoint
class MyNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var notificationFilterUseCase: NotificationFilterUseCase

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName) return

        if (notificationFilterUseCase.shouldBlockNotification(sbn)) {
            notificationFilterUseCase.processBlockedNotification(sbn)
            // Cancela a notificação bloqueada
            cancelNotification(sbn.key)
        }
    }
}

/**
 * Service em primeiro plano para garantir a execução contínua
 */
@AndroidEntryPoint
class MyNotificationForegroundService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "notification_manager_channel"
        private const val CHECK_INTERVAL = 30L // minutos
    }

    @Inject
    lateinit var notificationFilterUseCase: NotificationFilterUseCase

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        // Inicia o verificador periódico de notificações bloqueadas
        scheduleNotificationCheck()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW

            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_service_title))
            .setContentText(getString(R.string.notification_service_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun scheduleNotificationCheck() {
        serviceScope.launch {
            while (true) {
                delay(TimeUnit.MINUTES.toMillis(CHECK_INTERVAL))
                notificationFilterUseCase.processDelayedNotifications()
            }
        }
    }

    class Starter {
        companion object {
            fun startService(context: Context) {
                val intent = Intent(context, MyNotificationForegroundService::class.java)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }
    }
}

/**
 * Receiver para iniciar o serviço quando o dispositivo é reiniciado
 */
class BootCompletedReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            MyNotificationForegroundService.Starter.startService(context)
        }
    }
}
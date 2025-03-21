package dev.gmarques.notifications.domain.usecase

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.service.notification.StatusBarNotification
import dev.gmarques.notifications.data.repository.AppRepository
import dev.gmarques.notifications.domain.model.BlockedNotification
import dev.gmarques.notifications.ui.main.MainActivity
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class NotificationFilterUseCase @Inject constructor(
    private val context: Context,
    private val appRepository: AppRepository
) {

    /**
     * Verifica se uma notificação deve ser bloqueada
     */
    fun shouldBlockNotification(sbn: StatusBarNotification): Boolean {
        val packageName = sbn.packageName
        val appConfiguration = appRepository.getAppConfiguration(packageName) ?: return false

        val currentDay = LocalDate.now().dayOfWeek
        val currentTime = LocalTime.now()

        return !appConfiguration.isNotificationAllowed(currentDay, currentTime)
    }

    /**
     * Processa uma notificação bloqueada
     */
    fun processBlockedNotification(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val appInfo = appRepository.getAppInfo(packageName) ?: return

        val notification = sbn.notification
        val title = notification.extras.getString("android.title") ?: ""
        val text = notification.extras.getString("android.text") ?: ""

        val blockedNotification = BlockedNotification(
            packageName = packageName,
            appName = appInfo.appName,
            appIcon = appInfo.appIcon,
            title = title,
            content = text
        )

        appRepository.saveBlockedNotification(blockedNotification)
    }

    /**
     * Verifica e envia notificações bloqueadas que agora podem ser mostradas
     */
    fun processDelayedNotifications() {
        val managedApps = appRepository.getManagedApps()

        for (appConfig in managedApps) {
            val currentDay = LocalDate.now().dayOfWeek
            val currentTime = LocalTime.now()

            if (appConfig.isNotificationAllowed(currentDay, currentTime)) {
                val blockedNotifications = appRepository.getBlockedNotifications(appConfig.packageName)
                for (notification in blockedNotifications) {
                    sendDelayedNotification(notification)
                    appRepository.removeBlockedNotification(notification.packageName, notification.id)
                }
            }
        }
    }

    /**
     * Envia uma notificação bloqueada
     */
    private fun sendDelayedNotification(notification: BlockedNotification) {
        val notificationManager = android.app.NotificationManager::class.java
        val manager = context.getSystemService(notificationManager) as android.app.NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = android.app.Notification.Builder(context, "blocked_channel")
            .setContentTitle("[${notification.appName}] ${notification.title}")
            .setContentText(notification.content)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId("blocked_channel")
        }

        manager.notify(notification.id.hashCode(), notificationBuilder.build())
    }
}
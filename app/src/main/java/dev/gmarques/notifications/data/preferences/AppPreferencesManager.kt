package dev.gmarques.notifications.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dev.gmarques.notifications.domain.model.AppConfiguration
import dev.gmarques.notifications.domain.model.BlockedNotification
import dev.gmarques.notifications.domain.model.ListType
import dev.gmarques.notifications.utils.DrawableTypeAdapter
import dev.gmarques.notifications.utils.LocalTimeTypeAdapter
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferencesManager @Inject constructor(context: Context) {

    companion object {
        private const val PREFS_NAME = "notification_manager_prefs"
        private const val KEY_MANAGED_APPS = "managed_apps"
        private const val KEY_BLOCKED_NOTIFICATIONS_PREFIX = "blocked_notifications_"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalTime::class.java, LocalTimeTypeAdapter())
        .registerTypeAdapter(Drawable::class.java, DrawableTypeAdapter(context))
        .create()

    /**
     * Salva a configuração de um aplicativo gerenciado
     */
    fun saveAppConfiguration(appConfiguration: AppConfiguration) {
        val managedApps = getManagedApps().toMutableList()

        // Remove configuração existente se houver
        managedApps.removeIf { it.packageName == appConfiguration.packageName }

        // Adiciona a nova configuração
        managedApps.add(appConfiguration)

        // Salva a lista atualizada
        val json = gson.toJson(managedApps)
        sharedPreferences.edit().putString(KEY_MANAGED_APPS, json).apply()
    }

    /**
     * Obtém todas as configurações de aplicativos gerenciados
     */
    fun getManagedApps(): List<AppConfiguration> {
        val json = sharedPreferences.getString(KEY_MANAGED_APPS, null) ?: return emptyList()
        val type = object : TypeToken<List<AppConfiguration>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    /**
     * Remove a configuração de um aplicativo
     */
    fun removeAppConfiguration(packageName: String) {
        val managedApps = getManagedApps().toMutableList()
        managedApps.removeIf { it.packageName == packageName }

        val json = gson.toJson(managedApps)
        sharedPreferences.edit().putString(KEY_MANAGED_APPS, json).apply()
    }

    /**
     * Salva uma notificação bloqueada
     */
    fun saveBlockedNotification(notification: BlockedNotification) {
        val blockedNotifications = getBlockedNotifications(notification.packageName).toMutableList()
        blockedNotifications.add(notification)

        val json = gson.toJson(blockedNotifications)
        sharedPreferences.edit()
            .putString(KEY_BLOCKED_NOTIFICATIONS_PREFIX + notification.packageName, json)
            .apply()
    }

    /**
     * Obtém todas as notificações bloqueadas para um aplicativo específico
     */
    fun getBlockedNotifications(packageName: String): List<BlockedNotification> {
        val json = sharedPreferences.getString(KEY_BLOCKED_NOTIFICATIONS_PREFIX + packageName, null)
            ?: return emptyList()

        val type = object : TypeToken<List<BlockedNotification>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    /**
     * Remove uma notificação bloqueada
     */
    fun removeBlockedNotification(packageName: String, notificationId: String) {
        val blockedNotifications = getBlockedNotifications(packageName).toMutableList()
        blockedNotifications.removeIf { it.id == notificationId }

        val json = gson.toJson(blockedNotifications)
        sharedPreferences.edit()
            .putString(KEY_BLOCKED_NOTIFICATIONS_PREFIX + packageName, json)
            .apply()
    }

    /**
     * Remove todas as notificações bloqueadas de um aplicativo
     */
    fun clearBlockedNotifications(packageName: String) {
        sharedPreferences.edit()
            .remove(KEY_BLOCKED_NOTIFICATIONS_PREFIX + packageName)
            .apply()
    }
}
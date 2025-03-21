package dev.gmarques.notifications.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dev.gmarques.notifications.data.preferences.AppPreferencesManager
import dev.gmarques.notifications.domain.model.AppConfiguration
import dev.gmarques.notifications.domain.model.AppInfo
import dev.gmarques.notifications.domain.model.BlockedNotification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val context: Context,
    private val preferencesManager: AppPreferencesManager
) {

    private val _managedAppsFlow = MutableStateFlow<List<AppConfiguration>>(emptyList())
    val managedAppsFlow: Flow<List<AppConfiguration>> = _managedAppsFlow.asStateFlow()

    init {
        refreshManagedApps()
    }

    /**
     * Obtém a lista de todos os aplicativos instalados
     */
    fun getInstalledApps(): List<AppInfo> {
        val packageManager = context.packageManager
        val ownPackageName = context.packageName

        return packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 && it.packageName != ownPackageName }
            .map { appInfo ->
                AppInfo(
                    packageName = appInfo.packageName,
                    appName = packageManager.getApplicationLabel(appInfo).toString(),
                    appIcon = packageManager.getApplicationIcon(appInfo.packageName),
                    isManaged = preferencesManager.getManagedApps()
                        .any { it.packageName == appInfo.packageName }
                )
            }
            .sortedBy { it.appName }
    }

    /**
     * Obtém aplicativos gerenciados com suas configurações
     */
    fun getManagedApps(): List<AppConfiguration> {
        return preferencesManager.getManagedApps()
    }

    /**
     * Atualiza o fluxo de aplicativos gerenciados
     */
    fun refreshManagedApps() {
        _managedAppsFlow.value = preferencesManager.getManagedApps()
    }

    /**
     * Obtém informações de um aplicativo específico
     */
    fun getAppInfo(packageName: String): AppInfo? {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)

            AppInfo(
                packageName = packageName,
                appName = packageManager.getApplicationLabel(appInfo).toString(),
                appIcon = packageManager.getApplicationIcon(packageName),
                isManaged = preferencesManager.getManagedApps()
                    .any { it.packageName == packageName }
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    /**
     * Salva a configuração de um aplicativo
     */
    fun saveAppConfiguration(appConfiguration: AppConfiguration) {
        preferencesManager.saveAppConfiguration(appConfiguration)
        refreshManagedApps()
    }

    /**
     * Remove a configuração de um aplicativo
     */
    fun removeAppConfiguration(packageName: String) {
        preferencesManager.removeAppConfiguration(packageName)
        refreshManagedApps()
    }

    /**
     * Obtém a configuração de um aplicativo específico
     */
    fun getAppConfiguration(packageName: String): AppConfiguration? {
        return preferencesManager.getManagedApps().find { it.packageName == packageName }
    }

    /**
     * Salva uma notificação bloqueada
     */
    fun saveBlockedNotification(notification: BlockedNotification) {
        preferencesManager.saveBlockedNotification(notification)
    }

    /**
     * Obtém notificações bloqueadas de um aplicativo específico
     */
    fun getBlockedNotifications(packageName: String): List<BlockedNotification> {
        return preferencesManager.getBlockedNotifications(packageName)
    }

    /**
     * Remove uma notificação bloqueada
     */
    fun removeBlockedNotification(packageName: String, notificationId: String) {
        preferencesManager.removeBlockedNotification(packageName, notificationId)
    }

    /**
     * Remove todas as notificações bloqueadas de um aplicativo
     */
    fun clearBlockedNotifications(packageName: String) {
        preferencesManager.clearBlockedNotifications(packageName)
    }
}


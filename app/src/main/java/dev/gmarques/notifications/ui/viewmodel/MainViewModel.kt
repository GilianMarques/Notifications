package dev.gmarques.notifications.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.notifications.data.repository.AppRepository
import dev.gmarques.notifications.domain.model.AppConfiguration
import dev.gmarques.notifications.domain.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para a tela principal
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {

    private val _managedApps = MutableStateFlow<List<AppConfiguration>>(emptyList())
    val managedApps: StateFlow<List<AppConfiguration>> = _managedApps

    private val _appInfoMap = MutableStateFlow<Map<String, AppInfo>>(emptyMap())
    val appInfoMap: StateFlow<Map<String, AppInfo>> = _appInfoMap

    init {
        loadManagedApps()
    }

    fun loadManagedApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = appRepository.getManagedApps()
            _managedApps.value = apps

            // Carrega informações detalhadas dos apps
            val appMap = apps.mapNotNull { config ->
                appRepository.getAppInfo(config.packageName)?.let { appInfo ->
                    config.packageName to appInfo
                }
            }.toMap()

            _appInfoMap.value = appMap
        }
    }

    fun removeAppConfiguration(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            appRepository.removeAppConfiguration(packageName)
            loadManagedApps()
        }
    }
}






package dev.gmarques.notifications.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.notifications.data.repository.AppRepository
import dev.gmarques.notifications.domain.model.AppInfo
import dev.gmarques.notifications.domain.model.BlockedNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para a tela de notificações bloqueadas
 */
@HiltViewModel
class BlockedNotificationsViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {

    private val _appInfo = MutableLiveData<AppInfo>()
    val appInfo: LiveData<AppInfo> = _appInfo

    private val _blockedNotifications = MutableLiveData<List<BlockedNotification>>()
    val blockedNotifications: LiveData<List<BlockedNotification>> = _blockedNotifications

    fun loadBlockedNotifications(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            appRepository.getAppInfo(packageName)?.let {
                _appInfo.postValue(it)
            }

            val notifications = appRepository.getBlockedNotifications(packageName)
            _blockedNotifications.postValue(notifications)
        }
    }

    fun removeBlockedNotification(packageName: String, notificationId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            appRepository.removeBlockedNotification(packageName, notificationId)
            loadBlockedNotifications(packageName)
        }
    }

    fun clearAllBlockedNotifications(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            appRepository.clearBlockedNotifications(packageName)
            _blockedNotifications.postValue(emptyList())
        }
    }
}
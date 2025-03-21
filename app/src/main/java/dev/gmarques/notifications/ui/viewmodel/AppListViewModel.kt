package dev.gmarques.notifications.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.notifications.data.repository.AppRepository
import dev.gmarques.notifications.domain.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para a tela de lista de aplicativos
 */
@HiltViewModel
class AppListViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {

    private val _installedApps = MutableLiveData<List<AppInfo>>()
    val installedApps: LiveData<List<AppInfo>> = _installedApps

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun loadInstalledApps() {
        _loading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val apps = appRepository.getInstalledApps()
            _installedApps.postValue(apps)
            _loading.postValue(false)
        }
    }
}
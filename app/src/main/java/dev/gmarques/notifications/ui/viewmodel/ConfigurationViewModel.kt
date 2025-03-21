package dev.gmarques.notifications.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gmarques.notifications.data.repository.AppRepository
import dev.gmarques.notifications.domain.model.AppConfiguration
import dev.gmarques.notifications.domain.model.AppInfo
import dev.gmarques.notifications.domain.model.ListType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject

/**
 * ViewModel para a tela de configuração de aplicativo
 */
@HiltViewModel
class ConfigurationViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {

    private val _appInfo = MutableLiveData<AppInfo>()
    val appInfo: LiveData<AppInfo> = _appInfo

    private val _appConfiguration = MutableLiveData<AppConfiguration>()
    val appConfiguration: LiveData<AppConfiguration> = _appConfiguration

    private val _selectedDays = MutableLiveData<Set<DayOfWeek>>(emptySet())
    val selectedDays: LiveData<Set<DayOfWeek>> = _selectedDays

    private val _startTime = MutableLiveData<LocalTime>(LocalTime.of(8, 0))
    val startTime: LiveData<LocalTime> = _startTime

    private val _endTime = MutableLiveData<LocalTime>(LocalTime.of(18, 0))
    val endTime: LiveData<LocalTime> = _endTime

    private val _listType = MutableLiveData<ListType>(ListType.BLACKLIST)
    val listType: LiveData<ListType> = _listType

    fun loadAppInfo(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            appRepository.getAppInfo(packageName)?.let {
                _appInfo.postValue(it)
            }

            appRepository.getAppConfiguration(packageName)?.let { config ->

                Log.d("USUK", "ConfigurationViewModel.".plus("loadAppInfo() config = $config"))
                _appConfiguration.postValue(config)
                _selectedDays.postValue(config.scheduledDays)
                _startTime.postValue(config.startTime)
                _endTime.postValue(config.endTime)
                _listType.postValue(config.listType)
            }
        }
    }

    fun toggleDay(day: DayOfWeek) {
        val currentDays = _selectedDays.value ?: emptySet()
        val newDays = if (currentDays.contains(day)) {
            currentDays - day
        } else {
            currentDays + day
        }
        _selectedDays.value = newDays
    }

    fun setStartTime(time: LocalTime) {
        _startTime.value = time
    }

    fun setEndTime(time: LocalTime) {
        _endTime.value = time
    }

    fun setListType(type: ListType) {
        _listType.value = type
    }

    fun saveConfiguration() {
        val packageName = _appInfo.value?.packageName ?: return
        val configuration = AppConfiguration(
            packageName = packageName,
            listType = _listType.value ?: ListType.BLACKLIST,
            scheduledDays = _selectedDays.value ?: emptySet(),
            startTime = _startTime.value ?: LocalTime.of(8, 0),
            endTime = _endTime.value ?: LocalTime.of(18, 0)
        )

        viewModelScope.launch(Dispatchers.IO) {
            appRepository.saveAppConfiguration(configuration)
        }
    }
}
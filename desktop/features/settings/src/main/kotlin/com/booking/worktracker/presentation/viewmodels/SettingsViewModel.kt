package com.booking.worktracker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booking.worktracker.data.DatabaseProvider
import com.booking.worktracker.data.repository.SettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _morningTime = MutableStateFlow("10:30")
    val morningTime: StateFlow<String> = _morningTime.asStateFlow()

    private val _afternoonTime = MutableStateFlow("16:30")
    val afternoonTime: StateFlow<String> = _afternoonTime.asStateFlow()

    private val _saveMessage = MutableStateFlow<String?>(null)
    val saveMessage: StateFlow<String?> = _saveMessage.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _morningTime.value = settingsRepository.getMorningReminderTime()
            _afternoonTime.value = settingsRepository.getAfternoonReminderTime()
        }
    }

    fun setMorningTime(time: String) {
        _morningTime.value = time
    }

    fun setAfternoonTime(time: String) {
        _afternoonTime.value = time
    }

    fun saveSettings(savedMsg: String) {
        viewModelScope.launch {
            try {
                settingsRepository.setMorningReminderTime(_morningTime.value)
                settingsRepository.setAfternoonReminderTime(_afternoonTime.value)
                _saveMessage.value = savedMsg
                delay(5000)
                _saveMessage.value = null
            } catch (e: Exception) {
                _saveMessage.value = "Error: ${e.message ?: ""}"
            }
        }
    }

    fun deleteAllData(deletedMsg: String) {
        viewModelScope.launch {
            try {
                DatabaseProvider.deleteAllData()
                _saveMessage.value = deletedMsg
            } catch (e: Exception) {
                _saveMessage.value = "Error: ${e.message ?: ""}"
            }
        }
    }
}

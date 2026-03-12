package com.booking.worktracker.presentation.viewmodels

import com.booking.worktracker.data.models.TimeEntry
import com.booking.worktracker.data.repository.TimeEntryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*

class TimeTrackingViewModel(
    private val repository: TimeEntryRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(Clock.System.todayIn(TimeZone.currentSystemDefault()))
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _entries = MutableStateFlow<List<TimeEntry>>(emptyList())
    val entries: StateFlow<List<TimeEntry>> = _entries.asStateFlow()

    private val _runningEntry = MutableStateFlow<TimeEntry?>(null)
    val runningEntry: StateFlow<TimeEntry?> = _runningEntry.asStateFlow()

    private val _totalMinutes = MutableStateFlow(0)
    val totalMinutes: StateFlow<Int> = _totalMinutes.asStateFlow()

    private val _categoryBreakdown = MutableStateFlow<Map<String, Int>>(emptyMap())
    val categoryBreakdown: StateFlow<Map<String, Int>> = _categoryBreakdown.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(listOf("General", "Meeting", "Coding", "Review", "Planning"))
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val date = _selectedDate.value
            _entries.value = repository.getEntriesForDate(date)
            _runningEntry.value = repository.getRunningEntry()
            _totalMinutes.value = repository.getTotalMinutesForDate(date)
            _categoryBreakdown.value = repository.getMinutesByCategoryForDate(date)

            val savedCategories = repository.getCategories()
            val defaults = listOf("General", "Meeting", "Coding", "Review", "Planning")
            _categories.value = (defaults + savedCategories).distinct().sorted()
        }
    }

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
        loadData()
    }

    fun startTimer(description: String, category: String) {
        viewModelScope.launch {
            try {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val timeStr = "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}"
                repository.startTimer(description, category, _selectedDate.value, timeStr)
                _message.value = "Timer started!"
                loadData()
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
            }
        }
    }

    fun stopTimer() {
        viewModelScope.launch {
            val running = _runningEntry.value ?: return@launch
            try {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val timeStr = "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}"

                // Calculate duration
                val startParts = running.startTime.split(":")
                val startMinutes = startParts[0].toInt() * 60 + startParts[1].toInt()
                val endMinutes = now.hour * 60 + now.minute
                val duration = if (endMinutes >= startMinutes) endMinutes - startMinutes else (24 * 60 - startMinutes) + endMinutes

                repository.stopTimer(running.id, timeStr, duration)
                _message.value = "Timer stopped!"
                loadData()
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
            }
        }
    }

    fun addManualEntry(description: String, category: String, startTime: String, endTime: String) {
        viewModelScope.launch {
            try {
                val startParts = startTime.split(":")
                val endParts = endTime.split(":")
                val startMinutes = startParts[0].toInt() * 60 + startParts[1].toInt()
                val endMinutes = endParts[0].toInt() * 60 + endParts[1].toInt()
                val duration = if (endMinutes >= startMinutes) endMinutes - startMinutes else (24 * 60 - startMinutes) + endMinutes

                repository.addManualEntry(description, category, _selectedDate.value, startTime, endTime, duration)
                _message.value = "Entry added!"
                loadData()
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
            }
        }
    }

    fun deleteEntry(id: Int) {
        viewModelScope.launch {
            repository.delete(id)
            _message.value = "Entry deleted"
            loadData()
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun formatTotalTime(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
    }
}

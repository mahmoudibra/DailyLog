package com.booking.worktracker.presentation.viewmodels

import com.booking.worktracker.data.models.TimeEntry
import com.booking.worktracker.domain.usecases.timetracking.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*

class TimeTrackingViewModel(
    private val getTimeTrackingData: GetTimeTrackingDataUseCase = GetTimeTrackingDataUseCase(),
    private val startTimerUseCase: StartTimerUseCase = StartTimerUseCase(),
    private val stopTimerUseCase: StopTimerUseCase = StopTimerUseCase(),
    private val addManualEntryUseCase: AddManualEntryUseCase = AddManualEntryUseCase(),
    private val deleteTimeEntryUseCase: DeleteTimeEntryUseCase = DeleteTimeEntryUseCase()
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
            getTimeTrackingData(date).fold(
                onSuccess = { data ->
                    _entries.value = data.entries
                    _runningEntry.value = data.runningEntry
                    _totalMinutes.value = data.totalMinutes
                    _categoryBreakdown.value = data.categoryBreakdown
                    _categories.value = data.categories
                },
                onFailure = { _message.value = "Error: ${it.message}" }
            )
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
                startTimerUseCase(description, category, _selectedDate.value, timeStr).fold(
                    onSuccess = { _message.value = "Timer started!" },
                    onFailure = { _message.value = "Error: ${it.message}" }
                )
                loadData()
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
            }
        }
    }

    fun stopTimer(focusRating: Int? = null) {
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

                stopTimerUseCase(running.id, timeStr, duration).fold(
                    onSuccess = { _message.value = "Timer stopped!" },
                    onFailure = { _message.value = "Error: ${it.message}" }
                )
                loadData()
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
            }
        }
    }

    fun addManualEntry(description: String, category: String, startTime: String, endTime: String, focusRating: Int? = null) {
        viewModelScope.launch {
            try {
                val startParts = startTime.split(":")
                val endParts = endTime.split(":")
                val startMinutes = startParts[0].toInt() * 60 + startParts[1].toInt()
                val endMinutes = endParts[0].toInt() * 60 + endParts[1].toInt()
                val duration = if (endMinutes >= startMinutes) endMinutes - startMinutes else (24 * 60 - startMinutes) + endMinutes

                addManualEntryUseCase(description, category, _selectedDate.value, startTime, endTime, duration).fold(
                    onSuccess = { _message.value = "Entry added!" },
                    onFailure = { _message.value = "Error: ${it.message}" }
                )
                loadData()
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
            }
        }
    }

    fun deleteEntry(id: Int) {
        viewModelScope.launch {
            deleteTimeEntryUseCase(id)
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

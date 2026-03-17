package com.booking.worktracker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booking.worktracker.data.models.DailyLog
import com.booking.worktracker.data.models.Tag
import com.booking.worktracker.data.models.WorkEntry
import com.booking.worktracker.data.repository.LogRepository
import com.booking.worktracker.domain.usecases.logs.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import me.tatarka.inject.annotations.Inject

@Inject
class DailyLogViewModel(
    private val getLogForDate: GetLogForDateUseCase,
    private val addWorkEntryUseCase: AddWorkEntryUseCase,
    private val deleteWorkEntryUseCase: DeleteWorkEntryUseCase,
    private val updateLogTagsUseCase: UpdateLogTagsUseCase,
    private val getAllTagsUseCase: GetAllTagsUseCase,
    private val createTagUseCase: CreateTagUseCase,
    private val logRepository: LogRepository,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(Clock.System.todayIn(TimeZone.currentSystemDefault()))
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _workEntries = MutableStateFlow<List<WorkEntry>>(emptyList())
    val workEntries: StateFlow<List<WorkEntry>> = _workEntries.asStateFlow()

    private val _selectedTags = MutableStateFlow<Set<Tag>>(emptySet())
    val selectedTags: StateFlow<Set<Tag>> = _selectedTags.asStateFlow()

    private val _availableTags = MutableStateFlow<List<Tag>>(emptyList())
    val availableTags: StateFlow<List<Tag>> = _availableTags.asStateFlow()

    private val _saveMessage = MutableStateFlow<String?>(null)
    val saveMessage: StateFlow<String?> = _saveMessage.asStateFlow()

    private val _entryCountByDate = MutableStateFlow<Map<LocalDate, Int>>(emptyMap())
    val entryCountByDate: StateFlow<Map<LocalDate, Int>> = _entryCountByDate.asStateFlow()

    private val _streakCount = MutableStateFlow(0)
    val streakCount: StateFlow<Int> = _streakCount.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _availableTags.value = getAllTagsUseCase()
            loadLogForDate(_selectedDate.value)
        }
    }

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
        loadLogForDate(date)
        loadMonthData(date)
    }

    fun loadMonthData(date: LocalDate) {
        viewModelScope.launch {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

            // Build entry counts for the selected month
            val year = date.year
            val month = date.month
            val daysInMonth = when (month) {
                Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY,
                Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31
                Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
                Month.FEBRUARY -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
            }
            val counts = mutableMapOf<LocalDate, Int>()
            for (day in 1..daysInMonth) {
                val d = LocalDate(year, month, day)
                val dayLog = logRepository.getLogForDate(d)
                val entryCount = dayLog?.entries?.size ?: 0
                if (entryCount > 0) {
                    counts[d] = entryCount
                }
            }
            _entryCountByDate.value = counts

            // Calculate streak
            var streak = 0
            var checkDate = today
            while (true) {
                val log = logRepository.getLogForDate(checkDate)
                if (log != null && log.entries.isNotEmpty()) {
                    streak++
                    checkDate = checkDate.minus(1, DateTimeUnit.DAY)
                } else {
                    break
                }
            }
            _streakCount.value = streak
        }
    }

    private fun loadLogForDate(date: LocalDate) {
        val log = getLogForDate(date)
        if (log != null) {
            _workEntries.value = log.entries
            _selectedTags.value = log.tags.toSet()
        } else {
            _workEntries.value = emptyList()
            _selectedTags.value = emptySet()
        }
    }

    fun addWorkEntry(content: String) {
        viewModelScope.launch {
            addWorkEntryUseCase(_selectedDate.value, content).fold(
                onSuccess = { entry ->
                    _workEntries.value = _workEntries.value + entry
                    _saveMessage.value = "Entry added successfully!"
                },
                onFailure = { _saveMessage.value = "Error: ${it.message}" }
            )
        }
    }

    fun deleteWorkEntry(entryId: Int) {
        viewModelScope.launch {
            deleteWorkEntryUseCase(entryId)
            _workEntries.value = _workEntries.value.filter { it.id != entryId }
            _saveMessage.value = "Entry deleted"
        }
    }

    fun toggleTag(tag: Tag) {
        val currentTags = _selectedTags.value
        _selectedTags.value = if (tag in currentTags) {
            currentTags - tag
        } else {
            currentTags + tag
        }
        viewModelScope.launch {
            updateLogTagsUseCase(_selectedDate.value, _selectedTags.value.toList())
        }
    }

    fun createTag(name: String, color: String?) {
        viewModelScope.launch {
            createTagUseCase(name, color)
            _availableTags.value = getAllTagsUseCase()
        }
    }

    fun clearSaveMessage() {
        _saveMessage.value = null
    }
}

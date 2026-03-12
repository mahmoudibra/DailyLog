package com.booking.worktracker.presentation.viewmodels

import com.booking.worktracker.data.models.DailyLog
import com.booking.worktracker.data.models.Tag
import com.booking.worktracker.data.models.WorkEntry
import com.booking.worktracker.data.repository.LogRepository
import com.booking.worktracker.data.repository.TagRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class DailyLogViewModel(
    private val logRepository: LogRepository,
    private val tagRepository: TagRepository
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

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _availableTags.value = tagRepository.getAllTags()
            loadLogForDate(_selectedDate.value)
        }
    }

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
        loadLogForDate(date)
    }

    private fun loadLogForDate(date: LocalDate) {
        val log = logRepository.getLogForDate(date)
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
            try {
                val entry = logRepository.addWorkEntry(_selectedDate.value, content)
                _workEntries.value = _workEntries.value + entry
                _saveMessage.value = "Entry added successfully!"
            } catch (e: Exception) {
                _saveMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun deleteWorkEntry(entryId: Int) {
        viewModelScope.launch {
            logRepository.deleteWorkEntry(entryId)
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
            logRepository.updateLogTags(_selectedDate.value, _selectedTags.value.toList())
        }
    }

    fun createTag(name: String, color: String?) {
        viewModelScope.launch {
            tagRepository.createTag(name, color)
            _availableTags.value = tagRepository.getAllTags()
        }
    }

    fun clearSaveMessage() {
        _saveMessage.value = null
    }
}

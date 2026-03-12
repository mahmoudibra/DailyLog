package com.booking.worktracker.presentation.viewmodels

import com.booking.worktracker.data.models.DailyLog
import com.booking.worktracker.data.models.Tag
import com.booking.worktracker.data.models.WorkEntry
import com.booking.worktracker.domain.usecases.logs.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class DailyLogViewModel(
    private val getLogForDate: GetLogForDateUseCase,
    private val addWorkEntryUseCase: AddWorkEntryUseCase,
    private val deleteWorkEntryUseCase: DeleteWorkEntryUseCase,
    private val updateLogTagsUseCase: UpdateLogTagsUseCase,
    private val getAllTagsUseCase: GetAllTagsUseCase,
    private val createTagUseCase: CreateTagUseCase
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
            _availableTags.value = getAllTagsUseCase()
            loadLogForDate(_selectedDate.value)
        }
    }

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
        loadLogForDate(date)
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

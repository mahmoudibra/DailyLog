package com.booking.worktracker.presentation.viewmodels

import com.booking.worktracker.data.models.Objective
import com.booking.worktracker.data.models.ObjectiveStatus
import com.booking.worktracker.data.models.ObjectiveType
import com.booking.worktracker.data.repository.ObjectiveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ObjectivesViewModel(
    private val objectiveRepository: ObjectiveRepository
) : ViewModel() {

    private val _objectives = MutableStateFlow<List<Objective>>(emptyList())
    val objectives: StateFlow<List<Objective>> = _objectives.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    private val _selectedYear = MutableStateFlow(now.year)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedQuarter = MutableStateFlow((now.monthNumber - 1) / 3 + 1)
    val selectedQuarter: StateFlow<Int> = _selectedQuarter.asStateFlow()

    init {
        loadObjectives()
    }

    fun setTab(tab: Int) {
        _selectedTab.value = tab
        loadObjectives()
    }

    fun setYear(year: Int) {
        _selectedYear.value = year
        loadObjectives()
    }

    fun setQuarter(quarter: Int) {
        _selectedQuarter.value = quarter
        loadObjectives()
    }

    fun loadObjectives() {
        viewModelScope.launch {
            _objectives.value = when (_selectedTab.value) {
                0 -> objectiveRepository.getYearlyObjectives(_selectedYear.value)
                1 -> objectiveRepository.getQuarterlyObjectives(_selectedYear.value, _selectedQuarter.value)
                else -> emptyList()
            }
        }
    }

    fun createObjective(title: String, description: String, type: ObjectiveType, year: Int, quarter: Int?) {
        viewModelScope.launch {
            objectiveRepository.createObjective(title, description, type, year, quarter)
            loadObjectives()
        }
    }

    fun updateObjective(id: Int, title: String, description: String, status: ObjectiveStatus) {
        viewModelScope.launch {
            objectiveRepository.updateObjective(id, title, description, status)
            loadObjectives()
        }
    }

    fun deleteObjective(id: Int) {
        viewModelScope.launch {
            objectiveRepository.deleteObjective(id)
            loadObjectives()
        }
    }

    fun addChecklistItem(objectiveId: Int, text: String) {
        viewModelScope.launch {
            objectiveRepository.addChecklistItem(objectiveId, text)
            loadObjectives()
        }
    }

    fun toggleChecklistItem(itemId: Int) {
        viewModelScope.launch {
            objectiveRepository.toggleChecklistItem(itemId)
            loadObjectives()
        }
    }

    fun deleteChecklistItem(itemId: Int) {
        viewModelScope.launch {
            objectiveRepository.deleteChecklistItem(itemId)
            loadObjectives()
        }
    }
}

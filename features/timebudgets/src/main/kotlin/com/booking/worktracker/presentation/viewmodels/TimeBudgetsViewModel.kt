package com.booking.worktracker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booking.worktracker.data.models.BudgetProgress
import com.booking.worktracker.data.models.PeriodType
import com.booking.worktracker.domain.usecases.timebudgets.*
import me.tatarka.inject.annotations.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Inject
class TimeBudgetsViewModel(
    private val getDashboard: GetTimeBudgetDashboardUseCase,
    private val createBudget: CreateTimeBudgetUseCase,
    private val updateBudget: UpdateTimeBudgetUseCase,
    private val deleteBudget: DeleteTimeBudgetUseCase,
) : ViewModel() {

    private val _budgetProgress = MutableStateFlow<List<BudgetProgress>>(emptyList())
    val budgetProgress: StateFlow<List<BudgetProgress>> = _budgetProgress.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _activeObjectives = MutableStateFlow<List<Pair<Int, String>>>(emptyList())
    val activeObjectives: StateFlow<List<Pair<Int, String>>> = _activeObjectives.asStateFlow()

    private val _periodElapsedFraction = MutableStateFlow(0f)
    val periodElapsedFraction: StateFlow<Float> = _periodElapsedFraction.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            getDashboard().fold(
                onSuccess = { data ->
                    _budgetProgress.value = data.budgetProgress
                    _categories.value = data.categories
                    _activeObjectives.value = data.activeObjectives
                    _periodElapsedFraction.value = data.periodElapsedFraction
                },
                onFailure = { _message.value = "Error: ${it.message}" }
            )
        }
    }

    fun addBudget(category: String, targetMinutes: Int, periodType: PeriodType, objectiveId: Int?) {
        viewModelScope.launch {
            createBudget(category, targetMinutes, periodType, objectiveId).fold(
                onSuccess = { _message.value = "Budget created!" },
                onFailure = { _message.value = "Error: ${it.message}" }
            )
            loadData()
        }
    }

    fun editBudget(id: Int, category: String, targetMinutes: Int, periodType: PeriodType, objectiveId: Int?) {
        viewModelScope.launch {
            updateBudget(id, category, targetMinutes, periodType, objectiveId).fold(
                onSuccess = { _message.value = "Budget updated!" },
                onFailure = { _message.value = "Error: ${it.message}" }
            )
            loadData()
        }
    }

    fun removeBudget(id: Int) {
        viewModelScope.launch {
            deleteBudget(id).fold(
                onSuccess = { _message.value = "Budget deleted" },
                onFailure = { _message.value = "Error: ${it.message}" }
            )
            loadData()
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun formatMinutes(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
    }
}

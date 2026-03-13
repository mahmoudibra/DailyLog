package com.booking.worktracker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booking.worktracker.data.models.*
import com.booking.worktracker.domain.usecases.habits.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import me.tatarka.inject.annotations.Inject

@Inject
class HabitsViewModel(
    private val getAllHabitsUseCase: GetAllHabitsUseCase,
    private val createHabitUseCase: CreateHabitUseCase,
    private val updateHabitUseCase: UpdateHabitUseCase,
    private val archiveHabitUseCase: ArchiveHabitUseCase,
    private val toggleCompletionUseCase: ToggleHabitCompletionUseCase,
    private val getHabitStreaksUseCase: GetHabitStreaksUseCase,
    private val getCompletedHabitIdsUseCase: GetCompletedHabitIdsUseCase,
    private val getWeeklyScoreUseCase: GetWeeklyScoreUseCase,
) : ViewModel() {

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _habitStreaks = MutableStateFlow<List<HabitStreak>>(emptyList())
    val habitStreaks: StateFlow<List<HabitStreak>> = _habitStreaks.asStateFlow()

    private val _completedToday = MutableStateFlow<Set<Int>>(emptySet())
    val completedToday: StateFlow<Set<Int>> = _completedToday.asStateFlow()

    private val _weeklyScore = MutableStateFlow<HabitWeeklyScore?>(null)
    val weeklyScore: StateFlow<HabitWeeklyScore?> = _weeklyScore.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedDate = MutableStateFlow(Clock.System.todayIn(TimeZone.currentSystemDefault()))
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

            getAllHabitsUseCase().onSuccess { _habits.value = it }
            getHabitStreaksUseCase().onSuccess { _habitStreaks.value = it }
            getCompletedHabitIdsUseCase(today).onSuccess { _completedToday.value = it }

            // Weekly score: Monday to Sunday of current week
            val dayOfWeek = today.dayOfWeek.isoDayNumber // Monday=1, Sunday=7
            val weekStart = today.minus(dayOfWeek - 1, DateTimeUnit.DAY)
            val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)
            getWeeklyScoreUseCase(weekStart, weekEnd).onSuccess { _weeklyScore.value = it }

            _isLoading.value = false
        }
    }

    fun toggleHabitCompletion(habitId: Int) {
        viewModelScope.launch {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            toggleCompletionUseCase(habitId, today).onSuccess { completed ->
                val current = _completedToday.value.toMutableSet()
                if (completed) current.add(habitId) else current.remove(habitId)
                _completedToday.value = current
                // Refresh streaks and weekly score
                getHabitStreaksUseCase().onSuccess { _habitStreaks.value = it }
                val dayOfWeek = today.dayOfWeek.isoDayNumber
                val weekStart = today.minus(dayOfWeek - 1, DateTimeUnit.DAY)
                val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)
                getWeeklyScoreUseCase(weekStart, weekEnd).onSuccess { _weeklyScore.value = it }
            }
        }
    }

    fun createHabit(name: String, icon: String?, color: String?, objectiveId: Int?) {
        viewModelScope.launch {
            createHabitUseCase(name, icon, color, objectiveId).onSuccess {
                loadData()
            }
        }
    }

    fun updateHabit(id: Int, name: String, icon: String?, color: String?, objectiveId: Int?) {
        viewModelScope.launch {
            updateHabitUseCase(id, name, icon, color, objectiveId).onSuccess {
                loadData()
            }
        }
    }

    fun archiveHabit(id: Int) {
        viewModelScope.launch {
            archiveHabitUseCase(id).onSuccess {
                loadData()
            }
        }
    }
}

package com.booking.worktracker.presentation.viewmodels

import com.booking.worktracker.data.models.AutoSummary
import com.booking.worktracker.data.models.DailyReview
import com.booking.worktracker.data.models.WeeklySummary
import com.booking.worktracker.domain.usecases.reviews.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*

class ReviewsViewModel(
    private val getReviewForDate: GetReviewForDateUseCase = GetReviewForDateUseCase(),
    private val saveDailyReview: SaveDailyReviewUseCase = SaveDailyReviewUseCase(),
    private val loadWeeklySummaryUseCase: LoadWeeklySummaryUseCase = LoadWeeklySummaryUseCase(),
    private val saveWeeklySummaryUseCase: SaveWeeklySummaryUseCase = SaveWeeklySummaryUseCase(),
    private val generateAutoSummary: GenerateAutoSummaryUseCase = GenerateAutoSummaryUseCase()
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(Clock.System.todayIn(TimeZone.currentSystemDefault()))
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentReview = MutableStateFlow<DailyReview?>(null)
    val currentReview: StateFlow<DailyReview?> = _currentReview.asStateFlow()

    private val _isWeeklyView = MutableStateFlow(false)
    val isWeeklyView: StateFlow<Boolean> = _isWeeklyView.asStateFlow()

    private val _wentWellText = MutableStateFlow("")
    val wentWellText: StateFlow<String> = _wentWellText.asStateFlow()

    private val _couldImproveText = MutableStateFlow("")
    val couldImproveText: StateFlow<String> = _couldImproveText.asStateFlow()

    private val _tomorrowPriorityText = MutableStateFlow("")
    val tomorrowPriorityText: StateFlow<String> = _tomorrowPriorityText.asStateFlow()

    private val _weeklySummaryText = MutableStateFlow("")
    val weeklySummaryText: StateFlow<String> = _weeklySummaryText.asStateFlow()

    private val _autoSummary = MutableStateFlow<AutoSummary?>(null)
    val autoSummary: StateFlow<AutoSummary?> = _autoSummary.asStateFlow()

    private val _currentWeeklySummary = MutableStateFlow<WeeklySummary?>(null)
    val currentWeeklySummary: StateFlow<WeeklySummary?> = _currentWeeklySummary.asStateFlow()

    private val _saveMessage = MutableStateFlow<String?>(null)
    val saveMessage: StateFlow<String?> = _saveMessage.asStateFlow()

    private val _weekStartDate = MutableStateFlow(getWeekStart(Clock.System.todayIn(TimeZone.currentSystemDefault())))
    val weekStartDate: StateFlow<LocalDate> = _weekStartDate.asStateFlow()

    init {
        loadReviewForDate(_selectedDate.value)
    }

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
        loadReviewForDate(date)
    }

    fun updateWentWell(text: String) { _wentWellText.value = text }
    fun updateCouldImprove(text: String) { _couldImproveText.value = text }
    fun updateTomorrowPriority(text: String) { _tomorrowPriorityText.value = text }
    fun updateWeeklySummaryText(text: String) { _weeklySummaryText.value = text }

    fun toggleView() {
        _isWeeklyView.value = !_isWeeklyView.value
        if (_isWeeklyView.value) {
            loadWeeklySummary(_weekStartDate.value)
        }
    }

    fun setWeeklyView(isWeekly: Boolean) {
        _isWeeklyView.value = isWeekly
        if (isWeekly) {
            loadWeeklySummary(_weekStartDate.value)
        }
    }

    fun navigateToPreviousWeek() {
        val newStart = _weekStartDate.value.minus(DatePeriod(days = 7))
        _weekStartDate.value = newStart
        loadWeeklySummary(newStart)
    }

    fun navigateToNextWeek() {
        val newStart = _weekStartDate.value.plus(DatePeriod(days = 7))
        _weekStartDate.value = newStart
        loadWeeklySummary(newStart)
    }

    private fun loadReviewForDate(date: LocalDate) {
        viewModelScope.launch {
            val review = getReviewForDate(date.toString())
            _currentReview.value = review
            _wentWellText.value = review?.wentWell ?: ""
            _couldImproveText.value = review?.couldImprove ?: ""
            _tomorrowPriorityText.value = review?.tomorrowPriority ?: ""
        }
    }

    fun saveDailyReview() {
        viewModelScope.launch {
            val wentWell = _wentWellText.value.takeIf { it.isNotBlank() }
            val couldImprove = _couldImproveText.value.takeIf { it.isNotBlank() }
            val tomorrowPriority = _tomorrowPriorityText.value.takeIf { it.isNotBlank() }

            saveDailyReview(_selectedDate.value.toString(), wentWell, couldImprove, tomorrowPriority).fold(
                onSuccess = { review ->
                    _currentReview.value = review
                    _saveMessage.value = "Review saved!"
                },
                onFailure = { _saveMessage.value = "Error: ${it.message}" }
            )
        }
    }

    fun loadWeeklySummary(weekStart: LocalDate) {
        viewModelScope.launch {
            _weekStartDate.value = weekStart
            val weekEnd = weekStart.plus(DatePeriod(days = 6))

            val summary = loadWeeklySummaryUseCase(weekStart.toString())
            _currentWeeklySummary.value = summary
            _weeklySummaryText.value = summary?.summaryText ?: ""

            generateAutoSummary(weekStart, weekEnd).fold(
                onSuccess = { _autoSummary.value = it },
                onFailure = { _autoSummary.value = AutoSummary(0, 0, 0, 0, emptyList(), 0) }
            )
        }
    }

    fun saveWeeklySummary() {
        viewModelScope.launch {
            val weekStart = _weekStartDate.value
            val weekEnd = weekStart.plus(DatePeriod(days = 6))
            val summaryText = _weeklySummaryText.value.takeIf { it.isNotBlank() }

            val autoSummary = _autoSummary.value
            val autoSummaryJson = if (autoSummary != null) {
                buildString {
                    append("{")
                    append("\"entryCount\":${autoSummary.entryCount},")
                    append("\"timeTrackedMinutes\":${autoSummary.timeTrackedMinutes},")
                    append("\"objectivesProgressed\":${autoSummary.objectivesProgressed},")
                    append("\"streakDays\":${autoSummary.streakDays},")
                    append("\"topTags\":[${autoSummary.topTags.joinToString(",") { "\"$it\"" }}],")
                    append("\"dailyReviewCount\":${autoSummary.dailyReviewCount}")
                    append("}")
                }
            } else null

            saveWeeklySummaryUseCase(weekStart.toString(), weekEnd.toString(), summaryText, autoSummaryJson).fold(
                onSuccess = { summary ->
                    _currentWeeklySummary.value = summary
                    _saveMessage.value = "Weekly summary saved!"
                },
                onFailure = { _saveMessage.value = "Error: ${it.message}" }
            )
        }
    }

    fun clearSaveMessage() {
        _saveMessage.value = null
    }

    companion object {
        fun getWeekStart(date: LocalDate): LocalDate {
            val dayOfWeek = date.dayOfWeek
            val daysFromMonday = dayOfWeek.ordinal // Monday = 0
            return date.minus(DatePeriod(days = daysFromMonday))
        }
    }
}

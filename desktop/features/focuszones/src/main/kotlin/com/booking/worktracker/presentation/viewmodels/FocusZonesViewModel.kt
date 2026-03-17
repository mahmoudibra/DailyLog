package com.booking.worktracker.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booking.worktracker.data.models.*
import com.booking.worktracker.domain.usecases.focuszones.GetFocusSummaryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import me.tatarka.inject.annotations.Inject

@Inject
class FocusZonesViewModel(
    private val getFocusSummary: GetFocusSummaryUseCase,
) : ViewModel() {

    private val _focusSummary = MutableStateFlow<FocusSummary?>(null)
    val focusSummary: StateFlow<FocusSummary?> = _focusSummary.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _weeksBack = MutableStateFlow(4)
    val weeksBack: StateFlow<Int> = _weeksBack.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val startDate = today.minus(DatePeriod(days = _weeksBack.value * 7))
            getFocusSummary(startDate, today).fold(
                onSuccess = { _focusSummary.value = it },
                onFailure = { /* summary stays null */ }
            )
            _isLoading.value = false
        }
    }

    fun setWeeksBack(weeks: Int) {
        _weeksBack.value = weeks
        loadData()
    }

    companion object {
        fun formatHour(hour: Int): String {
            val h = hour % 24
            return when {
                h == 0 -> "12am"
                h < 12 -> "${h}am"
                h == 12 -> "12pm"
                else -> "${h - 12}pm"
            }
        }
    }
}

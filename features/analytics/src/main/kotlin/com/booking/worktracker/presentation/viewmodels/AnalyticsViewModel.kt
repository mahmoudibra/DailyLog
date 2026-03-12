package com.booking.worktracker.presentation.viewmodels

import com.booking.worktracker.data.models.AnalyticsSummary
import com.booking.worktracker.domain.usecases.analytics.GetAnalyticsSummaryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val getAnalyticsSummary: GetAnalyticsSummaryUseCase = GetAnalyticsSummaryUseCase()
) : ViewModel() {

    private val _summary = MutableStateFlow<AnalyticsSummary?>(null)
    val summary: StateFlow<AnalyticsSummary?> = _summary.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            getAnalyticsSummary().fold(
                onSuccess = { _summary.value = it },
                onFailure = { println("Error loading analytics: ${it.message}") }
            )
            _isLoading.value = false
        }
    }
}

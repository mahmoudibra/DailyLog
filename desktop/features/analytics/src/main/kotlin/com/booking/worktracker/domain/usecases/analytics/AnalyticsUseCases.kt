package com.booking.worktracker.domain.usecases.analytics

import com.booking.worktracker.data.models.AnalyticsSummary
import com.booking.worktracker.data.repository.AnalyticsRepository
import me.tatarka.inject.annotations.Inject

@Inject
class GetAnalyticsSummaryUseCase(private val repository: AnalyticsRepository) {
    operator fun invoke(): Result<AnalyticsSummary> {
        return try {
            Result.success(repository.getFullSummary())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

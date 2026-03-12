package com.booking.worktracker.domain.usecases.focuszones

import com.booking.worktracker.data.models.*
import com.booking.worktracker.data.repository.FocusZonesRepository
import com.booking.worktracker.presentation.viewmodels.FocusZonesViewModel
import kotlinx.datetime.LocalDate

class GetFocusSummaryUseCase(private val repository: FocusZonesRepository) {
    operator fun invoke(startDate: LocalDate, endDate: LocalDate): Result<FocusSummary> {
        return try {
            val heatmapData = repository.getHourlyFocusData(startDate, endDate)
            val totalRated = repository.getTotalRatedEntries(startDate, endDate)
            val avgRating = repository.getOverallAverageRating(startDate, endDate)
            val categoryAvgs = repository.getAverageFocusByCategory(startDate, endDate)

            val patterns = detectPatterns(heatmapData)

            val recommendations = categoryAvgs.keys.mapNotNull { category ->
                repository.getBestHoursForCategory(category, startDate, endDate)?.let { (hour, rating) ->
                    CategoryRecommendation(
                        category = category,
                        bestHourStart = hour,
                        bestHourEnd = (hour + 1).coerceAtMost(23),
                        averageRating = rating,
                        description = "Best time for $category: ${FocusZonesViewModel.formatHour(hour)}-${FocusZonesViewModel.formatHour(hour + 1)}"
                    )
                }
            }.sortedByDescending { it.averageRating }

            Result.success(FocusSummary(
                totalRatedEntries = totalRated,
                averageFocusRating = avgRating,
                patterns = patterns,
                recommendations = recommendations,
                heatmapData = heatmapData
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun detectPatterns(data: List<HourlyFocusData>): List<FocusPattern> {
        if (data.isEmpty()) return emptyList()

        val hourlyAverages = data
            .groupBy { it.hour }
            .mapValues { (_, entries) ->
                entries.sumOf { it.averageRating * it.entryCount } / entries.sumOf { it.entryCount }
            }
            .toSortedMap()

        if (hourlyAverages.isEmpty()) return emptyList()

        val overallAvg = hourlyAverages.values.average()

        val patterns = mutableListOf<FocusPattern>()
        var peakStart: Int? = null
        var peakRatings = mutableListOf<Double>()

        for (hour in 0..23) {
            val rating = hourlyAverages[hour]
            if (rating != null && rating > overallAvg) {
                if (peakStart == null) peakStart = hour
                peakRatings.add(rating)
            } else {
                if (peakStart != null && peakRatings.size >= 1) {
                    val avgPeak = peakRatings.average()
                    val endHour = peakStart + peakRatings.size
                    patterns.add(FocusPattern(
                        peakHourStart = peakStart,
                        peakHourEnd = endHour.coerceAtMost(23),
                        averagePeakRating = avgPeak,
                        description = "Deep work best ${FocusZonesViewModel.formatHour(peakStart)}-${FocusZonesViewModel.formatHour(endHour)}"
                    ))
                }
                peakStart = null
                peakRatings = mutableListOf()
            }
        }
        if (peakStart != null && peakRatings.size >= 1) {
            val avgPeak = peakRatings.average()
            val endHour = peakStart + peakRatings.size
            patterns.add(FocusPattern(
                peakHourStart = peakStart,
                peakHourEnd = endHour.coerceAtMost(23),
                averagePeakRating = avgPeak,
                description = "Deep work best ${FocusZonesViewModel.formatHour(peakStart)}-${FocusZonesViewModel.formatHour(endHour)}"
            ))
        }

        return patterns.sortedByDescending { it.averagePeakRating }
    }
}

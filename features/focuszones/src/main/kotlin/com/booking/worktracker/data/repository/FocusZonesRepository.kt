package com.booking.worktracker.data.repository

import com.booking.worktracker.data.datasource.FocusZonesLocalDataSource
import com.booking.worktracker.data.models.HourlyFocusData
import com.booking.worktracker.di.Singleton
import kotlinx.datetime.LocalDate
import me.tatarka.inject.annotations.Inject

@Inject
@Singleton
class FocusZonesRepository(private val localDataSource: FocusZonesLocalDataSource) {
    fun getHourlyFocusData(startDate: LocalDate, endDate: LocalDate): List<HourlyFocusData> =
        localDataSource.getHourlyFocusData(startDate, endDate)

    fun getAverageFocusByCategory(startDate: LocalDate, endDate: LocalDate): Map<String, Double> =
        localDataSource.getAverageFocusByCategory(startDate, endDate)

    fun getBestHoursForCategory(category: String, startDate: LocalDate, endDate: LocalDate): Pair<Int, Double>? =
        localDataSource.getBestHoursForCategory(category, startDate, endDate)

    fun getTotalRatedEntries(startDate: LocalDate, endDate: LocalDate): Int =
        localDataSource.getTotalRatedEntries(startDate, endDate)

    fun getOverallAverageRating(startDate: LocalDate, endDate: LocalDate): Double =
        localDataSource.getOverallAverageRating(startDate, endDate)
}

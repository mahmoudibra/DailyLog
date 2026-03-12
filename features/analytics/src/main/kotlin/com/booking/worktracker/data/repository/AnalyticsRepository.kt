package com.booking.worktracker.data.repository

import com.booking.worktracker.data.datasource.AnalyticsLocalDataSource
import com.booking.worktracker.data.models.*

class AnalyticsRepository(
    private val localDataSource: AnalyticsLocalDataSource = AnalyticsLocalDataSource()
) {
    fun getTotalEntries(): Int = localDataSource.getTotalEntries()
    fun getAverageEntriesPerDay(): Double = localDataSource.getAverageEntriesPerDay()
    fun getMostActiveDay(): String? = localDataSource.getMostActiveDay()
    fun getRecentDailyStats(limit: Int = 30): List<DailyStats> = localDataSource.getRecentDailyStats(limit)
    fun getWeeklyStats(weeks: Int = 12): List<WeeklyStats> = localDataSource.getWeeklyStats(weeks)
    fun getTagStats(): List<TagStats> = localDataSource.getTagStats()
    fun getObjectiveStats(): ObjectiveStats = localDataSource.getObjectiveStats()
    fun getStreakInfo(): StreakInfo = localDataSource.getStreakInfo()

    fun getFullSummary(): AnalyticsSummary {
        return AnalyticsSummary(
            streakInfo = getStreakInfo(),
            totalEntries = getTotalEntries(),
            averageEntriesPerDay = getAverageEntriesPerDay(),
            mostActiveDay = getMostActiveDay(),
            recentDailyStats = getRecentDailyStats(),
            weeklyStats = getWeeklyStats(),
            tagStats = getTagStats(),
            objectiveStats = getObjectiveStats()
        )
    }
}

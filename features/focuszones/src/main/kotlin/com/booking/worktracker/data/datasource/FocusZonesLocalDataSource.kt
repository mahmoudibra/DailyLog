package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.DatabaseProvider
import com.booking.worktracker.data.DailyWorkTrackerDatabase
import com.booking.worktracker.data.models.HourlyFocusData
import kotlinx.datetime.LocalDate

class FocusZonesLocalDataSource(db: DailyWorkTrackerDatabase = DatabaseProvider.getDatabase()) {

    private val queries = db.focusZonesQueries

    fun getHourlyFocusData(startDate: LocalDate, endDate: LocalDate): List<HourlyFocusData> {
        return queries.getHourlyFocusData(startDate.toString(), endDate.toString()).executeAsList().map { row ->
            // SQLite strftime('%w') returns 0=Sunday, 1=Monday, etc.
            // Convert to 1=Monday...7=Sunday
            val sqliteDow = row.day_of_week.toInt()
            val dow = if (sqliteDow == 0) 7 else sqliteDow
            HourlyFocusData(
                dayOfWeek = dow,
                hour = row.hour.toInt(),
                averageRating = row.avg_rating ?: 0.0,
                entryCount = row.entry_count.toInt()
            )
        }
    }

    fun getAverageFocusByCategory(startDate: LocalDate, endDate: LocalDate): Map<String, Double> {
        return queries.getAverageFocusByCategory(startDate.toString(), endDate.toString()).executeAsList().associate { row ->
            row.category to (row.avg_rating ?: 0.0)
        }
    }

    fun getBestHoursForCategory(category: String, startDate: LocalDate, endDate: LocalDate): Pair<Int, Double>? {
        val row = queries.getBestHoursForCategory(category, startDate.toString(), endDate.toString())
            .executeAsOneOrNull() ?: return null
        return Pair(row.hour.toInt(), row.avg_rating ?: 0.0)
    }

    fun getTotalRatedEntries(startDate: LocalDate, endDate: LocalDate): Int {
        return queries.getTotalRatedEntries(startDate.toString(), endDate.toString()).executeAsOne().toInt()
    }

    fun getOverallAverageRating(startDate: LocalDate, endDate: LocalDate): Double {
        return queries.getOverallAverageRating(startDate.toString(), endDate.toString()).executeAsOne()
    }
}

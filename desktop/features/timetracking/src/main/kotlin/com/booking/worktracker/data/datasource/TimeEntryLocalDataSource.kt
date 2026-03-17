package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.DailyTrackerDatabase
import com.booking.worktracker.data.models.TimeEntry
import com.booking.worktracker.di.Singleton
import kotlinx.datetime.LocalDate
import me.tatarka.inject.annotations.Inject

@Inject
@Singleton
class TimeEntryLocalDataSource(db: DailyTrackerDatabase) {

    private val queries = db.timeEntriesQueries
    private val dailyLogsQueries = db.dailyLogsQueries

    fun getEntriesForDate(date: LocalDate): List<TimeEntry> {
        return queries.getEntriesForDate(date.toString()).executeAsList().map { it.toTimeEntry() }
    }

    fun getRunningEntry(): TimeEntry? {
        return queries.getRunningEntry().executeAsOneOrNull()?.toTimeEntry()
    }

    fun startTimer(description: String, category: String, date: LocalDate, startTime: String): TimeEntry {
        val entryId = queries.transactionWithResult {
            queries.startTimer(description, category, startTime, date.toString())
            dailyLogsQueries.lastInsertRowId().executeAsOne()
        }
        return queries.getById(entryId).executeAsOne().toTimeEntry()
    }

    fun stopTimer(id: Int, endTime: String, durationMinutes: Int, focusRating: Int?): TimeEntry {
        if (focusRating != null) {
            queries.stopTimerWithRating(endTime, durationMinutes.toLong(), focusRating.toLong(), id.toLong())
        } else {
            queries.stopTimer(endTime, durationMinutes.toLong(), id.toLong())
        }
        return queries.getById(id.toLong()).executeAsOne().toTimeEntry()
    }

    fun addManualEntry(description: String, category: String, date: LocalDate, startTime: String, endTime: String, durationMinutes: Int, focusRating: Int?): TimeEntry {
        val entryId = queries.transactionWithResult {
            if (focusRating != null) {
                queries.addManualEntryWithRating(description, category, startTime, endTime, durationMinutes.toLong(), date.toString(), focusRating.toLong())
            } else {
                queries.addManualEntry(description, category, startTime, endTime, durationMinutes.toLong(), date.toString())
            }
            dailyLogsQueries.lastInsertRowId().executeAsOne()
        }
        return queries.getById(entryId).executeAsOne().toTimeEntry()
    }

    fun delete(id: Int) {
        queries.deleteEntry(id.toLong())
    }

    fun getCategories(): List<String> {
        return queries.getDistinctCategories().executeAsList()
    }

    fun getTotalMinutesForDate(date: LocalDate): Int {
        return queries.getTotalMinutesForDate(date.toString()).executeAsOne().toInt()
    }

    fun getMinutesByCategoryForDate(date: LocalDate): Map<String, Int> {
        return queries.getMinutesByCategoryForDate(date.toString()).executeAsList()
            .associate { it.category to it.total.toInt() }
    }

    fun updateFocusRating(id: Int, rating: Int) {
        queries.updateFocusRating(rating.toLong(), id.toLong())
    }

    fun getEntriesWithFocusRatingForDateRange(startDate: LocalDate, endDate: LocalDate): List<TimeEntry> {
        return queries.getEntriesWithFocusRatingForDateRange(startDate.toString(), endDate.toString())
            .executeAsList().map { row ->
                TimeEntry(
                    id = row.id.toInt(),
                    description = row.description,
                    category = row.category,
                    startTime = row.start_time,
                    endTime = row.end_time,
                    durationMinutes = row.duration_minutes.toInt(),
                    date = row.date,
                    createdAt = row.created_at,
                    focusRating = row.focus_rating.toInt()
                )
            }
    }

    private fun com.booking.worktracker.data.Time_entries.toTimeEntry() = TimeEntry(
        id = id.toInt(),
        description = description,
        category = category,
        startTime = start_time,
        endTime = end_time,
        durationMinutes = duration_minutes.toInt(),
        date = date,
        createdAt = created_at,
        focusRating = focus_rating?.toInt()
    )
}

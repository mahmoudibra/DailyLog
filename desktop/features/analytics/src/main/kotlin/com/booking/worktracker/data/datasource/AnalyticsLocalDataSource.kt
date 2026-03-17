package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.DailyTrackerDatabase
import com.booking.worktracker.data.models.*
import com.booking.worktracker.di.Singleton
import kotlinx.datetime.*
import me.tatarka.inject.annotations.Inject

@Inject
@Singleton
class AnalyticsLocalDataSource(db: DailyTrackerDatabase) {

    private val queries = db.analyticsQueries

    fun getTotalEntries(): Int {
        return queries.getTotalEntries().executeAsOne().toInt()
    }

    fun getAverageEntriesPerDay(): Double {
        val avg = queries.getAverageEntriesPerDay().executeAsOne()
        return avg ?: 0.0
    }

    fun getMostActiveDay(): String? {
        val row = queries.getMostActiveDay().executeAsOneOrNull() ?: return null
        return row.date
    }

    fun getRecentDailyStats(limit: Int = 30): List<DailyStats> {
        return queries.getRecentDailyStats(limit.toLong()).executeAsList().map { row ->
            DailyStats(
                date = row.date,
                entryCount = row.entry_count.toInt(),
                tagCount = row.tag_count.toInt()
            )
        }
    }

    fun getWeeklyStats(weeks: Int = 12): List<WeeklyStats> {
        return queries.getWeeklyStats(weeks.toLong()).executeAsList().map { row ->
            WeeklyStats(
                weekStart = row.week_start ?: "",
                weekEnd = row.week_end ?: "",
                totalEntries = row.total_entries.toInt(),
                activeDays = row.active_days.toInt()
            )
        }
    }

    fun getTagStats(): List<TagStats> {
        return queries.getTagStats().executeAsList().map { row ->
            TagStats(
                tagName = row.name,
                tagColor = row.color,
                usageCount = row.usage_count.toInt()
            )
        }
    }

    fun getObjectiveStats(): ObjectiveStats {
        val row = queries.getObjectiveStats().executeAsOne()
        val avgCompletion = queries.getChecklistCompletion().executeAsOne()

        return ObjectiveStats(
            totalObjectives = row.total.toInt(),
            completedObjectives = (row.completed ?: 0L).toInt(),
            inProgressObjectives = (row.in_progress ?: 0L).toInt(),
            cancelledObjectives = (row.cancelled ?: 0L).toInt(),
            averageChecklistCompletion = (avgCompletion ?: 0L).toInt()
        )
    }

    fun getStreakInfo(): StreakInfo {
        val dates = queries.getAllDatesWithEntries().executeAsList()

        if (dates.isEmpty()) {
            return StreakInfo(currentStreak = 0, longestStreak = 0, totalDaysLogged = 0)
        }

        // Calculate current streak and longest streak
        var currentStreak = 1
        var longestStreak = 1
        var tempStreak = 1
        var isCurrentStreak = true

        // Check if the most recent date is today or yesterday for current streak
        val todayDate = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        val today = todayDate.toString()
        val yesterday = todayDate.minus(1, DateTimeUnit.DAY).toString()

        if (dates.first() != today && dates.first() != yesterday) {
            currentStreak = 0
            isCurrentStreak = false
        }

        for (i in 1 until dates.size) {
            val current = LocalDate.parse(dates[i - 1])
            val previous = LocalDate.parse(dates[i])
            val diff = current.toEpochDays() - previous.toEpochDays()

            if (diff == 1) {
                tempStreak++
                if (isCurrentStreak) currentStreak = tempStreak
            } else {
                isCurrentStreak = false
                tempStreak = 1
            }
            if (tempStreak > longestStreak) longestStreak = tempStreak
        }

        return StreakInfo(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalDaysLogged = dates.size
        )
    }
}

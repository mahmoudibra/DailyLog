package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.Database
import com.booking.worktracker.data.models.*
import kotlinx.datetime.*

class AnalyticsLocalDataSource {

    fun getTotalEntries(): Int {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement("SELECT COUNT(*) as total FROM work_entries")
        val rs = stmt.executeQuery()
        rs.next()
        val total = rs.getInt("total")
        rs.close()
        stmt.close()
        return total
    }

    fun getAverageEntriesPerDay(): Double {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            """
            SELECT CAST(COUNT(we.id) AS REAL) / COUNT(DISTINCT dl.date) as avg_entries
            FROM work_entries we
            INNER JOIN daily_logs dl ON we.daily_log_id = dl.id
            """
        )
        val rs = stmt.executeQuery()
        rs.next()
        val avg = rs.getDouble("avg_entries")
        rs.close()
        stmt.close()
        return if (avg.isNaN()) 0.0 else avg
    }

    fun getMostActiveDay(): String? {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            """
            SELECT dl.date, COUNT(we.id) as entry_count
            FROM daily_logs dl
            INNER JOIN work_entries we ON dl.id = we.daily_log_id
            GROUP BY dl.date
            ORDER BY entry_count DESC
            LIMIT 1
            """
        )
        val rs = stmt.executeQuery()
        val day = if (rs.next()) rs.getString("date") else null
        rs.close()
        stmt.close()
        return day
    }

    fun getRecentDailyStats(limit: Int = 30): List<DailyStats> {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            """
            SELECT dl.date,
                   COUNT(DISTINCT we.id) as entry_count,
                   COUNT(DISTINCT lt.tag_id) as tag_count
            FROM daily_logs dl
            LEFT JOIN work_entries we ON dl.id = we.daily_log_id
            LEFT JOIN log_tags lt ON dl.id = lt.daily_log_id
            GROUP BY dl.date
            ORDER BY dl.date DESC
            LIMIT ?
            """
        )
        stmt.setInt(1, limit)
        val rs = stmt.executeQuery()

        val stats = mutableListOf<DailyStats>()
        while (rs.next()) {
            stats.add(
                DailyStats(
                    date = rs.getString("date"),
                    entryCount = rs.getInt("entry_count"),
                    tagCount = rs.getInt("tag_count")
                )
            )
        }
        rs.close()
        stmt.close()
        return stats
    }

    fun getWeeklyStats(weeks: Int = 12): List<WeeklyStats> {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            """
            SELECT
                date(dl.date, 'weekday 0', '-6 days') as week_start,
                date(dl.date, 'weekday 0') as week_end,
                COUNT(DISTINCT we.id) as total_entries,
                COUNT(DISTINCT dl.date) as active_days
            FROM daily_logs dl
            LEFT JOIN work_entries we ON dl.id = we.daily_log_id
            WHERE we.id IS NOT NULL
            GROUP BY week_start
            ORDER BY week_start DESC
            LIMIT ?
            """
        )
        stmt.setInt(1, weeks)
        val rs = stmt.executeQuery()

        val stats = mutableListOf<WeeklyStats>()
        while (rs.next()) {
            stats.add(
                WeeklyStats(
                    weekStart = rs.getString("week_start"),
                    weekEnd = rs.getString("week_end"),
                    totalEntries = rs.getInt("total_entries"),
                    activeDays = rs.getInt("active_days")
                )
            )
        }
        rs.close()
        stmt.close()
        return stats
    }

    fun getTagStats(): List<TagStats> {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            """
            SELECT t.name, t.color, COUNT(lt.daily_log_id) as usage_count
            FROM tags t
            LEFT JOIN log_tags lt ON t.id = lt.tag_id
            GROUP BY t.id
            ORDER BY usage_count DESC
            """
        )
        val rs = stmt.executeQuery()

        val stats = mutableListOf<TagStats>()
        while (rs.next()) {
            stats.add(
                TagStats(
                    tagName = rs.getString("name"),
                    tagColor = rs.getString("color"),
                    usageCount = rs.getInt("usage_count")
                )
            )
        }
        rs.close()
        stmt.close()
        return stats
    }

    fun getObjectiveStats(): ObjectiveStats {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            """
            SELECT
                COUNT(*) as total,
                SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed,
                SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as in_progress,
                SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled
            FROM objectives
            """
        )
        val rs = stmt.executeQuery()
        rs.next()
        val total = rs.getInt("total")
        val completed = rs.getInt("completed")
        val inProgress = rs.getInt("in_progress")
        val cancelled = rs.getInt("cancelled")
        rs.close()
        stmt.close()

        // Average checklist completion
        val clStmt = conn.prepareStatement(
            """
            SELECT
                CASE WHEN COUNT(*) = 0 THEN 0
                ELSE (SUM(CASE WHEN completed = 1 THEN 1 ELSE 0 END) * 100) / COUNT(*)
                END as avg_completion
            FROM checklist_items
            """
        )
        val clRs = clStmt.executeQuery()
        clRs.next()
        val avgCompletion = clRs.getInt("avg_completion")
        clRs.close()
        clStmt.close()

        return ObjectiveStats(
            totalObjectives = total,
            completedObjectives = completed,
            inProgressObjectives = inProgress,
            cancelledObjectives = cancelled,
            averageChecklistCompletion = avgCompletion
        )
    }

    fun getStreakInfo(): StreakInfo {
        val conn = Database.getConnection()

        // Get all dates with entries, ordered descending
        val stmt = conn.prepareStatement(
            """
            SELECT DISTINCT dl.date
            FROM daily_logs dl
            INNER JOIN work_entries we ON dl.id = we.daily_log_id
            ORDER BY dl.date DESC
            """
        )
        val rs = stmt.executeQuery()

        val dates = mutableListOf<String>()
        while (rs.next()) {
            dates.add(rs.getString("date"))
        }
        rs.close()
        stmt.close()

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

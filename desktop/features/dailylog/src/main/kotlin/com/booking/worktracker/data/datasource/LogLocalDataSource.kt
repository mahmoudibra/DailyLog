package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.DatabaseProvider
import com.booking.worktracker.data.DailyTrackerDatabase
import com.booking.worktracker.data.models.DailyLog
import com.booking.worktracker.data.models.Tag
import com.booking.worktracker.data.models.WorkEntry
import kotlinx.datetime.LocalDate
import me.tatarka.inject.annotations.Inject
import com.booking.worktracker.di.Singleton

@Inject
@Singleton
class LogLocalDataSource(db: DailyTrackerDatabase) {

    private val dailyLogsQueries = db.dailyLogsQueries
    private val tagsQueries = db.tagsQueries

    fun getLogForDate(date: LocalDate): DailyLog? {
        val row = dailyLogsQueries.getLogForDate(date.toString()).executeAsOneOrNull() ?: return null
        val logId = row.id
        return DailyLog(
            id = logId.toInt(),
            date = row.date,
            entries = getEntriesForLog(logId),
            tags = getTagsForLog(logId),
            createdAt = row.created_at,
            updatedAt = row.updated_at
        )
    }

    fun addWorkEntry(date: LocalDate, content: String): WorkEntry {
        val logId = getOrCreateLogId(date.toString())
        return dailyLogsQueries.transactionWithResult {
            dailyLogsQueries.insertWorkEntry(logId, content)
            val entryId = dailyLogsQueries.lastInsertRowId().executeAsOne()
            dailyLogsQueries.updateLogTimestamp(logId)
            val row = dailyLogsQueries.getWorkEntryById(entryId).executeAsOne()
            WorkEntry(
                id = row.id.toInt(),
                dailyLogId = row.daily_log_id.toInt(),
                content = row.content,
                createdAt = row.created_at
            )
        }
    }

    fun deleteWorkEntry(entryId: Int) {
        dailyLogsQueries.deleteWorkEntry(entryId.toLong())
    }

    fun updateLogTags(date: LocalDate, tags: List<Tag>) {
        val logId = getOrCreateLogId(date.toString())
        tagsQueries.deleteLogTags(logId)
        tags.forEach { tag ->
            tagsQueries.insertLogTag(logId, tag.id.toLong())
        }
        dailyLogsQueries.updateLogTimestamp(logId)
    }

    fun getAllLogs(limit: Int = 50): List<DailyLog> {
        return dailyLogsQueries.getAllLogs(limit.toLong()).executeAsList().map { row ->
            val logId = row.id
            DailyLog(
                id = logId.toInt(),
                date = row.date,
                entries = getEntriesForLog(logId),
                tags = getTagsForLog(logId),
                createdAt = row.created_at,
                updatedAt = row.updated_at
            )
        }
    }

    fun getLogForDateRange(start: LocalDate, end: LocalDate): List<DailyLog>? {
        val logs = dailyLogsQueries.getLogsForDateRange(start.toString(), end.toString()).executeAsList().map { row ->
            val logId = row.id
            DailyLog(
                id = logId.toInt(),
                date = row.date,
                entries = getEntriesForLog(logId),
                tags = getTagsForLog(logId),
                createdAt = row.created_at,
                updatedAt = row.updated_at
            )
        }
        return logs.ifEmpty { null }
    }

    fun hasLogForDate(date: LocalDate): Boolean {
        return dailyLogsQueries.hasLogForDate(date.toString()).executeAsOneOrNull() != null
    }

    private fun getOrCreateLogId(dateStr: String): Long {
        val existing = dailyLogsQueries.getLogForDate(dateStr).executeAsOneOrNull()
        if (existing != null) return existing.id
        return dailyLogsQueries.transactionWithResult {
            dailyLogsQueries.insertLog(dateStr)
            dailyLogsQueries.lastInsertRowId().executeAsOne()
        }
    }

    private fun getEntriesForLog(logId: Long): List<WorkEntry> {
        return dailyLogsQueries.getEntriesForLog(logId).executeAsList().map { row ->
            WorkEntry(
                id = row.id.toInt(),
                dailyLogId = row.daily_log_id.toInt(),
                content = row.content,
                createdAt = row.created_at
            )
        }
    }

    private fun getTagsForLog(logId: Long): List<Tag> {
        return tagsQueries.getTagsForLog(logId).executeAsList().map { row ->
            Tag(
                id = row.id.toInt(),
                name = row.name,
                color = row.color,
                createdAt = row.created_at
            )
        }
    }
}

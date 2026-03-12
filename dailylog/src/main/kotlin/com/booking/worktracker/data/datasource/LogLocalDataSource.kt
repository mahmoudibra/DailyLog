package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.Database
import com.booking.worktracker.data.models.DailyLog
import com.booking.worktracker.data.models.Tag
import com.booking.worktracker.data.models.WorkEntry
import kotlinx.datetime.LocalDate

class LogLocalDataSource {

    fun getLogForDate(date: LocalDate): DailyLog? {
        val conn = Database.getConnection()
        val dateStr = date.toString()

        val logStmt = conn.prepareStatement("SELECT id, date, created_at, updated_at FROM daily_logs WHERE date = ?")
        logStmt.setString(1, dateStr)
        val logRs = logStmt.executeQuery()

        if (!logRs.next()) {
            logRs.close()
            logStmt.close()
            return null
        }

        val logId = logRs.getInt("id")
        val logDate = logRs.getString("date")
        val createdAt = logRs.getString("created_at")
        val updatedAt = logRs.getString("updated_at")
        logRs.close()
        logStmt.close()

        val entries = getEntriesForLog(logId)
        val tags = getTagsForLog(logId)

        return DailyLog(
            id = logId,
            date = logDate,
            entries = entries,
            tags = tags,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun addWorkEntry(date: LocalDate, content: String): WorkEntry {
        val conn = Database.getConnection()
        val dateStr = date.toString()

        val logId = getOrCreateLogId(dateStr)

        val stmt = conn.prepareStatement(
            "INSERT INTO work_entries (daily_log_id, content) VALUES (?, ?)"
        )
        stmt.setInt(1, logId)
        stmt.setString(2, content)
        stmt.executeUpdate()
        stmt.close()

        val idRs = conn.createStatement().executeQuery("SELECT last_insert_rowid()")
        idRs.next()
        val entryId = idRs.getInt(1)
        idRs.close()

        val updateStmt = conn.prepareStatement("UPDATE daily_logs SET updated_at = datetime('now') WHERE id = ?")
        updateStmt.setInt(1, logId)
        updateStmt.executeUpdate()
        updateStmt.close()

        val readStmt = conn.prepareStatement("SELECT id, daily_log_id, content, created_at FROM work_entries WHERE id = ?")
        readStmt.setInt(1, entryId)
        val rs = readStmt.executeQuery()
        rs.next()
        val entry = WorkEntry(
            id = rs.getInt("id"),
            dailyLogId = rs.getInt("daily_log_id"),
            content = rs.getString("content"),
            createdAt = rs.getString("created_at")
        )
        rs.close()
        readStmt.close()
        return entry
    }

    fun deleteWorkEntry(entryId: Int) {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement("DELETE FROM work_entries WHERE id = ?")
        stmt.setInt(1, entryId)
        stmt.executeUpdate()
        stmt.close()
    }

    fun updateLogTags(date: LocalDate, tags: List<Tag>) {
        val conn = Database.getConnection()
        val dateStr = date.toString()
        val logId = getOrCreateLogId(dateStr)

        val deleteStmt = conn.prepareStatement("DELETE FROM log_tags WHERE daily_log_id = ?")
        deleteStmt.setInt(1, logId)
        deleteStmt.executeUpdate()
        deleteStmt.close()

        if (tags.isNotEmpty()) {
            val insertStmt = conn.prepareStatement("INSERT INTO log_tags (daily_log_id, tag_id) VALUES (?, ?)")
            for (tag in tags) {
                insertStmt.setInt(1, logId)
                insertStmt.setInt(2, tag.id)
                insertStmt.addBatch()
            }
            insertStmt.executeBatch()
            insertStmt.close()
        }

        val updateStmt = conn.prepareStatement("UPDATE daily_logs SET updated_at = datetime('now') WHERE id = ?")
        updateStmt.setInt(1, logId)
        updateStmt.executeUpdate()
        updateStmt.close()
    }

    fun getAllLogs(limit: Int = 50): List<DailyLog> {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "SELECT id, date, created_at, updated_at FROM daily_logs ORDER BY date DESC LIMIT ?"
        )
        stmt.setInt(1, limit)
        val rs = stmt.executeQuery()

        val logs = mutableListOf<DailyLog>()
        while (rs.next()) {
            val logId = rs.getInt("id")
            logs.add(
                DailyLog(
                    id = logId,
                    date = rs.getString("date"),
                    entries = getEntriesForLog(logId),
                    tags = getTagsForLog(logId),
                    createdAt = rs.getString("created_at"),
                    updatedAt = rs.getString("updated_at")
                )
            )
        }
        rs.close()
        stmt.close()
        return logs
    }

    fun getLogForDateRange(start: LocalDate, end: LocalDate): List<DailyLog>? {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "SELECT id, date, created_at, updated_at FROM daily_logs WHERE date BETWEEN ? AND ? ORDER BY date DESC"
        )
        stmt.setString(1, start.toString())
        stmt.setString(2, end.toString())
        val rs = stmt.executeQuery()

        val logs = mutableListOf<DailyLog>()
        while (rs.next()) {
            val logId = rs.getInt("id")
            logs.add(
                DailyLog(
                    id = logId,
                    date = rs.getString("date"),
                    entries = getEntriesForLog(logId),
                    tags = getTagsForLog(logId),
                    createdAt = rs.getString("created_at"),
                    updatedAt = rs.getString("updated_at")
                )
            )
        }
        rs.close()
        stmt.close()
        return logs.ifEmpty { null }
    }

    fun hasLogForDate(date: LocalDate): Boolean {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "SELECT dl.id FROM daily_logs dl INNER JOIN work_entries we ON dl.id = we.daily_log_id WHERE dl.date = ? LIMIT 1"
        )
        stmt.setString(1, date.toString())
        val rs = stmt.executeQuery()
        val hasLog = rs.next()
        rs.close()
        stmt.close()
        return hasLog
    }

    private fun getOrCreateLogId(dateStr: String): Int {
        val conn = Database.getConnection()

        val checkStmt = conn.prepareStatement("SELECT id FROM daily_logs WHERE date = ?")
        checkStmt.setString(1, dateStr)
        val checkRs = checkStmt.executeQuery()

        if (checkRs.next()) {
            val id = checkRs.getInt("id")
            checkRs.close()
            checkStmt.close()
            return id
        }
        checkRs.close()
        checkStmt.close()

        val insertStmt = conn.prepareStatement(
            "INSERT INTO daily_logs (date) VALUES (?)"
        )
        insertStmt.setString(1, dateStr)
        insertStmt.executeUpdate()
        insertStmt.close()

        val idRs = conn.createStatement().executeQuery("SELECT last_insert_rowid()")
        idRs.next()
        val id = idRs.getInt(1)
        idRs.close()
        return id
    }

    private fun getEntriesForLog(logId: Int): List<WorkEntry> {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "SELECT id, daily_log_id, content, created_at FROM work_entries WHERE daily_log_id = ? ORDER BY created_at ASC"
        )
        stmt.setInt(1, logId)
        val rs = stmt.executeQuery()

        val entries = mutableListOf<WorkEntry>()
        while (rs.next()) {
            entries.add(
                WorkEntry(
                    id = rs.getInt("id"),
                    dailyLogId = rs.getInt("daily_log_id"),
                    content = rs.getString("content"),
                    createdAt = rs.getString("created_at")
                )
            )
        }
        rs.close()
        stmt.close()
        return entries
    }

    private fun getTagsForLog(logId: Int): List<Tag> {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            """
            SELECT t.id, t.name, t.color, t.created_at
            FROM tags t
            INNER JOIN log_tags lt ON t.id = lt.tag_id
            WHERE lt.daily_log_id = ?
            ORDER BY t.name
            """
        )
        stmt.setInt(1, logId)
        val rs = stmt.executeQuery()

        val tags = mutableListOf<Tag>()
        while (rs.next()) {
            tags.add(
                Tag(
                    id = rs.getInt("id"),
                    name = rs.getString("name"),
                    color = rs.getString("color"),
                    createdAt = rs.getString("created_at")
                )
            )
        }
        rs.close()
        stmt.close()
        return tags
    }
}

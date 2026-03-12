package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.Database
import com.booking.worktracker.data.models.TimeEntry
import kotlinx.datetime.LocalDate

class TimeEntryLocalDataSource {

    fun getEntriesForDate(date: LocalDate): List<TimeEntry> {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "SELECT id, description, category, start_time, end_time, duration_minutes, date, focus_rating, created_at FROM time_entries WHERE date = ? ORDER BY start_time DESC"
        )
        stmt.setString(1, date.toString())
        val rs = stmt.executeQuery()

        val entries = mutableListOf<TimeEntry>()
        while (rs.next()) {
            entries.add(mapTimeEntry(rs))
        }
        rs.close()
        stmt.close()
        return entries
    }

    fun getRunningEntry(): TimeEntry? {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "SELECT id, description, category, start_time, end_time, duration_minutes, date, focus_rating, created_at FROM time_entries WHERE end_time IS NULL LIMIT 1"
        )
        val rs = stmt.executeQuery()
        val entry = if (rs.next()) mapTimeEntry(rs) else null
        rs.close()
        stmt.close()
        return entry
    }

    fun startTimer(description: String, category: String, date: LocalDate, startTime: String): TimeEntry {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "INSERT INTO time_entries (description, category, start_time, date) VALUES (?, ?, ?, ?)"
        )
        stmt.setString(1, description)
        stmt.setString(2, category)
        stmt.setString(3, startTime)
        stmt.setString(4, date.toString())
        stmt.executeUpdate()
        stmt.close()

        val idRs = conn.createStatement().executeQuery("SELECT last_insert_rowid()")
        idRs.next()
        val entryId = idRs.getInt(1)
        idRs.close()

        return getById(entryId)!!
    }

    fun stopTimer(id: Int, endTime: String, durationMinutes: Int, focusRating: Int? = null): TimeEntry {
        val conn = Database.getConnection()
        val stmt = if (focusRating != null) {
            conn.prepareStatement(
                "UPDATE time_entries SET end_time = ?, duration_minutes = ?, focus_rating = ? WHERE id = ?"
            ).apply {
                setString(1, endTime)
                setInt(2, durationMinutes)
                setInt(3, focusRating)
                setInt(4, id)
            }
        } else {
            conn.prepareStatement(
                "UPDATE time_entries SET end_time = ?, duration_minutes = ? WHERE id = ?"
            ).apply {
                setString(1, endTime)
                setInt(2, durationMinutes)
                setInt(3, id)
            }
        }
        stmt.executeUpdate()
        stmt.close()

        return getById(id)!!
    }

    fun addManualEntry(description: String, category: String, date: LocalDate, startTime: String, endTime: String, durationMinutes: Int, focusRating: Int? = null): TimeEntry {
        val conn = Database.getConnection()
        val stmt = if (focusRating != null) {
            conn.prepareStatement(
                "INSERT INTO time_entries (description, category, start_time, end_time, duration_minutes, date, focus_rating) VALUES (?, ?, ?, ?, ?, ?, ?)"
            ).apply {
                setString(1, description)
                setString(2, category)
                setString(3, startTime)
                setString(4, endTime)
                setInt(5, durationMinutes)
                setString(6, date.toString())
                setInt(7, focusRating)
            }
        } else {
            conn.prepareStatement(
                "INSERT INTO time_entries (description, category, start_time, end_time, duration_minutes, date) VALUES (?, ?, ?, ?, ?, ?)"
            ).apply {
                setString(1, description)
                setString(2, category)
                setString(3, startTime)
                setString(4, endTime)
                setInt(5, durationMinutes)
                setString(6, date.toString())
            }
        }
        stmt.executeUpdate()
        stmt.close()

        val idRs = conn.createStatement().executeQuery("SELECT last_insert_rowid()")
        idRs.next()
        val entryId = idRs.getInt(1)
        idRs.close()

        return getById(entryId)!!
    }

    fun delete(id: Int) {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement("DELETE FROM time_entries WHERE id = ?")
        stmt.setInt(1, id)
        stmt.executeUpdate()
        stmt.close()
    }

    fun getCategories(): List<String> {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement("SELECT DISTINCT category FROM time_entries ORDER BY category")
        val rs = stmt.executeQuery()
        val categories = mutableListOf<String>()
        while (rs.next()) {
            categories.add(rs.getString("category"))
        }
        rs.close()
        stmt.close()
        return categories
    }

    fun getTotalMinutesForDate(date: LocalDate): Int {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "SELECT COALESCE(SUM(duration_minutes), 0) as total FROM time_entries WHERE date = ? AND end_time IS NOT NULL"
        )
        stmt.setString(1, date.toString())
        val rs = stmt.executeQuery()
        rs.next()
        val total = rs.getInt("total")
        rs.close()
        stmt.close()
        return total
    }

    fun getMinutesByCategoryForDate(date: LocalDate): Map<String, Int> {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "SELECT category, COALESCE(SUM(duration_minutes), 0) as total FROM time_entries WHERE date = ? AND end_time IS NOT NULL GROUP BY category ORDER BY total DESC"
        )
        stmt.setString(1, date.toString())
        val rs = stmt.executeQuery()
        val result = mutableMapOf<String, Int>()
        while (rs.next()) {
            result[rs.getString("category")] = rs.getInt("total")
        }
        rs.close()
        stmt.close()
        return result
    }

    private fun getById(id: Int): TimeEntry? {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "SELECT id, description, category, start_time, end_time, duration_minutes, date, focus_rating, created_at FROM time_entries WHERE id = ?"
        )
        stmt.setInt(1, id)
        val rs = stmt.executeQuery()
        val entry = if (rs.next()) mapTimeEntry(rs) else null
        rs.close()
        stmt.close()
        return entry
    }

    fun updateFocusRating(id: Int, rating: Int) {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "UPDATE time_entries SET focus_rating = ? WHERE id = ?"
        )
        stmt.setInt(1, rating)
        stmt.setInt(2, id)
        stmt.executeUpdate()
        stmt.close()
    }

    fun getEntriesWithFocusRatingForDateRange(startDate: LocalDate, endDate: LocalDate): List<TimeEntry> {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "SELECT id, description, category, start_time, end_time, duration_minutes, date, focus_rating, created_at FROM time_entries WHERE date >= ? AND date <= ? AND focus_rating IS NOT NULL ORDER BY date, start_time"
        )
        stmt.setString(1, startDate.toString())
        stmt.setString(2, endDate.toString())
        val rs = stmt.executeQuery()

        val entries = mutableListOf<TimeEntry>()
        while (rs.next()) {
            entries.add(mapTimeEntry(rs))
        }
        rs.close()
        stmt.close()
        return entries
    }

    private fun mapTimeEntry(rs: java.sql.ResultSet): TimeEntry {
        return TimeEntry(
            id = rs.getInt("id"),
            description = rs.getString("description"),
            category = rs.getString("category"),
            startTime = rs.getString("start_time"),
            endTime = rs.getString("end_time"),
            durationMinutes = rs.getInt("duration_minutes"),
            date = rs.getString("date"),
            createdAt = rs.getString("created_at"),
            focusRating = rs.getObject("focus_rating") as? Int
        )
    }
}

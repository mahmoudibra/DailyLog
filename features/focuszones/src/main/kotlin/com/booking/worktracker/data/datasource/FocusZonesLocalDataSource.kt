package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.Database
import com.booking.worktracker.data.models.HourlyFocusData
import kotlinx.datetime.LocalDate

class FocusZonesLocalDataSource {

    fun getHourlyFocusData(startDate: LocalDate, endDate: LocalDate): List<HourlyFocusData> {
        val conn = Database.getConnection()
        // Get day of week and hour from start_time, grouped
        // start_time is stored as "HH:MM" format, date is "YYYY-MM-DD"
        val stmt = conn.prepareStatement("""
            SELECT
                CAST(strftime('%w', date) AS INTEGER) as day_of_week,
                CAST(substr(start_time, 1, 2) AS INTEGER) as hour,
                AVG(focus_rating) as avg_rating,
                COUNT(*) as entry_count
            FROM time_entries
            WHERE focus_rating IS NOT NULL
                AND date >= ? AND date <= ?
                AND end_time IS NOT NULL
            GROUP BY day_of_week, hour
            ORDER BY day_of_week, hour
        """)
        stmt.setString(1, startDate.toString())
        stmt.setString(2, endDate.toString())
        val rs = stmt.executeQuery()

        val data = mutableListOf<HourlyFocusData>()
        while (rs.next()) {
            // SQLite strftime('%w') returns 0=Sunday, 1=Monday, etc.
            // Convert to 1=Monday...7=Sunday
            val sqliteDow = rs.getInt("day_of_week")
            val dow = if (sqliteDow == 0) 7 else sqliteDow
            data.add(HourlyFocusData(
                dayOfWeek = dow,
                hour = rs.getInt("hour"),
                averageRating = rs.getDouble("avg_rating"),
                entryCount = rs.getInt("entry_count")
            ))
        }
        rs.close()
        stmt.close()
        return data
    }

    fun getAverageFocusByCategory(startDate: LocalDate, endDate: LocalDate): Map<String, Double> {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement("""
            SELECT category, AVG(focus_rating) as avg_rating
            FROM time_entries
            WHERE focus_rating IS NOT NULL
                AND date >= ? AND date <= ?
                AND end_time IS NOT NULL
            GROUP BY category
            ORDER BY avg_rating DESC
        """)
        stmt.setString(1, startDate.toString())
        stmt.setString(2, endDate.toString())
        val rs = stmt.executeQuery()

        val result = mutableMapOf<String, Double>()
        while (rs.next()) {
            result[rs.getString("category")] = rs.getDouble("avg_rating")
        }
        rs.close()
        stmt.close()
        return result
    }

    fun getBestHoursForCategory(category: String, startDate: LocalDate, endDate: LocalDate): Pair<Int, Double>? {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement("""
            SELECT
                CAST(substr(start_time, 1, 2) AS INTEGER) as hour,
                AVG(focus_rating) as avg_rating,
                COUNT(*) as cnt
            FROM time_entries
            WHERE focus_rating IS NOT NULL
                AND category = ?
                AND date >= ? AND date <= ?
                AND end_time IS NOT NULL
            GROUP BY hour
            HAVING cnt >= 2
            ORDER BY avg_rating DESC
            LIMIT 1
        """)
        stmt.setString(1, category)
        stmt.setString(2, startDate.toString())
        stmt.setString(3, endDate.toString())
        val rs = stmt.executeQuery()

        val result = if (rs.next()) {
            Pair(rs.getInt("hour"), rs.getDouble("avg_rating"))
        } else null
        rs.close()
        stmt.close()
        return result
    }

    fun getTotalRatedEntries(startDate: LocalDate, endDate: LocalDate): Int {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement("""
            SELECT COUNT(*) as total
            FROM time_entries
            WHERE focus_rating IS NOT NULL
                AND date >= ? AND date <= ?
                AND end_time IS NOT NULL
        """)
        stmt.setString(1, startDate.toString())
        stmt.setString(2, endDate.toString())
        val rs = stmt.executeQuery()
        rs.next()
        val total = rs.getInt("total")
        rs.close()
        stmt.close()
        return total
    }

    fun getOverallAverageRating(startDate: LocalDate, endDate: LocalDate): Double {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement("""
            SELECT COALESCE(AVG(focus_rating), 0.0) as avg_rating
            FROM time_entries
            WHERE focus_rating IS NOT NULL
                AND date >= ? AND date <= ?
                AND end_time IS NOT NULL
        """)
        stmt.setString(1, startDate.toString())
        stmt.setString(2, endDate.toString())
        val rs = stmt.executeQuery()
        rs.next()
        val avg = rs.getDouble("avg_rating")
        rs.close()
        stmt.close()
        return avg
    }
}

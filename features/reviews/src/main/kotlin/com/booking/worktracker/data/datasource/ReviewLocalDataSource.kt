package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.Database
import com.booking.worktracker.data.models.DailyReview
import com.booking.worktracker.data.models.WeeklySummary

class ReviewLocalDataSource {

    fun getReviewForDate(date: String): DailyReview? {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "SELECT id, date, went_well, could_improve, tomorrow_priority, created_at, updated_at FROM daily_reviews WHERE date = ?"
        )
        stmt.setString(1, date)
        val rs = stmt.executeQuery()

        if (!rs.next()) {
            rs.close()
            stmt.close()
            return null
        }

        val review = DailyReview(
            id = rs.getInt("id"),
            date = rs.getString("date"),
            wentWell = rs.getString("went_well"),
            couldImprove = rs.getString("could_improve"),
            tomorrowPriority = rs.getString("tomorrow_priority"),
            createdAt = rs.getString("created_at"),
            updatedAt = rs.getString("updated_at")
        )
        rs.close()
        stmt.close()
        return review
    }

    fun insertOrUpdateReview(
        date: String,
        wentWell: String?,
        couldImprove: String?,
        tomorrowPriority: String?
    ): DailyReview {
        val conn = Database.getConnection()

        val existing = getReviewForDate(date)
        if (existing != null) {
            val updateStmt = conn.prepareStatement(
                """
                UPDATE daily_reviews
                SET went_well = ?, could_improve = ?, tomorrow_priority = ?, updated_at = datetime('now')
                WHERE date = ?
                """
            )
            updateStmt.setString(1, wentWell)
            updateStmt.setString(2, couldImprove)
            updateStmt.setString(3, tomorrowPriority)
            updateStmt.setString(4, date)
            updateStmt.executeUpdate()
            updateStmt.close()
        } else {
            val insertStmt = conn.prepareStatement(
                """
                INSERT INTO daily_reviews (date, went_well, could_improve, tomorrow_priority)
                VALUES (?, ?, ?, ?)
                """
            )
            insertStmt.setString(1, date)
            insertStmt.setString(2, wentWell)
            insertStmt.setString(3, couldImprove)
            insertStmt.setString(4, tomorrowPriority)
            insertStmt.executeUpdate()
            insertStmt.close()
        }

        return getReviewForDate(date)!!
    }

    fun deleteReview(date: String) {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement("DELETE FROM daily_reviews WHERE date = ?")
        stmt.setString(1, date)
        stmt.executeUpdate()
        stmt.close()
    }

    fun getReviewsForDateRange(startDate: String, endDate: String): List<DailyReview> {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "SELECT id, date, went_well, could_improve, tomorrow_priority, created_at, updated_at FROM daily_reviews WHERE date BETWEEN ? AND ? ORDER BY date DESC"
        )
        stmt.setString(1, startDate)
        stmt.setString(2, endDate)
        val rs = stmt.executeQuery()

        val reviews = mutableListOf<DailyReview>()
        while (rs.next()) {
            reviews.add(
                DailyReview(
                    id = rs.getInt("id"),
                    date = rs.getString("date"),
                    wentWell = rs.getString("went_well"),
                    couldImprove = rs.getString("could_improve"),
                    tomorrowPriority = rs.getString("tomorrow_priority"),
                    createdAt = rs.getString("created_at"),
                    updatedAt = rs.getString("updated_at")
                )
            )
        }
        rs.close()
        stmt.close()
        return reviews
    }

    fun getAllReviews(): List<DailyReview> {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "SELECT id, date, went_well, could_improve, tomorrow_priority, created_at, updated_at FROM daily_reviews ORDER BY date DESC"
        )
        val rs = stmt.executeQuery()

        val reviews = mutableListOf<DailyReview>()
        while (rs.next()) {
            reviews.add(
                DailyReview(
                    id = rs.getInt("id"),
                    date = rs.getString("date"),
                    wentWell = rs.getString("went_well"),
                    couldImprove = rs.getString("could_improve"),
                    tomorrowPriority = rs.getString("tomorrow_priority"),
                    createdAt = rs.getString("created_at"),
                    updatedAt = rs.getString("updated_at")
                )
            )
        }
        rs.close()
        stmt.close()
        return reviews
    }

    fun getWeeklySummary(weekStartDate: String): WeeklySummary? {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "SELECT id, week_start_date, week_end_date, summary_text, auto_summary_json, created_at, updated_at FROM weekly_summaries WHERE week_start_date = ?"
        )
        stmt.setString(1, weekStartDate)
        val rs = stmt.executeQuery()

        if (!rs.next()) {
            rs.close()
            stmt.close()
            return null
        }

        val summary = WeeklySummary(
            id = rs.getInt("id"),
            weekStartDate = rs.getString("week_start_date"),
            weekEndDate = rs.getString("week_end_date"),
            summaryText = rs.getString("summary_text"),
            autoSummary = null,
            createdAt = rs.getString("created_at"),
            updatedAt = rs.getString("updated_at")
        )
        rs.close()
        stmt.close()
        return summary
    }

    fun insertOrUpdateWeeklySummary(
        weekStartDate: String,
        weekEndDate: String,
        summaryText: String?,
        autoSummaryJson: String?
    ): WeeklySummary {
        val conn = Database.getConnection()

        val existing = getWeeklySummary(weekStartDate)
        if (existing != null) {
            val updateStmt = conn.prepareStatement(
                """
                UPDATE weekly_summaries
                SET week_end_date = ?, summary_text = ?, auto_summary_json = ?, updated_at = datetime('now')
                WHERE week_start_date = ?
                """
            )
            updateStmt.setString(1, weekEndDate)
            updateStmt.setString(2, summaryText)
            updateStmt.setString(3, autoSummaryJson)
            updateStmt.setString(4, weekStartDate)
            updateStmt.executeUpdate()
            updateStmt.close()
        } else {
            val insertStmt = conn.prepareStatement(
                """
                INSERT INTO weekly_summaries (week_start_date, week_end_date, summary_text, auto_summary_json)
                VALUES (?, ?, ?, ?)
                """
            )
            insertStmt.setString(1, weekStartDate)
            insertStmt.setString(2, weekEndDate)
            insertStmt.setString(3, summaryText)
            insertStmt.setString(4, autoSummaryJson)
            insertStmt.executeUpdate()
            insertStmt.close()
        }

        return getWeeklySummary(weekStartDate)!!
    }

    fun getWeeklySummariesForDateRange(startDate: String, endDate: String): List<WeeklySummary> {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "SELECT id, week_start_date, week_end_date, summary_text, auto_summary_json, created_at, updated_at FROM weekly_summaries WHERE week_start_date BETWEEN ? AND ? ORDER BY week_start_date DESC"
        )
        stmt.setString(1, startDate)
        stmt.setString(2, endDate)
        val rs = stmt.executeQuery()

        val summaries = mutableListOf<WeeklySummary>()
        while (rs.next()) {
            summaries.add(
                WeeklySummary(
                    id = rs.getInt("id"),
                    weekStartDate = rs.getString("week_start_date"),
                    weekEndDate = rs.getString("week_end_date"),
                    summaryText = rs.getString("summary_text"),
                    autoSummary = null,
                    createdAt = rs.getString("created_at"),
                    updatedAt = rs.getString("updated_at")
                )
            )
        }
        rs.close()
        stmt.close()
        return summaries
    }

    fun getReviewCountForDateRange(startDate: String, endDate: String): Int {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "SELECT COUNT(*) FROM daily_reviews WHERE date BETWEEN ? AND ?"
        )
        stmt.setString(1, startDate)
        stmt.setString(2, endDate)
        val rs = stmt.executeQuery()
        rs.next()
        val count = rs.getInt(1)
        rs.close()
        stmt.close()
        return count
    }

    fun getRawAutoSummaryJson(weekStartDate: String): String? {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "SELECT auto_summary_json FROM weekly_summaries WHERE week_start_date = ?"
        )
        stmt.setString(1, weekStartDate)
        val rs = stmt.executeQuery()

        val json = if (rs.next()) rs.getString("auto_summary_json") else null
        rs.close()
        stmt.close()
        return json
    }
}

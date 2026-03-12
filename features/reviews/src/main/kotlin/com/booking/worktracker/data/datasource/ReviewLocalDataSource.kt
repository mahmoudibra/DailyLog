package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.ReviewsQueries
import com.booking.worktracker.data.models.DailyReview
import com.booking.worktracker.data.models.WeeklySummary

class ReviewLocalDataSource(
    private val queries: ReviewsQueries
) {

    fun getReviewForDate(date: String): DailyReview? {
        return queries.getReviewForDate(date).executeAsOneOrNull()?.toDailyReview()
    }

    fun insertOrUpdateReview(
        date: String,
        wentWell: String?,
        couldImprove: String?,
        tomorrowPriority: String?
    ): DailyReview {
        val existing = getReviewForDate(date)
        if (existing != null) {
            queries.updateReview(wentWell, couldImprove, tomorrowPriority, date)
        } else {
            queries.insertReview(date, wentWell, couldImprove, tomorrowPriority)
        }
        return getReviewForDate(date)!!
    }

    fun deleteReview(date: String) {
        queries.deleteReview(date)
    }

    fun getReviewsForDateRange(startDate: String, endDate: String): List<DailyReview> {
        return queries.getReviewsForDateRange(startDate, endDate).executeAsList().map { it.toDailyReview() }
    }

    fun getAllReviews(): List<DailyReview> {
        return queries.getAllReviews().executeAsList().map { it.toDailyReview() }
    }

    fun getWeeklySummary(weekStartDate: String): WeeklySummary? {
        return queries.getWeeklySummary(weekStartDate).executeAsOneOrNull()?.toWeeklySummary()
    }

    fun insertOrUpdateWeeklySummary(
        weekStartDate: String,
        weekEndDate: String,
        summaryText: String?,
        autoSummaryJson: String?
    ): WeeklySummary {
        val existing = getWeeklySummary(weekStartDate)
        if (existing != null) {
            queries.updateWeeklySummary(weekEndDate, summaryText, autoSummaryJson, weekStartDate)
        } else {
            queries.insertWeeklySummary(weekStartDate, weekEndDate, summaryText, autoSummaryJson)
        }
        return getWeeklySummary(weekStartDate)!!
    }

    fun getWeeklySummariesForDateRange(startDate: String, endDate: String): List<WeeklySummary> {
        return queries.getWeeklySummariesForDateRange(startDate, endDate).executeAsList()
            .map { it.toWeeklySummary() }
    }

    fun getReviewCountForDateRange(startDate: String, endDate: String): Int {
        return queries.getReviewCountForDateRange(startDate, endDate).executeAsOne().toInt()
    }

    fun getRawAutoSummaryJson(weekStartDate: String): String? {
        return queries.getRawAutoSummaryJson(weekStartDate).executeAsOneOrNull()?.auto_summary_json
    }

    private fun com.booking.worktracker.data.Daily_reviews.toDailyReview() = DailyReview(
        id = id.toInt(),
        date = date,
        wentWell = went_well,
        couldImprove = could_improve,
        tomorrowPriority = tomorrow_priority,
        createdAt = created_at,
        updatedAt = updated_at
    )

    private fun com.booking.worktracker.data.Weekly_summaries.toWeeklySummary() = WeeklySummary(
        id = id.toInt(),
        weekStartDate = week_start_date,
        weekEndDate = week_end_date,
        summaryText = summary_text,
        autoSummary = null,
        createdAt = created_at,
        updatedAt = updated_at
    )
}

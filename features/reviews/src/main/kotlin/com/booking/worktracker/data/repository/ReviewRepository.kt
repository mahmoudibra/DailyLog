package com.booking.worktracker.data.repository

import com.booking.worktracker.data.datasource.ReviewLocalDataSource
import com.booking.worktracker.data.models.DailyReview
import com.booking.worktracker.data.models.WeeklySummary

class ReviewRepository(private val localDataSource: ReviewLocalDataSource = ReviewLocalDataSource()) {

    fun getReviewForDate(date: String): DailyReview? {
        return localDataSource.getReviewForDate(date)
    }

    fun insertOrUpdateReview(
        date: String,
        wentWell: String?,
        couldImprove: String?,
        tomorrowPriority: String?
    ): DailyReview {
        return localDataSource.insertOrUpdateReview(date, wentWell, couldImprove, tomorrowPriority)
    }

    fun deleteReview(date: String) {
        localDataSource.deleteReview(date)
    }

    fun getReviewsForDateRange(startDate: String, endDate: String): List<DailyReview> {
        return localDataSource.getReviewsForDateRange(startDate, endDate)
    }

    fun getAllReviews(): List<DailyReview> {
        return localDataSource.getAllReviews()
    }

    fun getWeeklySummary(weekStartDate: String): WeeklySummary? {
        return localDataSource.getWeeklySummary(weekStartDate)
    }

    fun insertOrUpdateWeeklySummary(
        weekStartDate: String,
        weekEndDate: String,
        summaryText: String?,
        autoSummaryJson: String?
    ): WeeklySummary {
        return localDataSource.insertOrUpdateWeeklySummary(weekStartDate, weekEndDate, summaryText, autoSummaryJson)
    }

    fun getWeeklySummariesForDateRange(startDate: String, endDate: String): List<WeeklySummary> {
        return localDataSource.getWeeklySummariesForDateRange(startDate, endDate)
    }

    fun getReviewCountForDateRange(startDate: String, endDate: String): Int {
        return localDataSource.getReviewCountForDateRange(startDate, endDate)
    }

    fun getRawAutoSummaryJson(weekStartDate: String): String? {
        return localDataSource.getRawAutoSummaryJson(weekStartDate)
    }
}

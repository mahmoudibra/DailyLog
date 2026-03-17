package com.booking.worktracker.data.models

data class AutoSummary(
    val entryCount: Int,
    val timeTrackedMinutes: Int,
    val objectivesProgressed: Int,
    val streakDays: Int,
    val topTags: List<String>,
    val dailyReviewCount: Int
)

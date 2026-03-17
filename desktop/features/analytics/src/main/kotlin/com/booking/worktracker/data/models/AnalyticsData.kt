package com.booking.worktracker.data.models

data class DailyStats(
    val date: String,
    val entryCount: Int,
    val tagCount: Int
)

data class WeeklyStats(
    val weekStart: String,
    val weekEnd: String,
    val totalEntries: Int,
    val activeDays: Int
)

data class TagStats(
    val tagName: String,
    val tagColor: String?,
    val usageCount: Int
)

data class ObjectiveStats(
    val totalObjectives: Int,
    val completedObjectives: Int,
    val inProgressObjectives: Int,
    val cancelledObjectives: Int,
    val averageChecklistCompletion: Int
)

data class StreakInfo(
    val currentStreak: Int,
    val longestStreak: Int,
    val totalDaysLogged: Int
)

data class AnalyticsSummary(
    val streakInfo: StreakInfo,
    val totalEntries: Int,
    val averageEntriesPerDay: Double,
    val mostActiveDay: String?,
    val recentDailyStats: List<DailyStats>,
    val weeklyStats: List<WeeklyStats>,
    val tagStats: List<TagStats>,
    val objectiveStats: ObjectiveStats
)

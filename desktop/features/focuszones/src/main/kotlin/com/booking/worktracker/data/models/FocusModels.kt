package com.booking.worktracker.data.models

data class HourlyFocusData(
    val dayOfWeek: Int,  // 1=Monday, 7=Sunday
    val hour: Int,       // 0-23
    val averageRating: Double,
    val entryCount: Int
)

data class FocusPattern(
    val peakHourStart: Int,
    val peakHourEnd: Int,
    val averagePeakRating: Double,
    val description: String  // e.g., "Deep work best 9-11am"
)

data class CategoryRecommendation(
    val category: String,
    val bestHourStart: Int,
    val bestHourEnd: Int,
    val averageRating: Double,
    val description: String  // e.g., "Best time for Coding: 9am-11am"
)

data class FocusSummary(
    val totalRatedEntries: Int,
    val averageFocusRating: Double,
    val patterns: List<FocusPattern>,
    val recommendations: List<CategoryRecommendation>,
    val heatmapData: List<HourlyFocusData>
)

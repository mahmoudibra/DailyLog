package com.booking.worktracker.data.models

data class DailyReview(
    val id: Int,
    val date: String,
    val wentWell: String?,
    val couldImprove: String?,
    val tomorrowPriority: String?,
    val createdAt: String,
    val updatedAt: String
)

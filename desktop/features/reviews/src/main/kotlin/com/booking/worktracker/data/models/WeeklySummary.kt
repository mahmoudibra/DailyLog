package com.booking.worktracker.data.models

data class WeeklySummary(
    val id: Int,
    val weekStartDate: String,
    val weekEndDate: String,
    val summaryText: String?,
    val autoSummary: AutoSummary?,
    val createdAt: String,
    val updatedAt: String
)

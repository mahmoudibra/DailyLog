package com.booking.worktracker.data.models

data class DailyLog(
    val id: Int,
    val date: String,
    val entries: List<WorkEntry>,
    val tags: List<Tag>,
    val createdAt: String,
    val updatedAt: String
)

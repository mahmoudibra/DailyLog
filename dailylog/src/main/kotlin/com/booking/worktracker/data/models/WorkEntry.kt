package com.booking.worktracker.data.models

data class WorkEntry(
    val id: Int,
    val dailyLogId: Int,
    val content: String,
    val createdAt: String
)

package com.booking.worktracker.data.models

data class ChecklistItem(
    val id: Int,
    val objectiveId: Int,
    val text: String,
    val completed: Boolean,
    val position: Int,
    val createdAt: String
)

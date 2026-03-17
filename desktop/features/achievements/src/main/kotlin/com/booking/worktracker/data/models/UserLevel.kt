package com.booking.worktracker.data.models

data class UserLevel(
    val totalXp: Long,
    val currentLevel: Int,
    val rankTitle: String,
    val xpForNextLevel: Long,
    val progressPercent: Float
)

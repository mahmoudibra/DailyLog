package com.booking.worktracker.data.models

data class Achievement(
    val id: Int,
    val definition: AchievementDefinition,
    val xpNeeded: Int,
    val isUnlocked: Boolean,
    val unlockedAt: String?
)

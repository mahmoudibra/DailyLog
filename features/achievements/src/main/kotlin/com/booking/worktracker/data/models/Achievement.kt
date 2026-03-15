package com.booking.worktracker.data.models

data class Achievement(
    val id: Int,
    val name: String,
    val description: String,
    val category: AchievementCategory,
    val icon: String,
    val xpReward: Int,
    val requirementType: String,
    val requirementValue: Int,
    val isUnlocked: Boolean,
    val unlockedAt: String?
)

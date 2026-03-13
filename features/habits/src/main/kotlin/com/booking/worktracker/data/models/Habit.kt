package com.booking.worktracker.data.models

data class Habit(
    val id: Int,
    val name: String,
    val icon: String?,
    val color: String?,
    val objectiveId: Int?,
    val isArchived: Boolean,
    val createdAt: String,
    val updatedAt: String
)

data class HabitCompletion(
    val id: Int,
    val habitId: Int,
    val date: String,
    val createdAt: String
)

data class HabitStreak(
    val habit: Habit,
    val currentStreak: Int,
    val longestStreak: Int,
    val totalCompletions: Int,
    val completedToday: Boolean
)

data class HabitWeeklyScore(
    val totalHabits: Int,
    val totalPossible: Int,
    val completionRate: Double,
    val perfectDays: Int
)

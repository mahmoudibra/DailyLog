package com.booking.worktracker.data.models

data class TimeBudget(
    val id: Int,
    val category: String,
    val targetMinutes: Int,
    val periodType: PeriodType,
    val objectiveId: Int?,
    val createdAt: String,
    val updatedAt: String
) {
    fun formattedTarget(): String {
        val hours = targetMinutes / 60
        val minutes = targetMinutes % 60
        return if (minutes > 0) "${hours}h ${minutes}m" else "${hours}h"
    }
}

enum class PeriodType {
    WEEKLY, MONTHLY
}

data class BudgetProgress(
    val budget: TimeBudget,
    val actualMinutes: Int,
    val objectiveTitle: String?
) {
    val percentage: Float get() = if (budget.targetMinutes > 0) {
        (actualMinutes.toFloat() / budget.targetMinutes.toFloat()).coerceAtMost(1.5f)
    } else 0f

    val status: BudgetStatus get() = when {
        percentage >= 0.8f -> BudgetStatus.ON_TRACK
        percentage >= 0.5f -> BudgetStatus.AT_RISK
        else -> BudgetStatus.BEHIND
    }

    fun formattedActual(): String {
        val hours = actualMinutes / 60
        val minutes = actualMinutes % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }
}

enum class BudgetStatus {
    ON_TRACK, AT_RISK, BEHIND
}

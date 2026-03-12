package com.booking.worktracker.domain.usecases.timebudgets

import com.booking.worktracker.data.models.BudgetProgress
import com.booking.worktracker.data.models.PeriodType
import com.booking.worktracker.data.models.TimeBudget
import com.booking.worktracker.data.repository.TimeBudgetRepository
import kotlinx.datetime.*

class CreateTimeBudgetUseCase(private val repository: TimeBudgetRepository) {
    operator fun invoke(category: String, targetMinutes: Int, periodType: PeriodType, objectiveId: Int?): Result<TimeBudget> {
        return try {
            require(category.isNotBlank()) { "Category cannot be blank" }
            require(targetMinutes > 0) { "Target must be positive" }
            val budget = repository.create(category, targetMinutes, periodType, objectiveId)
            Result.success(budget)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class UpdateTimeBudgetUseCase(private val repository: TimeBudgetRepository) {
    operator fun invoke(id: Int, category: String, targetMinutes: Int, periodType: PeriodType, objectiveId: Int?): Result<TimeBudget> {
        return try {
            require(category.isNotBlank()) { "Category cannot be blank" }
            require(targetMinutes > 0) { "Target must be positive" }
            val budget = repository.update(id, category, targetMinutes, periodType, objectiveId)
            Result.success(budget)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class DeleteTimeBudgetUseCase(private val repository: TimeBudgetRepository) {
    operator fun invoke(id: Int): Result<Unit> {
        return try {
            repository.delete(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class TimeBudgetDashboardData(
    val budgetProgress: List<BudgetProgress>,
    val categories: List<String>,
    val activeObjectives: List<Pair<Int, String>>,
    val periodElapsedFraction: Float
)

class GetTimeBudgetDashboardUseCase(private val repository: TimeBudgetRepository) {
    operator fun invoke(): Result<TimeBudgetDashboardData> {
        return try {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val budgets = repository.getAll()

            val progressList = budgets.map { budget ->
                val (startDate, endDate, elapsedFraction) = calculatePeriodRange(budget.periodType, today)
                val actualMinutes = repository.getActualMinutesForCategory(budget.category, startDate, endDate)
                val objectiveTitle = budget.objectiveId?.let { repository.getObjectiveTitle(it) }
                BudgetProgress(budget, actualMinutes, objectiveTitle)
            }

            val (_, _, periodElapsed) = calculatePeriodRange(
                if (budgets.any { it.periodType == PeriodType.WEEKLY }) PeriodType.WEEKLY else PeriodType.MONTHLY,
                today
            )

            val activeObjectives = repository.getActiveObjectives()

            // Gather known categories from time_entries via budgets + defaults
            val budgetCategories = budgets.map { it.category }
            val defaults = listOf("General", "Meeting", "Coding", "Review", "Planning")
            val categories = (defaults + budgetCategories).distinct().sorted()

            Result.success(TimeBudgetDashboardData(progressList, categories, activeObjectives, periodElapsed))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculatePeriodRange(periodType: PeriodType, today: LocalDate): Triple<LocalDate, LocalDate, Float> {
        return when (periodType) {
            PeriodType.WEEKLY -> {
                val dayOfWeek = today.dayOfWeek.ordinal // Monday=0
                val weekStart = today.minus(DatePeriod(days = dayOfWeek))
                val weekEnd = weekStart.plus(DatePeriod(days = 6))
                val elapsed = (dayOfWeek + 1).toFloat() / 7f
                Triple(weekStart, weekEnd, elapsed)
            }
            PeriodType.MONTHLY -> {
                val monthStart = LocalDate(today.year, today.month, 1)
                val nextMonth = if (today.monthNumber == 12) {
                    LocalDate(today.year + 1, 1, 1)
                } else {
                    LocalDate(today.year, today.monthNumber + 1, 1)
                }
                val monthEnd = nextMonth.minus(DatePeriod(days = 1))
                val daysInMonth = monthEnd.dayOfMonth
                val elapsed = today.dayOfMonth.toFloat() / daysInMonth.toFloat()
                Triple(monthStart, monthEnd, elapsed)
            }
        }
    }
}

package com.booking.worktracker.data.repository

import com.booking.worktracker.data.datasource.HabitLocalDataSource
import com.booking.worktracker.data.models.*
import com.booking.worktracker.di.Singleton
import kotlinx.datetime.*
import me.tatarka.inject.annotations.Inject

@Inject
@Singleton
class HabitRepository(private val localDataSource: HabitLocalDataSource) {

    fun getAllActiveHabits(): List<Habit> = localDataSource.getAllActiveHabits()

    fun getHabitById(id: Int): Habit? = localDataSource.getHabitById(id)

    fun createHabit(name: String, icon: String?, color: String?, objectiveId: Int?): Habit =
        localDataSource.createHabit(name, icon, color, objectiveId)

    fun updateHabit(id: Int, name: String, icon: String?, color: String?, objectiveId: Int?) =
        localDataSource.updateHabit(id, name, icon, color, objectiveId)

    fun archiveHabit(id: Int) = localDataSource.archiveHabit(id)

    fun toggleCompletion(habitId: Int, date: LocalDate): Boolean =
        localDataSource.toggleCompletion(habitId, date.toString())

    fun getCompletedHabitIdsForDate(date: LocalDate): Set<Int> {
        return localDataSource.getCompletionsForDate(date.toString())
            .map { it.habitId }
            .toSet()
    }

    fun getHabitStreak(habit: Habit): HabitStreak {
        val dates = localDataSource.getCompletionDatesForHabit(habit.id)
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        val currentStreak = calculateCurrentStreak(dates, today)
        val longestStreak = calculateLongestStreak(dates)
        val completedToday = dates.contains(today.toString())

        return HabitStreak(
            habit = habit,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalCompletions = dates.size,
            completedToday = completedToday
        )
    }

    fun getWeeklyScore(startDate: LocalDate, endDate: LocalDate): HabitWeeklyScore {
        val habits = getAllActiveHabits()
        if (habits.isEmpty()) return HabitWeeklyScore(0, 0, 0.0, 0)

        val completions = localDataSource.getAllCompletionsInRange(startDate.toString(), endDate.toString())
        val completionsByDate = completions.groupBy { it.date }

        var totalDays = 0
        var date = startDate
        var perfectDays = 0
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        while (date <= endDate && date <= today) {
            totalDays++
            val dayCompletions = completionsByDate[date.toString()] ?: emptyList()
            if (dayCompletions.map { it.habitId }.toSet().size >= habits.size) {
                perfectDays++
            }
            date = date.plus(1, DateTimeUnit.DAY)
        }

        val totalPossible = totalDays * habits.size
        val totalCompleted = completions.size
        val rate = if (totalPossible > 0) totalCompleted.toDouble() / totalPossible else 0.0

        return HabitWeeklyScore(
            totalHabits = totalCompleted,
            totalPossible = totalPossible,
            completionRate = rate,
            perfectDays = perfectDays
        )
    }

    fun getHabitsByObjectiveId(objectiveId: Int): List<Habit> =
        localDataSource.getHabitsByObjectiveId(objectiveId)

    private fun calculateCurrentStreak(dates: List<String>, today: LocalDate): Int {
        if (dates.isEmpty()) return 0

        // dates are sorted DESC
        val mostRecent = dates.first()
        val yesterday = today.minus(1, DateTimeUnit.DAY).toString()

        // Current streak only counts if most recent is today or yesterday
        if (mostRecent != today.toString() && mostRecent != yesterday) return 0

        var streak = 1
        for (i in 1 until dates.size) {
            val current = LocalDate.parse(dates[i - 1])
            val previous = LocalDate.parse(dates[i])
            val diff = current.toEpochDays() - previous.toEpochDays()
            if (diff == 1) {
                streak++
            } else {
                break
            }
        }
        return streak
    }

    private fun calculateLongestStreak(dates: List<String>): Int {
        if (dates.isEmpty()) return 0

        var longest = 1
        var current = 1

        for (i in 1 until dates.size) {
            val d1 = LocalDate.parse(dates[i - 1])
            val d2 = LocalDate.parse(dates[i])
            val diff = d1.toEpochDays() - d2.toEpochDays()
            if (diff == 1) {
                current++
                if (current > longest) longest = current
            } else {
                current = 1
            }
        }
        return longest
    }
}

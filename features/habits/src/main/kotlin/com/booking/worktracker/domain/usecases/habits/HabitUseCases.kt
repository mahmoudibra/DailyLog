package com.booking.worktracker.domain.usecases.habits

import com.booking.worktracker.data.models.*
import com.booking.worktracker.data.repository.HabitRepository
import kotlinx.datetime.LocalDate
import me.tatarka.inject.annotations.Inject

@Inject
class GetAllHabitsUseCase(private val repository: HabitRepository) {
    operator fun invoke(): Result<List<Habit>> = try {
        Result.success(repository.getAllActiveHabits())
    } catch (e: Exception) {
        Result.failure(e)
    }
}

@Inject
class CreateHabitUseCase(private val repository: HabitRepository) {
    operator fun invoke(name: String, icon: String?, color: String?, objectiveId: Int?): Result<Habit> = try {
        require(name.isNotBlank()) { "Habit name cannot be blank" }
        Result.success(repository.createHabit(name, icon, color, objectiveId))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

@Inject
class UpdateHabitUseCase(private val repository: HabitRepository) {
    operator fun invoke(id: Int, name: String, icon: String?, color: String?, objectiveId: Int?): Result<Unit> = try {
        require(name.isNotBlank()) { "Habit name cannot be blank" }
        Result.success(repository.updateHabit(id, name, icon, color, objectiveId))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

@Inject
class ArchiveHabitUseCase(private val repository: HabitRepository) {
    operator fun invoke(id: Int): Result<Unit> = try {
        Result.success(repository.archiveHabit(id))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

@Inject
class ToggleHabitCompletionUseCase(private val repository: HabitRepository) {
    operator fun invoke(habitId: Int, date: LocalDate): Result<Boolean> = try {
        Result.success(repository.toggleCompletion(habitId, date))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

@Inject
class GetHabitStreaksUseCase(private val repository: HabitRepository) {
    operator fun invoke(): Result<List<HabitStreak>> = try {
        val habits = repository.getAllActiveHabits()
        val streaks = habits.map { repository.getHabitStreak(it) }
        Result.success(streaks)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

@Inject
class GetCompletedHabitIdsUseCase(private val repository: HabitRepository) {
    operator fun invoke(date: LocalDate): Result<Set<Int>> = try {
        Result.success(repository.getCompletedHabitIdsForDate(date))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

@Inject
class GetWeeklyScoreUseCase(private val repository: HabitRepository) {
    operator fun invoke(startDate: LocalDate, endDate: LocalDate): Result<HabitWeeklyScore> = try {
        Result.success(repository.getWeeklyScore(startDate, endDate))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

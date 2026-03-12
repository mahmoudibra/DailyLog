package com.booking.worktracker.domain.usecases.timetracking

import com.booking.worktracker.data.models.TimeEntry
import com.booking.worktracker.data.repository.TimeEntryRepository
import kotlinx.datetime.LocalDate

class StartTimerUseCase(private val repository: TimeEntryRepository) {
    operator fun invoke(description: String, category: String, date: LocalDate, startTime: String): Result<TimeEntry> {
        return try {
            require(description.isNotBlank()) { "Description cannot be blank" }
            // Stop any running timer first
            val running = repository.getRunningEntry()
            if (running != null) {
                return Result.failure(IllegalStateException("A timer is already running. Stop it first."))
            }
            val entry = repository.startTimer(description, category, date, startTime)
            Result.success(entry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class StopTimerUseCase(private val repository: TimeEntryRepository) {
    operator fun invoke(id: Int, endTime: String, durationMinutes: Int): Result<TimeEntry> {
        return try {
            require(durationMinutes >= 0) { "Duration cannot be negative" }
            val entry = repository.stopTimer(id, endTime, durationMinutes)
            Result.success(entry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class AddManualEntryUseCase(private val repository: TimeEntryRepository) {
    operator fun invoke(
        description: String,
        category: String,
        date: LocalDate,
        startTime: String,
        endTime: String,
        durationMinutes: Int
    ): Result<TimeEntry> {
        return try {
            require(description.isNotBlank()) { "Description cannot be blank" }
            require(durationMinutes > 0) { "Duration must be positive" }
            val entry = repository.addManualEntry(description, category, date, startTime, endTime, durationMinutes)
            Result.success(entry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class DeleteTimeEntryUseCase(private val repository: TimeEntryRepository) {
    operator fun invoke(id: Int): Result<Unit> {
        return try {
            repository.delete(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package com.booking.worktracker.domain.usecases.logs

import com.booking.worktracker.data.repository.LogRepository

class DeleteWorkEntryUseCase(private val logRepository: LogRepository) {
    operator fun invoke(entryId: Int): Result<Unit> {
        return try {
            logRepository.deleteWorkEntry(entryId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package com.booking.worktracker.domain.usecases.logs

import com.booking.worktracker.data.models.WorkEntry
import com.booking.worktracker.data.repository.LogRepository
import kotlinx.datetime.LocalDate

class AddWorkEntryUseCase(private val logRepository: LogRepository) {
    operator fun invoke(date: LocalDate, content: String): Result<WorkEntry> {
        return try {
            require(content.isNotBlank()) { "Work entry content cannot be blank" }
            val entry = logRepository.addWorkEntry(date, content)
            Result.success(entry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package com.booking.worktracker.domain.usecases.logs

import com.booking.worktracker.data.models.Tag
import com.booking.worktracker.data.repository.LogRepository
import kotlinx.datetime.LocalDate

class UpdateLogTagsUseCase(private val logRepository: LogRepository = LogRepository()) {
    operator fun invoke(date: LocalDate, tags: List<Tag>) {
        logRepository.updateLogTags(date, tags)
    }
}

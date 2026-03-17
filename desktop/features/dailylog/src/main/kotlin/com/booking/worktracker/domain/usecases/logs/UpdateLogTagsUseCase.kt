package com.booking.worktracker.domain.usecases.logs

import com.booking.worktracker.data.models.Tag
import com.booking.worktracker.data.repository.LogRepository
import kotlinx.datetime.LocalDate
import me.tatarka.inject.annotations.Inject

@Inject
class UpdateLogTagsUseCase(private val logRepository: LogRepository) {
    operator fun invoke(date: LocalDate, tags: List<Tag>) {
        logRepository.updateLogTags(date, tags)
    }
}

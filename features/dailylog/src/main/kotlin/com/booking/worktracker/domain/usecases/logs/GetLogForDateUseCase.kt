package com.booking.worktracker.domain.usecases.logs

import com.booking.worktracker.data.models.DailyLog
import com.booking.worktracker.data.repository.LogRepository
import kotlinx.datetime.LocalDate
import me.tatarka.inject.annotations.Inject

@Inject
class GetLogForDateUseCase(private val logRepository: LogRepository) {
    operator fun invoke(date: LocalDate): DailyLog? = logRepository.getLogForDate(date)
}

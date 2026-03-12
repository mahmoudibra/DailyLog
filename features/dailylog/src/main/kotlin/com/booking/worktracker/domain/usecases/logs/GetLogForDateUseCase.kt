package com.booking.worktracker.domain.usecases.logs

import com.booking.worktracker.data.models.DailyLog
import com.booking.worktracker.data.repository.LogRepository
import kotlinx.datetime.LocalDate

class GetLogForDateUseCase(private val logRepository: LogRepository = LogRepository()) {
    operator fun invoke(date: LocalDate): DailyLog? = logRepository.getLogForDate(date)
}

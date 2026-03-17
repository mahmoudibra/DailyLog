package com.booking.worktracker.data.repository

import com.booking.worktracker.data.datasource.LogLocalDataSource
import com.booking.worktracker.data.models.DailyLog
import com.booking.worktracker.data.models.Tag
import com.booking.worktracker.data.models.WorkEntry
import kotlinx.datetime.LocalDate
import me.tatarka.inject.annotations.Inject
import com.booking.worktracker.di.Singleton

@Inject
@Singleton
class LogRepository(private val localDataSource: LogLocalDataSource) {

    fun getLogForDate(date: LocalDate): DailyLog? {
        return localDataSource.getLogForDate(date)
    }

    fun addWorkEntry(date: LocalDate, content: String): WorkEntry {
        return localDataSource.addWorkEntry(date, content)
    }

    fun deleteWorkEntry(entryId: Int) {
        localDataSource.deleteWorkEntry(entryId)
    }

    fun updateLogTags(date: LocalDate, tags: List<Tag>) {
        localDataSource.updateLogTags(date, tags)
    }

    fun getAllLogs(limit: Int = 50): List<DailyLog> {
        return localDataSource.getAllLogs(limit)
    }

    fun getLogForDateRange(start: LocalDate, end: LocalDate): List<DailyLog>? {
        return localDataSource.getLogForDateRange(start, end)
    }

    fun hasLogForDate(date: LocalDate): Boolean {
        return localDataSource.hasLogForDate(date)
    }
}

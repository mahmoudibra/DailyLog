package com.booking.worktracker.data.repository

import com.booking.worktracker.data.datasource.TimeEntryLocalDataSource
import com.booking.worktracker.data.models.TimeEntry
import kotlinx.datetime.LocalDate

class TimeEntryRepository(
    private val localDataSource: TimeEntryLocalDataSource
) {
    fun getEntriesForDate(date: LocalDate): List<TimeEntry> = localDataSource.getEntriesForDate(date)
    fun getRunningEntry(): TimeEntry? = localDataSource.getRunningEntry()
    fun startTimer(description: String, category: String, date: LocalDate, startTime: String): TimeEntry =
        localDataSource.startTimer(description, category, date, startTime)
    fun stopTimer(id: Int, endTime: String, durationMinutes: Int): TimeEntry =
        localDataSource.stopTimer(id, endTime, durationMinutes)
    fun addManualEntry(description: String, category: String, date: LocalDate, startTime: String, endTime: String, durationMinutes: Int): TimeEntry =
        localDataSource.addManualEntry(description, category, date, startTime, endTime, durationMinutes)
    fun delete(id: Int) = localDataSource.delete(id)
    fun getCategories(): List<String> = localDataSource.getCategories()
    fun getTotalMinutesForDate(date: LocalDate): Int = localDataSource.getTotalMinutesForDate(date)
    fun getMinutesByCategoryForDate(date: LocalDate): Map<String, Int> = localDataSource.getMinutesByCategoryForDate(date)
}

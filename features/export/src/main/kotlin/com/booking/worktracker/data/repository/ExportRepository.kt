package com.booking.worktracker.data.repository

import com.booking.worktracker.data.datasource.ExportLocalDataSource
import com.booking.worktracker.data.models.ExportEntry
import com.booking.worktracker.data.models.ExportObjective
import com.booking.worktracker.data.models.ExportResult

class ExportRepository(private val localDataSource: ExportLocalDataSource = ExportLocalDataSource()) {
    fun getEntriesForRange(startDate: String, endDate: String): List<ExportEntry> =
        localDataSource.getEntriesForRange(startDate, endDate)

    fun getObjectives(): List<ExportObjective> = localDataSource.getObjectives()

    fun getExportData(startDate: String, endDate: String, includeObjectives: Boolean): ExportResult {
        val entries = getEntriesForRange(startDate, endDate)
        val objectives = if (includeObjectives) getObjectives() else emptyList()
        val totalDays = localDataSource.getDaysInRange(startDate, endDate)

        return ExportResult(
            entries = entries,
            objectives = objectives,
            dateRange = "$startDate to $endDate",
            totalEntries = entries.size,
            totalDays = totalDays
        )
    }
}

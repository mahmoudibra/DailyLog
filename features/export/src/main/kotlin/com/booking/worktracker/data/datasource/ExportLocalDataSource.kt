package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.ExportQueries
import com.booking.worktracker.data.models.ExportEntry
import com.booking.worktracker.data.models.ExportObjective

class ExportLocalDataSource(
    private val queries: ExportQueries
) {

    fun getEntriesForRange(startDate: String, endDate: String): List<ExportEntry> {
        return queries.getEntriesForRange(startDate, endDate).executeAsList().map { row ->
            ExportEntry(
                date = row.date,
                content = row.content,
                tags = row.tag_names?.split(",")?.distinct() ?: emptyList(),
                createdAt = row.created_at
            )
        }
    }

    fun getObjectives(): List<ExportObjective> {
        return queries.getObjectives().executeAsList().map { row ->
            ExportObjective(
                title = row.title,
                description = row.description,
                type = row.type,
                status = row.status,
                checklistItems = row.items?.split("||") ?: emptyList(),
                completedItems = (row.completed_count ?: 0L).toInt(),
                totalItems = row.total_count.toInt()
            )
        }
    }

    fun getDaysInRange(startDate: String, endDate: String): Int {
        return queries.getDaysInRange(startDate, endDate).executeAsOne().toInt()
    }
}

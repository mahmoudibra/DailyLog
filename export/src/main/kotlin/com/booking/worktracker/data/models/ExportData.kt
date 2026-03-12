package com.booking.worktracker.data.models

enum class ExportFormat {
    PLAIN_TEXT, CSV, MARKDOWN
}

data class ExportOptions(
    val startDate: String,
    val endDate: String,
    val includeEntries: Boolean = true,
    val includeTags: Boolean = true,
    val includeObjectives: Boolean = false,
    val format: ExportFormat = ExportFormat.MARKDOWN
)

data class ExportEntry(
    val date: String,
    val content: String,
    val tags: List<String>,
    val createdAt: String
)

data class ExportObjective(
    val title: String,
    val description: String,
    val type: String,
    val status: String,
    val checklistItems: List<String>,
    val completedItems: Int,
    val totalItems: Int
)

data class ExportResult(
    val entries: List<ExportEntry>,
    val objectives: List<ExportObjective>,
    val dateRange: String,
    val totalEntries: Int,
    val totalDays: Int
)

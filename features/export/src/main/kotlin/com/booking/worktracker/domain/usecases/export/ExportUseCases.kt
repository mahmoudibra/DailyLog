package com.booking.worktracker.domain.usecases.export

import com.booking.worktracker.data.models.ExportFormat
import com.booking.worktracker.data.models.ExportOptions
import com.booking.worktracker.data.models.ExportResult
import com.booking.worktracker.data.repository.ExportRepository

class GenerateExportUseCase(private val repository: ExportRepository = ExportRepository()) {
    operator fun invoke(options: ExportOptions): Result<String> {
        return try {
            val data = repository.getExportData(options.startDate, options.endDate, options.includeObjectives)
            val content = when (options.format) {
                ExportFormat.PLAIN_TEXT -> formatPlainText(data, options)
                ExportFormat.CSV -> formatCsv(data, options)
                ExportFormat.MARKDOWN -> formatMarkdown(data, options)
            }
            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun formatPlainText(data: ExportResult, options: ExportOptions): String {
        val sb = StringBuilder()
        sb.appendLine("Daily Work Tracker Export")
        sb.appendLine("Period: ${data.dateRange}")
        sb.appendLine("Total entries: ${data.totalEntries} | Days logged: ${data.totalDays}")
        sb.appendLine("=".repeat(50))
        sb.appendLine()

        if (options.includeEntries) {
            var currentDate = ""
            for (entry in data.entries) {
                if (entry.date != currentDate) {
                    currentDate = entry.date
                    sb.appendLine("--- $currentDate ---")
                    if (options.includeTags && entry.tags.isNotEmpty()) {
                        sb.appendLine("Tags: ${entry.tags.joinToString(", ")}")
                    }
                }
                sb.appendLine("  - ${entry.content}")
            }
            sb.appendLine()
        }

        if (options.includeObjectives && data.objectives.isNotEmpty()) {
            sb.appendLine("=== OBJECTIVES ===")
            sb.appendLine()
            for (obj in data.objectives) {
                sb.appendLine("[${obj.status}] ${obj.title} (${obj.type})")
                if (obj.description.isNotBlank()) {
                    sb.appendLine("  ${obj.description}")
                }
                if (obj.totalItems > 0) {
                    sb.appendLine("  Progress: ${obj.completedItems}/${obj.totalItems}")
                    for (item in obj.checklistItems) {
                        sb.appendLine("    - $item")
                    }
                }
                sb.appendLine()
            }
        }

        return sb.toString()
    }

    private fun formatCsv(data: ExportResult, options: ExportOptions): String {
        val sb = StringBuilder()

        if (options.includeEntries) {
            sb.appendLine("Date,Content,Tags,Created At")
            for (entry in data.entries) {
                val tags = if (options.includeTags) entry.tags.joinToString(";") else ""
                val content = entry.content.replace("\"", "\"\"")
                sb.appendLine("${entry.date},\"$content\",\"$tags\",${entry.createdAt}")
            }
        }

        if (options.includeObjectives && data.objectives.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("Title,Type,Status,Description,Progress")
            for (obj in data.objectives) {
                val desc = obj.description.replace("\"", "\"\"")
                sb.appendLine("\"${obj.title}\",${obj.type},${obj.status},\"$desc\",${obj.completedItems}/${obj.totalItems}")
            }
        }

        return sb.toString()
    }

    private fun formatMarkdown(data: ExportResult, options: ExportOptions): String {
        val sb = StringBuilder()
        sb.appendLine("# Daily Work Tracker Export")
        sb.appendLine()
        sb.appendLine("**Period:** ${data.dateRange}")
        sb.appendLine("**Total entries:** ${data.totalEntries} | **Days logged:** ${data.totalDays}")
        sb.appendLine()

        if (options.includeEntries) {
            sb.appendLine("## Work Entries")
            sb.appendLine()
            var currentDate = ""
            for (entry in data.entries) {
                if (entry.date != currentDate) {
                    currentDate = entry.date
                    sb.appendLine("### $currentDate")
                    if (options.includeTags && entry.tags.isNotEmpty()) {
                        sb.appendLine("*Tags: ${entry.tags.joinToString(", ")}*")
                    }
                    sb.appendLine()
                }
                sb.appendLine("- ${entry.content}")
            }
            sb.appendLine()
        }

        if (options.includeObjectives && data.objectives.isNotEmpty()) {
            sb.appendLine("## Objectives")
            sb.appendLine()
            for (obj in data.objectives) {
                val statusEmoji = when (obj.status) {
                    "COMPLETED" -> "[x]"
                    "IN_PROGRESS" -> "[-]"
                    else -> "[ ]"
                }
                sb.appendLine("### $statusEmoji ${obj.title} (${obj.type})")
                if (obj.description.isNotBlank()) {
                    sb.appendLine("> ${obj.description}")
                }
                if (obj.totalItems > 0) {
                    sb.appendLine()
                    sb.appendLine("**Progress:** ${obj.completedItems}/${obj.totalItems}")
                    for (item in obj.checklistItems) {
                        sb.appendLine("- $item")
                    }
                }
                sb.appendLine()
            }
        }

        return sb.toString()
    }
}

class SaveExportToFileUseCase {
    operator fun invoke(content: String, filePath: String): Result<Unit> {
        return try {
            java.io.File(filePath).writeText(content)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

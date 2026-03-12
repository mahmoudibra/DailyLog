package com.booking.worktracker.data.models

enum class ObjectiveType {
    YEARLY, QUARTERLY
}

enum class ObjectiveStatus {
    IN_PROGRESS, COMPLETED, CANCELLED
}

data class Objective(
    val id: Int,
    val title: String,
    val description: String,
    val type: ObjectiveType,
    val year: Int,
    val quarter: Int?,
    val status: ObjectiveStatus,
    val checklistItems: List<ChecklistItem>,
    val createdAt: String,
    val updatedAt: String
) {
    fun checklistProgress(): String {
        val completed = checklistItems.count { it.completed }
        return "$completed/${checklistItems.size}"
    }

    fun completionPercentage(): Int {
        if (checklistItems.isEmpty()) return 0
        val completed = checklistItems.count { it.completed }
        return (completed * 100) / checklistItems.size
    }
}

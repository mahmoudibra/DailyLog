package com.booking.worktracker.data.models

data class TimeEntry(
    val id: Int,
    val description: String,
    val category: String,
    val startTime: String,
    val endTime: String?,
    val durationMinutes: Int,
    val date: String,
    val createdAt: String
) {
    val isRunning: Boolean get() = endTime == null

    fun formattedDuration(): String {
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }
}

package com.booking.worktracker.notifications

import com.booking.worktracker.data.repository.LogRepository
import com.booking.worktracker.data.repository.SettingsRepository
import com.booking.worktracker.ui.localization.AppLocale
import com.booking.worktracker.core.generated.resources.*
import org.jetbrains.compose.resources.getString
import kotlinx.coroutines.*
import kotlinx.datetime.*

class ReminderScheduler(
    private val logRepository: LogRepository = LogRepository(),
    private val settingsRepository: SettingsRepository = SettingsRepository()
) {
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var morningJob: Job? = null
    private var afternoonJob: Job? = null

    private fun setLocale() {
        val locale = AppLocale.fromCode(settingsRepository.getLanguage())
        java.util.Locale.setDefault(java.util.Locale(locale.code))
    }

    fun start() {
        // Morning reminder: 10:30 AM (default)
        morningJob = scheduleDaily(
            timeSupplier = { settingsRepository.getMorningReminderTime() }
        ) {
            setLocale()
            val title = getString(Res.string.notification_morning_title)
            val message = getString(Res.string.notification_morning_message)
            MacOSNotification.send(title = title, message = message)
        }

        // Afternoon reminder: 4:30 PM (default)
        afternoonJob = scheduleDaily(
            timeSupplier = { settingsRepository.getAfternoonReminderTime() }
        ) {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val hasLogged = logRepository.hasLogForDate(today)

            if (!hasLogged) {
                setLocale()
                val title = getString(Res.string.notification_afternoon_title)
                val message = getString(Res.string.notification_afternoon_message)
                MacOSNotification.send(title = title, message = message)
            }
        }
    }

    fun stop() {
        morningJob?.cancel()
        afternoonJob?.cancel()
        scope.cancel()
    }

    private fun scheduleDaily(timeSupplier: () -> String, action: suspend () -> Unit): Job {
        return scope.launch {
            while (isActive) {
                try {
                    val timeString = timeSupplier()
                    val (hour, minute) = parseTime(timeString)

                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    val today = now.date
                    val target = LocalDateTime(today, LocalTime(hour, minute))

                    val delay = if (now < target) {
                        // Today's target time
                        target.toInstant(TimeZone.currentSystemDefault()) - Clock.System.now()
                    } else {
                        // Tomorrow's target time
                        val tomorrow = today.plus(1, DateTimeUnit.DAY)
                        LocalDateTime(tomorrow, LocalTime(hour, minute))
                            .toInstant(TimeZone.currentSystemDefault()) - Clock.System.now()
                    }

                    delay(delay.inWholeMilliseconds)
                    action()

                    // Wait a bit to avoid triggering multiple times
                    delay(60_000) // 1 minute
                } catch (e: Exception) {
                    println("Error in reminder scheduler: ${e.message}")
                    delay(60_000) // Wait 1 minute before retrying
                }
            }
        }
    }

    private fun parseTime(timeString: String): Pair<Int, Int> {
        val parts = timeString.split(":")
        require(parts.size == 2) { "Invalid time format: $timeString" }
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        require(hour in 0..23) { "Invalid hour: $hour" }
        require(minute in 0..59) { "Invalid minute: $minute" }
        return hour to minute
    }
}

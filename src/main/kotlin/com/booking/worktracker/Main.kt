package com.booking.worktracker

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.booking.worktracker.data.Database
import com.booking.worktracker.data.datasource.LogLocalDataSource
import com.booking.worktracker.data.datasource.ObjectiveLocalDataSource
import com.booking.worktracker.data.datasource.SettingsLocalDataSource
import com.booking.worktracker.data.datasource.TagLocalDataSource
import com.booking.worktracker.data.datasource.TimeEntryLocalDataSource
import com.booking.worktracker.data.datasource.AnalyticsLocalDataSource
import com.booking.worktracker.data.datasource.ExportLocalDataSource
import com.booking.worktracker.data.repository.LogRepository
import com.booking.worktracker.data.repository.ObjectiveRepository
import com.booking.worktracker.data.repository.SettingsRepository
import com.booking.worktracker.data.repository.TagRepository
import com.booking.worktracker.data.repository.TimeEntryRepository
import com.booking.worktracker.data.repository.AnalyticsRepository
import com.booking.worktracker.data.repository.ExportRepository
import com.booking.worktracker.notifications.ReminderScheduler
import com.booking.worktracker.ui.localization.AppLocale
import com.booking.worktracker.core.generated.resources.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import com.booking.worktracker.ui.App

fun main() = application {
    // Initialize database
    Database.init()

    // Create local data sources
    val logLocalDataSource = LogLocalDataSource()
    val tagLocalDataSource = TagLocalDataSource()
    val objectiveLocalDataSource = ObjectiveLocalDataSource()
    val settingsLocalDataSource = SettingsLocalDataSource()
    val timeEntryLocalDataSource = TimeEntryLocalDataSource()
    val analyticsLocalDataSource = AnalyticsLocalDataSource()
    val exportLocalDataSource = ExportLocalDataSource()

    // Create repositories
    val logRepository = LogRepository(logLocalDataSource)
    val tagRepository = TagRepository(tagLocalDataSource)
    val objectiveRepository = ObjectiveRepository(objectiveLocalDataSource)
    val settingsRepository = SettingsRepository(settingsLocalDataSource)
    val timeEntryRepository = TimeEntryRepository(timeEntryLocalDataSource)
    val analyticsRepository = AnalyticsRepository(analyticsLocalDataSource)
    val exportRepository = ExportRepository(exportLocalDataSource)

    // Set system locale for Compose Resources
    val locale = AppLocale.fromCode(settingsRepository.getLanguage())
    java.util.Locale.setDefault(java.util.Locale(locale.code))

    // Resolve window title from saved language
    val windowTitle = runBlocking { getString(Res.string.app_title) }

    // Start reminder scheduler
    val reminderScheduler = ReminderScheduler(logRepository, settingsRepository)
    reminderScheduler.start()

    Window(
        onCloseRequest = {
            reminderScheduler.stop()
            Database.close()
            exitApplication()
        },
        title = windowTitle,
        state = rememberWindowState(width = 900.dp, height = 700.dp)
    ) {
        App(
            logRepository = logRepository,
            tagRepository = tagRepository,
            objectiveRepository = objectiveRepository,
            settingsRepository = settingsRepository,
            timeEntryRepository = timeEntryRepository,
            analyticsRepository = analyticsRepository,
            exportRepository = exportRepository
        )
    }
}

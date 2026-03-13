package com.booking.worktracker

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.booking.worktracker.data.DatabaseProvider
import com.booking.worktracker.data.datasource.LogLocalDataSource
import com.booking.worktracker.data.datasource.SettingsLocalDataSource
import com.booking.worktracker.data.repository.LogRepository
import com.booking.worktracker.data.repository.SettingsRepository
import com.booking.worktracker.notifications.ReminderScheduler
import com.booking.worktracker.ui.App
import com.booking.worktracker.ui.localization.AppLocale
import com.booking.worktracker.core.generated.resources.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

fun main() = application {
    DatabaseProvider.init()

    // Set system locale for Compose Resources
    val db = DatabaseProvider.getDatabase()
    val settingsRepo = SettingsRepository(SettingsLocalDataSource(db))
    val locale = AppLocale.fromCode(settingsRepo.getLanguage())
    java.util.Locale.setDefault(java.util.Locale(locale.code))

    val windowTitle = runBlocking { getString(Res.string.app_title) }

    val logRepo = LogRepository(LogLocalDataSource(db))
    val reminderScheduler = ReminderScheduler(logRepo, settingsRepo)
    reminderScheduler.start()

    Window(
        onCloseRequest = {
            reminderScheduler.stop()
            DatabaseProvider.close()
            exitApplication()
        },
        title = windowTitle,
        state = rememberWindowState(width = 900.dp, height = 700.dp)
    ) {
        App()
    }
}

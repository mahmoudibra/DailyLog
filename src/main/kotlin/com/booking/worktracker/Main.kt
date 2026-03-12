package com.booking.worktracker

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.booking.worktracker.data.Database
import com.booking.worktracker.data.repository.LogRepository
import com.booking.worktracker.data.repository.SettingsRepository
import com.booking.worktracker.notifications.ReminderScheduler
import com.booking.worktracker.ui.localization.AppLocale
import com.booking.worktracker.core.generated.resources.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import com.booking.worktracker.ui.App

fun main() = application {
    Database.init()

    val settingsRepository = SettingsRepository()

    // Set system locale for Compose Resources
    val locale = AppLocale.fromCode(settingsRepository.getLanguage())
    java.util.Locale.setDefault(java.util.Locale(locale.code))

    val windowTitle = runBlocking { getString(Res.string.app_title) }

    val reminderScheduler = ReminderScheduler(LogRepository(), settingsRepository)
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
        App(settingsRepository = settingsRepository)
    }
}

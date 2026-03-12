package com.booking.worktracker

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.booking.worktracker.data.DatabaseProvider
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
    val settingsRepo = SettingsRepository()
    val locale = AppLocale.fromCode(settingsRepo.getLanguage())
    java.util.Locale.setDefault(java.util.Locale(locale.code))

    val windowTitle = runBlocking { getString(Res.string.app_title) }

    val reminderScheduler = ReminderScheduler()
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

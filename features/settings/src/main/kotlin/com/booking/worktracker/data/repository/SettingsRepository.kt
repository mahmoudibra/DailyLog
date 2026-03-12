package com.booking.worktracker.data.repository

import com.booking.worktracker.data.datasource.SettingsLocalDataSource

class SettingsRepository(
    private val localDataSource: SettingsLocalDataSource
) {

    fun getMorningReminderTime(): String {
        return localDataSource.getSetting("morning_reminder_time") ?: "10:30"
    }

    fun getAfternoonReminderTime(): String {
        return localDataSource.getSetting("afternoon_reminder_time") ?: "16:30"
    }

    fun setMorningReminderTime(time: String) {
        localDataSource.setSetting("morning_reminder_time", time)
    }

    fun setAfternoonReminderTime(time: String) {
        localDataSource.setSetting("afternoon_reminder_time", time)
    }

    fun getLanguage(): String {
        return localDataSource.getSetting("language") ?: "en"
    }

    fun setLanguage(code: String) {
        localDataSource.setSetting("language", code)
    }
}

package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.DatabaseProvider
import com.booking.worktracker.data.DailyWorkTrackerDatabase

class SettingsLocalDataSource(db: DailyWorkTrackerDatabase = DatabaseProvider.getDatabase()) {

    private val queries = db.settingsQueries

    fun getSetting(key: String): String? {
        return queries.getSetting(key).executeAsOneOrNull()
    }

    fun setSetting(key: String, value: String) {
        queries.setSetting(key, value)
    }
}

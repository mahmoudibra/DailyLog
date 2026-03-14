package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.DailyTrackerDatabase
import com.booking.worktracker.di.Singleton
import me.tatarka.inject.annotations.Inject

@Inject
@Singleton
class SettingsLocalDataSource(db: DailyTrackerDatabase) {

    private val queries = db.settingsQueries

    fun getSetting(key: String): String? {
        return queries.getSetting(key).executeAsOneOrNull()
    }

    fun setSetting(key: String, value: String) {
        queries.setSetting(key, value)
    }
}

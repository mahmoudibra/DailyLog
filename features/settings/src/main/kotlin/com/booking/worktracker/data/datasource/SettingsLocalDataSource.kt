package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.SettingsQueries

class SettingsLocalDataSource(private val queries: SettingsQueries) {

    fun getSetting(key: String): String? {
        return queries.getSetting(key).executeAsOneOrNull()
    }

    fun setSetting(key: String, value: String) {
        queries.setSetting(key, value)
    }
}

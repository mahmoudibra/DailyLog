package com.booking.worktracker.data.datasource

import com.booking.worktracker.data.Database

class SettingsLocalDataSource {

    fun getSetting(key: String): String? {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement("SELECT value FROM settings WHERE key = ?")
        stmt.setString(1, key)
        val rs = stmt.executeQuery()

        val value = if (rs.next()) rs.getString("value") else null
        rs.close()
        stmt.close()
        return value
    }

    fun setSetting(key: String, value: String) {
        val conn = Database.getConnection()
        val stmt = conn.prepareStatement(
            "INSERT INTO settings (key, value) VALUES (?, ?) ON CONFLICT(key) DO UPDATE SET value = excluded.value"
        )
        stmt.setString(1, key)
        stmt.setString(2, value)
        stmt.executeUpdate()
        stmt.close()
    }
}
